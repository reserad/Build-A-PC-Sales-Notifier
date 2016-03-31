package com.example.alec.buildapcsalesnotifier;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alec on 2/26/2016.
 */
public class Notification extends ActionBarActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.app_deal);
        setContentView(R.layout.notification);

        if (isNetworkAvailable(getApplicationContext()))
        {
            Bitmap thumbnail = getThumbnailFromURL(getIntent().getStringExtra("Thumbnail"));
            ImageView imageView = (ImageView) findViewById(R.id.thumbnail);
            imageView.setImageBitmap(thumbnail);
        }

        final String url = getIntent().getStringExtra("dealUrl");
        String query = getIntent().getStringExtra("dealQuery");

        TextView title = (TextView) findViewById(R.id.notificationTitle);
        String dealTitle = getIntent().getStringExtra("dealTitle");

        String[] queries = query.split(" ");
        for (String q:queries)
            dealTitle = dealTitle.replaceAll("(?i)" + q, "<b><font color=red>" + q + "</font></b>");
        title.setText(Html.fromHtml(dealTitle));

        TextView price = (TextView) findViewById(R.id.notificationPrice);
        price.setText(Html.fromHtml("<b><font color=blue>$" + getIntent().getIntExtra("dealPrice", 0) + "</font></b>"));

        Button viewDeal = (Button) findViewById(R.id.btnNavigate);
        Button shareDeal = (Button) findViewById(R.id.btnShare);

        viewDeal.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://www.reddit.com" + url));
                startActivity(i);
            }
        });

        shareDeal.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "http://www.reddit.com" + url);
                        startActivity(intent);
            }
        });
    }

    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Bitmap getThumbnailFromURL(String url)
    {
        Bitmap bitmap;
        try
        {
            if (url.contains("nsfw"))
                url = "http://b.thumbs.redditmedia.com/uUaUv2ttf2MSfaGFPTu93ZLzNZ9hJcHYUeN6a9E0LyI.png";
            if (!url.contains("http://"))
                url = "http://" + url;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            bitmap = BitmapFactory.decodeStream(input, null, options);
            connection.disconnect();
            return Bitmap.createScaledBitmap(bitmap, (int)Math.round(bitmap.getWidth() * 1.5), (int)Math.round(bitmap.getHeight() * 1.5), true);
        }
        catch (IOException e) { }
        return Bitmap.createBitmap(1,1, Bitmap.Config.ALPHA_8);
    }

    public void onBackPressed()
    {
        super.onBackPressed();
    }
}