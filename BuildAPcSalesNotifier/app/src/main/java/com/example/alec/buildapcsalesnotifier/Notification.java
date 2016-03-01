package com.example.alec.buildapcsalesnotifier;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Alec on 2/26/2016.
 */
public class Notification extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
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
        Button shareDeal = (Button) findViewById(R.id.btnShare);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView reddit = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.redditImageView);
        reddit.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://www.reddit.com/r/buildapcsales"));
                startActivity(i);
            }
        });

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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_deal_searches)
            intent = new Intent(getApplicationContext(), MainActivity.class);

        if (id == R.id.nav_past_deals)
            intent = new Intent(getApplicationContext(), PastDeals.class);

        if (id == R.id.nav_manage)
            intent = new Intent(getApplicationContext(), Settings.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
