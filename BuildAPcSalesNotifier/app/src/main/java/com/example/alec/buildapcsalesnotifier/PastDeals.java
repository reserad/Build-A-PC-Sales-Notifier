package com.example.alec.buildapcsalesnotifier;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alec on 2/27/2016.
 */
public class PastDeals extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.past_deals);
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

        refresh(getApplicationContext());
    }

    private void refresh(final Context context)
    {
        LinearLayout entryLayout = (LinearLayout) findViewById(R.id.past_entries);
        entryLayout.removeAllViewsInLayout();
        final TinyDB tinydb = new TinyDB(context);

        Iterator it = tinydb.getAll().entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getValue() instanceof Boolean == true && !pair.getKey().equals("vibrate")) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                LinearLayout nested_li = new LinearLayout(context);
                nested_li.setOrientation(LinearLayout.HORIZONTAL);
                nested_li.setLayoutParams(lp);
                nested_li.setPadding(5, 5, 5, 5);

                final TextView url = new TextView(context);
                url.setText("http://www.reddit.com" + pair.getKey().toString());
                url.setTextColor(Color.DKGRAY);
                url.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                nested_li.addView(url);

                url.setOnClickListener(new TextView.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url.getText().toString()));
                        startActivity(i);
                    }
                });
                entryLayout.addView(nested_li);

                View spacerView = new View(this);
                spacerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                spacerView.setBackgroundColor(Color.DKGRAY);
                entryLayout.addView(spacerView);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
            super.onBackPressed();
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