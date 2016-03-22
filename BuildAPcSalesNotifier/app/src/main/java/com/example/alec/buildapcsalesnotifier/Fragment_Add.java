package com.example.alec.buildapcsalesnotifier;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alec Reser on 3/6/2016.
 */
public class Fragment_Add extends Fragment {
    View fragment_add_view;
    public Fragment_Add(){}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragment_add_view = inflater.inflate(R.layout.fragment_add, container, false);
        refreshEntries(getContext(), fragment_add_view);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_entries);

        FloatingActionButton fab = (FloatingActionButton) fragment_add_view.findViewById(R.id.fab);
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
                            public void onClick(View v)
                            {
                                if (searchQuery.getText().toString().length() > 0 && priceQuery.getText().toString().length() > 0)
                                {
                                    TinyDB tinydb = new TinyDB(fragment_add_view.getContext());

                                    AddedDealsModel addedDealsModel = new AddedDealsModel(Integer.parseInt(priceQuery.getText().toString()), Calendar.getInstance().getTimeInMillis(), searchQuery.getText().toString());
                                    tinydb.putObject(searchQuery.getText().toString(), addedDealsModel);
                                    refreshEntries(getContext(), fragment_add_view);
                                    MainActivity.hideKeyboard(getActivity());
                                    alertDialog.dismiss();
                                }
                                else if (searchQuery.getText().toString().length() == 0)
                                    Toast.makeText(getContext(), "Query field cannot be empty", Toast.LENGTH_SHORT).show();
                                else if (priceQuery.getText().toString().length() == 0)
                                    Toast.makeText(getContext(), "Price field cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                alertDialog.show();
            }
        });


        return fragment_add_view;
    }

    private void refreshEntries(final Context context, View view)
    {
        LinearLayout entryLayout = (LinearLayout) view.findViewById(R.id.entries);
        entryLayout.removeAllViewsInLayout();
        final TinyDB tinydb = new TinyDB(context);

        Iterator it = tinydb.getAll().entrySet().iterator();
        ArrayList<AddedDealsModel> addedDealsModelArrayList = new ArrayList<>();

        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            AddedDealsModel addedDealsModel;
            try
            {
                if (AddedDealsModel.isTypeAddedDealsModel(new JSONObject(tinydb.getString(pair.getKey().toString()))))
                {
                    addedDealsModel = (AddedDealsModel) tinydb.getObject(pair.getKey().toString(), AddedDealsModel.class);
                    addedDealsModelArrayList.add(addedDealsModel);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        Collections.sort(addedDealsModelArrayList, new Comparator<AddedDealsModel>() {
            @Override
            public int compare(AddedDealsModel p1, AddedDealsModel p2) {
                return Long.compare(p1.Timestamp, p2.Timestamp); // Ascending
            }
        });

        for (AddedDealsModel addedDealsModel: addedDealsModelArrayList)
        {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            final LinearLayout nested_li = new LinearLayout(context);
            nested_li.setOrientation(LinearLayout.HORIZONTAL);
            nested_li.setLayoutParams(lp);
            nested_li.setPadding(5, 5, 5, 5);
            nested_li.setBaselineAligned(false);
            nested_li.setGravity(Gravity.CENTER_VERTICAL);

            final TextView query = new TextView(context);
            query.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            query.setText(addedDealsModel.Queries);
            query.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            nested_li.addView(query);

            final TextView price = new TextView(context);
            price.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            price.setText("$" + addedDealsModel.Price);
            price.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            nested_li.addView(price);

            Button remove = new Button(new ContextThemeWrapper(context,R.style.green_button),null,R.style.green_button);
            remove.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            remove.setText("DELETE");
            remove.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            nested_li.addView(remove);

            entryLayout.addView(nested_li);

            View spacerView = new View(view.getContext());
            spacerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            spacerView.setBackgroundColor(Color.LTGRAY);

            entryLayout.addView(spacerView);
            remove.setOnClickListener(new Button.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    tinydb.remove(query.getText().toString());
                    refreshEntries(context, fragment_add_view);
                }
            });

            nested_li.setOnTouchListener(new OnSwipeTouchListener(getActivity())
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
                                        AddedDealsModel addedDealsModel = new AddedDealsModel(Integer.parseInt(modifiablePriceValue.getText().toString()), Calendar.getInstance().getTimeInMillis(), modifiableValue.getText().toString());
                                        tinyDB.putObject(modifiableValue.getText().toString(), addedDealsModel);
                                        refreshEntries(context, fragment_add_view);
                                        MainActivity.hideKeyboard(getActivity());
                                        alertDialog.dismiss();
                                    }
                                    else if (modifiableValue.getText().toString().length() == 0)
                                        Toast.makeText(getContext(), "Query field cannot be empty", Toast.LENGTH_SHORT).show();
                                    else if (modifiablePriceValue.getText().toString().length() == 0)
                                        Toast.makeText(getContext(), "Price field cannot be empty", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    alertDialog.show();
                }

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN)
                        nested_li.setBackgroundColor(Color.rgb(234,234,234));
                    else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP)
                        nested_li.setBackgroundColor(Color.parseColor("#f9f9f9"));
                    return super.onTouch(view, motionEvent);
                }
            });
        }
    }
}