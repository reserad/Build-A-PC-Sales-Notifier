package com.example.alec.buildapcsalesnotifier;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Created by Alec on 2/27/2016.
 */
public class Fragment_Settings extends Fragment
{
    public Fragment_Settings(){}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_settings);
        final TinyDB tinyDB = new TinyDB(view.getContext());

        final CheckBox vibrate = (CheckBox) view.findViewById(R.id.vibrate);
        vibrate.setChecked(tinyDB.getBoolean("vibrate"));

        final Spinner spinner = (Spinner) view.findViewById(R.id.notificationLight);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.colors_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        for (int i = 0; i < adapter.getCount(); i++)
        {
            if (tinyDB.getBoolean((String) adapter.getItem(i)))
            {
                spinner.setSelection(i);
                break;
            }
        }

        vibrate.setOnClickListener(new CheckBox.OnClickListener() {
            public void onClick(View view) {
                tinyDB.putBoolean("vibrate", vibrate.isChecked());
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                for (int iterator = 0; iterator < adapter.getCount(); iterator++)
                    tinyDB.putBoolean((String) adapter.getItem(iterator), false);
                tinyDB.putBoolean(spinner.getSelectedItem().toString(), true);
            }
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        return view;
    }
}