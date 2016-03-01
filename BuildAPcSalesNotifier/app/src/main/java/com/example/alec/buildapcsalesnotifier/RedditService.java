package com.example.alec.buildapcsalesnotifier;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alec on 2/24/2016.
 */
public class RedditService extends Service {
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private JSONObject getJsonObject(String sURL) throws IOException, JSONException
    {
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
        InputStream inputStream = new URL(url).openStream();
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String jsonText = readAll(bufferedReader);
            return new JSONObject(jsonText);
        }
        finally
        {
            inputStream.close();
        }
    }

    private int priceFound(String Title)
    {
        int i = 0;
        while (Title.charAt(i) != '$')
            i++;
        i+=1;
        int j = i;
        while (Character.isDigit(Title.charAt(j)) || Title.charAt(j) == ' ')
            j++;

        String[] items = Title.substring(i, j).trim().split(" ", 2);
        if (items.length > 1)
            return Integer.parseInt(items[0].trim());

        return Integer.parseInt(Title.substring(i, j).trim());
    }

    private void task(Handler handler)
    {
        if (isNetworkAvailable())
        {
            String url = "https://www.reddit.com/r/buildapcsales/search.json?q=&sort=new&restrict_sr=on";
            try
            {
                JSONArray jsonArray = getJsonObject(url).getJSONObject("data").getJSONArray("children");
                final TinyDB tinyDB = new TinyDB(getApplicationContext());

                searchForDeal(tinyDB.getAll().entrySet().iterator(), jsonArray, tinyDB);
            } catch (JSONException | IOException e)
            {
                e.printStackTrace();
            }
            scheduleTask(handler);
        }
    }

    private void scheduleTask(final Handler handler)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                task(handler);
            }
        }, 30000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        scheduleTask(new Handler());
        return START_STICKY;
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

    private void searchForDeal(Iterator iterator, JSONArray jsonArray, TinyDB tinyDB) throws JSONException
    {
        while (iterator.hasNext())
        {
            Map.Entry pair = (Map.Entry) iterator.next();
            if (!(pair.getKey() instanceof Boolean))
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    String title = jsonArray.getJSONObject(i).getJSONObject("data").getString("title");
                    String linkUrl = jsonArray.getJSONObject(i).getJSONObject("data").getString("permalink");
                    if (findSearchTermsInTitle(title.toLowerCase(), pair.getKey().toString().toLowerCase()))
                    {
                        int price;
                        try
                        {
                            price = priceFound(title);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            break;
                        }
                        if (Integer.parseInt(pair.getValue().toString()) >= price)
                        {
                            if(!tinyDB.getBoolean(linkUrl))
                            {
                                Intent intent = new Intent(getApplicationContext(), Notification.class);
                                intent.putExtra("dealQuery", pair.getKey().toString());
                                intent.putExtra("dealTitle", title);
                                intent.putExtra("dealPrice", price);
                                intent.putExtra("dealUrl", linkUrl);

                                sendNotification(tinyDB, pair, title, price, PendingIntent.getActivity(getApplicationContext(), (int)Calendar.getInstance().getTimeInMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                tinyDB.putBoolean(linkUrl, true);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendNotification(TinyDB tinyDB, Map.Entry pair, String title, int price, PendingIntent pendingIntent)
    {
        android.app.Notification builder;
        if (tinyDB.getBoolean("vibrate"))
        {
            builder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_menu_send)
                    .setContentTitle(pair.getKey().toString() + " for $" + price)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setContentText(title)
                    .extend(
                            new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                    .build();
        }
        else
        {
            builder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_menu_send)
                    .setContentTitle(pair.getKey().toString() + " for $" + price)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{1000, 1000, 1000})
                    .setAutoCancel(true)
                    .setContentText(title)
                    .extend(
                            new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                    .build();
            Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)Calendar.getInstance().getTimeInMillis(), builder);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}