package com.example.alec.buildapcsalesnotifier;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

        Bitmap thumbnail = getThumbnailFromURL(getIntent().getStringExtra("Thumbnail").toString());
        Log.d("Width, Height: ", thumbnail.getWidth() + ", " + thumbnail.getHeight());

        ImageView imageView = (ImageView) findViewById(R.id.thumbnail);
        imageView.setImageBitmap(thumbnail);

        final String url = getIntent().getStringExtra("dealUrl");
        String query = getIntent().getStringExtra("dealQuery");

        TextView title = (TextView) findViewById(R.id.notificationTitle);
        String dealTitle = getIntent().getStringExtra("dealTitle").toString();

        String[] queries = query.split(" ");
        for (String q:queries)
            dealTitle = dealTitle.replaceAll("(?i)" + q, "<b><font color=red>" + q + "</font></b>");
        title.setText(Html.fromHtml(dealTitle));

        TextView price = (TextView) findViewById(R.id.notificationPrice);
        price.setText(Html.fromHtml("<b><font color=blue>$" + getIntent().getIntExtra("dealPrice", 0) + "</font></b>"));

        Button viewDeal = (Button) findViewById(R.id.btnNavigate);
        Button shareDeal = (Button) findViewById(R.id.btnShare);;

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

    public Bitmap getThumbnailFromURL(String url)
    {
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            return Bitmap.createScaledBitmap(bitmap, (int)Math.round(bitmap.getWidth() * 1.5), (int)Math.round(bitmap.getHeight() * 1.5), true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return getThumbnailFromURL("https://b.thumbs.redditmedia.com/uUaUv2ttf2MSfaGFPTu93ZLzNZ9hJcHYUeN6a9E0LyI.png");
        }
    }

    public void onBackPressed()
    {
        super.onBackPressed();
    }
}