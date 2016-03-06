package com.example.alec.buildapcsalesnotifier;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Alec on 2/26/2016.
 */
public class Notification extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        final String url = getIntent().getStringExtra("dealUrl");
        String query = getIntent().getStringExtra("dealQuery");

        TextView title = (TextView) findViewById(R.id.notificationTitle);
        String t = getIntent().getStringExtra("dealTitle").toString();
        title.setText(Html.fromHtml(t.replaceAll("(?i)" + query, "<b><font color=red>" + query + "</font></b>")));

        TextView price = (TextView) findViewById(R.id.notificationPrice);
        price.setText(Html.fromHtml("<b><font color=rgb(66,133,244)>$" + getIntent().getIntExtra("dealPrice", 0) + "</font></b>"));

        Button viewDeal = (Button) findViewById(R.id.btnNavigate);
        Button shareDeal = (Button) findViewById(R.id.btnShare);;

        viewDeal.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}
