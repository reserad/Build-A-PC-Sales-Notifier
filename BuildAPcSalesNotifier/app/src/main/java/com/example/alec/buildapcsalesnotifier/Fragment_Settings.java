package com.example.alec.buildapcsalesnotifier;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

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
        vibrate.setOnClickListener(new CheckBox.OnClickListener() {
            public void onClick(View view) {
                tinyDB.putBoolean("vibrate", vibrate.isChecked());
            }
        });
        return view;
    }
}