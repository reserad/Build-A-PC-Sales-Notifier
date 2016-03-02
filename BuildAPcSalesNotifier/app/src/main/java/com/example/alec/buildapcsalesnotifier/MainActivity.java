package com.example.alec.buildapcsalesnotifier;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.text.method.DigitsKeyListener;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_entries);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        try
        {
            manager.cancel(pendingIntent);
            startAlarmManager(pendingIntent);
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(), "ERROR MESSAGE COMING- CHOO CHOO: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        final Context context = this;
        refreshEntries(context);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LayoutInflater li = LayoutInflater.from(view.getContext());
                final View promptsView = li.inflate(R.layout.add_item, null);

                final EditText searchQuery = (EditText) promptsView.findViewById(R.id.edit_searchQuery);
                final EditText priceQuery = (EditText) promptsView.findViewById(R.id.edit_price);
                priceQuery.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.setCancelable(false)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Add", null);
                final AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                {
                    @Override
                    public void onShow(DialogInterface dialog)
                    {
                        Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                if (searchQuery.getText().toString().length() > 0 && priceQuery.getText().toString().length() > 0)
                                {
                                    TinyDB tinydb = new TinyDB(context);
                                    tinydb.putString(searchQuery.getText().toString(), priceQuery.getText().toString());
                                    refreshEntries(context);
                                    if (getCurrentFocus() != null)
                                    {
                                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        inputMethodManager.hideSoftInputFromWindow(promptsView.getWindowToken(), 0);
                                    }
                                    alertDialog.dismiss();
                                }
                                else if (searchQuery.getText().toString().length() == 0)
                                    Toast.makeText(MainActivity.this, "Query field cannot be empty", Toast.LENGTH_SHORT).show();
                                else if (priceQuery.getText().toString().length() == 0)
                                    Toast.makeText(MainActivity.this, "Price field cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView reddit = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.redditImageView);
        reddit.setOnClickListener(new ImageView.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://www.reddit.com/r/buildapcsales"));
                startActivity(i);
            }
        });
    }

    private void startAlarmManager(PendingIntent pendingIntent)
    {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                60000, pendingIntent);
    }

    private void refreshEntries(final Context context)
    {
        LinearLayout entryLayout = (LinearLayout) findViewById(R.id.entries);
        entryLayout.removeAllViewsInLayout();
        final TinyDB tinydb = new TinyDB(context);

        Iterator it = tinydb.getAll().entrySet().iterator();
        int addedQueries = 0;
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getValue() instanceof Boolean == false)
            {
                addedQueries++;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                final LinearLayout nested_li = new LinearLayout(context);
                nested_li.setOrientation(LinearLayout.HORIZONTAL);
                nested_li.setLayoutParams(lp);
                nested_li.setPadding(5, 5, 5, 5);
                nested_li.setBaselineAligned(false);
                nested_li.setGravity(Gravity.CENTER_VERTICAL);

                final TextView query = new TextView(context);
                query.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                query.setText(pair.getKey().toString());
                query.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                nested_li.addView(query);

                final TextView price = new TextView(context);
                price.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                price.setText("$" + pair.getValue().toString());
                price.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                nested_li.addView(price);

                Button remove = new Button(new ContextThemeWrapper(context,R.style.green_button),null,R.style.green_button);
                remove.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                remove.setText("DELETE");
                remove.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                nested_li.addView(remove);

                entryLayout.addView(nested_li);

                View spacerView = new View(this);
                spacerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                spacerView.setBackgroundColor(Color.LTGRAY);

                entryLayout.addView(spacerView);
                remove.setOnClickListener(new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        tinydb.remove(query.getText().toString());
                        refreshEntries(context);
                    }
                });

                nested_li.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this)
                {
                    @Override
                    public void onLongClick()
                    {
                        super.onLongClick();
                        LayoutInflater li = LayoutInflater.from(context);
                        final View promptsView = li.inflate(R.layout.edit_item, null);

                        TextView currentQueryValue = (TextView) promptsView.findViewById(R.id.tv_edit_item_query);
                        final EditText modifiableValue = (EditText) promptsView.findViewById(R.id.et_edit_item_query);

                        currentQueryValue.setText(query.getText());
                        modifiableValue.setText(query.getText());

                        TextView currentPriceValue = (TextView) promptsView.findViewById(R.id.tv_edit_item_price);
                        final EditText modifiablePriceValue = (EditText) promptsView.findViewById(R.id.et_edit_item_price);

                        modifiablePriceValue.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

                        currentPriceValue.setText(price.getText());
                        modifiablePriceValue.setText(price.getText().length() > 1 ? price.getText().subSequence(1, price.getText().length()) : price.getText());

                        Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        vibe.vibrate(50);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setView(promptsView);
                        alertDialogBuilder.setCancelable(false)
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Save", null);
                        final AlertDialog alertDialog = alertDialogBuilder.create();

                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                        {
                            @Override
                            public void onShow(DialogInterface dialog)
                            {
                                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        if (modifiableValue.getText().toString().length() > 0 && modifiablePriceValue.getText().toString().length() > 0)
                                        {
                                            TinyDB tinyDB = new TinyDB(context);
                                            tinyDB.remove(query.getText().toString());
                                            tinyDB.putString(modifiableValue.getText().toString(), modifiablePriceValue.getText().toString());
                                            refreshEntries(context);
                                            if (getCurrentFocus() != null)
                                            {
                                                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                inputMethodManager.hideSoftInputFromWindow(promptsView.getWindowToken(), 0);
                                            }
                                            alertDialog.dismiss();
                                        }
                                        else if (modifiableValue.getText().toString().length() == 0)
                                            Toast.makeText(MainActivity.this, "Query field cannot be empty", Toast.LENGTH_SHORT).show();
                                        else if (modifiablePriceValue.getText().toString().length() == 0)
                                            Toast.makeText(MainActivity.this, "Price field cannot be empty", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        alertDialog.show();
                    }

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN)
                            nested_li.setBackgroundColor(Color.rgb(234,234,234));
                        else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP)
                            nested_li.setBackgroundColor(Color.parseColor("#f9f9f9"));
                        return super.onTouch(view, motionEvent);
                    }
                });
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        if (addedQueries == 0)
            tinydb.clear();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
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
            startActivity(new Intent(getApplicationContext(), Settings.class));

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_deal_searches)
        {
            refreshEntries(getApplicationContext());
            return true;
        }

        Intent intent = null;

        if (id == R.id.nav_manage)
            intent = new Intent(getApplicationContext(), Settings.class);
        if (id == R.id.nav_past_deals)
            intent = new Intent(getApplicationContext(), PastDeals.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
