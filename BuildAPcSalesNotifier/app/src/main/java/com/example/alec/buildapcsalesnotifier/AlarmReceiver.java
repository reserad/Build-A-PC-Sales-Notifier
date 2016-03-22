package com.example.alec.buildapcsalesnotifier;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alec on 3/1/2016.
 */

public class AlarmReceiver extends BroadcastReceiver
{
    private Context context;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.context = context;
        if (isNetworkAvailable())
        {
            String url = "http://gotshrekt.com/api/Redditapi";
            try
            {
                JSONArray jsonArray = getJsonObject(url).getJSONObject("data").getJSONArray("children");
                final TinyDB tinyDB = new TinyDB(context);

                searchForDeal(tinyDB.getAll().entrySet().iterator(), jsonArray, tinyDB);
            }
            catch (JSONException | IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean isNetworkAvailable()
    {
        //Determines network availability
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private JSONObject getJsonObject(String sURL) throws IOException, JSONException
    {
        //Returns json from a specified URL
        URL url = new URL(sURL);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("GET");
        request.setRequestProperty("Content-length", "0");
        request.setUseCaches(false);
        request.setAllowUserInteraction(false);
        request.connect();
        return readJsonFromUrl(sURL);
    }

    private static String readAll(Reader reader) throws IOException
    {
        StringBuilder stringBuilder = new StringBuilder();
        int cp;
        while ((cp = reader.read()) != -1)
        {
            stringBuilder.append((char) cp);
        }
        return stringBuilder.toString();
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException
    {
        try (InputStream inputStream = new URL(url).openStream())
        {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String jsonText = readAll(bufferedReader);
            return new JSONObject(jsonText);
        }
    }

    private int priceFound(String Title)
    {
        //Attempts to parse price from post title
        int i = 0;
        while (Title.charAt(i) != '$')
            i++;
        i += 1;
        int j = i;
        while (j < Title.length() && (Character.isDigit(Title.charAt(j)) || Title.charAt(j) == ' ' || Title.charAt(j) == ','))
            j++;

        String[] items = Title.substring(i, j).trim().replace(",","").split(" ", 2);
        return Integer.parseInt(items[0].trim());
    }

    private static boolean findSearchTermsInTitle(String title, String searchTerm)
    {
        //Parses 'searchTerm' into a String array, looks for all cases in 'title' and determines if the SearchTerm is sufficient ('acceptancePercentage')
        String[] terms = searchTerm.split(" ");
        double acceptancePercentage = 0.5;
        int correctHits = 0;
        for(String term:terms)
        {
            if (title.contains(term))
                correctHits++;
        }

        if (terms.length > 2 && (double)correctHits / (double)terms.length > acceptancePercentage)
            return true;
        else if(terms.length == correctHits)
            return true;

        return false;
    }

    private void searchForDeal(Iterator iterator, JSONArray jsonArray, TinyDB tinyDB)
    {
        //Iterates through all user data looking for matches
        ArrayList<AddedDealsModel> addedDealsModelArrayList = new ArrayList<>();

        while (iterator.hasNext())
        {
            Map.Entry pair = (Map.Entry)iterator.next();
            AddedDealsModel addedDealsModel;
            try
            {
                addedDealsModel = (AddedDealsModel) tinyDB.getObject(pair.getKey().toString(), AddedDealsModel.class);
                addedDealsModelArrayList.add(addedDealsModel);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            iterator.remove(); // avoids a ConcurrentModificationException
        }

        Collections.sort(addedDealsModelArrayList, new Comparator<AddedDealsModel>()
        {
            @Override
            public int compare(AddedDealsModel p1, AddedDealsModel p2)
            {
                return Long.compare(p1.Timestamp, p2.Timestamp); // Ascending
            }
        });

        for (AddedDealsModel addedDealsModel: addedDealsModelArrayList)
        {
            for (int i = 0; i < jsonArray.length(); i++)
            {
                try
                {
                    String title = jsonArray.getJSONObject(i).getJSONObject("data").getString("title");
                    String linkUrl = jsonArray.getJSONObject(i).getJSONObject("data").getString("permalink");
                    if (findSearchTermsInTitle(title.toLowerCase(), addedDealsModel.Queries.toLowerCase()))
                    {
                        int price = priceFound(title);
                        if (addedDealsModel.Price >= price)
                        {
                            try
                            {
                                tinyDB.getObject(linkUrl, PastDealsModel.class);
                            }
                            catch(Exception e)
                            {
                                //If not already alerted to user, send notification
                                Intent intent = new Intent(context, Notification.class);
                                intent.putExtra("Thumbnail",jsonArray.getJSONObject(i).getJSONObject("data").getString("thumbnail"));
                                intent.putExtra("dealQuery", addedDealsModel.Queries);
                                intent.putExtra("dealTitle", title);
                                intent.putExtra("dealPrice", price);
                                intent.putExtra("dealUrl", linkUrl);

                                sendNotification(tinyDB, addedDealsModel.Queries, title, price, PendingIntent.getActivity(context, (int) Calendar.getInstance().getTimeInMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                PastDealsModel pastDealsModel = new PastDealsModel(linkUrl, (int) Calendar.getInstance().getTimeInMillis(), addedDealsModel.Queries, price, jsonArray.getJSONObject(i).getJSONObject("data").getString("thumbnail"));
                                tinyDB.putObject(linkUrl, pastDealsModel);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(context,e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendNotification(TinyDB tinyDB, String query, String title, int price, PendingIntent pendingIntent)
    {
        //Sends notification to user with a color variable and vibration determined by user settings
        int color = getNotificationColor(tinyDB);
        android.app.Notification builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.reddit)
                .setContentTitle(query + " for $" + price)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentText(title)
                .setLights(color, 1000, 2000)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();

        if (!tinyDB.getBoolean("vibrate"))
        {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) Calendar.getInstance().getTimeInMillis(), builder);
    }

    private int getNotificationColor(TinyDB tinyDB)
    {
        //Returns notification color defined by settings
        int color;
        if (tinyDB.getBoolean("Red"))
            color = Color.RED;
        else if (tinyDB.getBoolean("Green"))
            color = Color.GREEN;
        else if (tinyDB.getBoolean("Magenta"))
            color = Color.MAGENTA;
        else if (tinyDB.getBoolean("Yellow"))
            color = Color.YELLOW;
        else
            color = Color.BLUE;
        return color;
    }
}