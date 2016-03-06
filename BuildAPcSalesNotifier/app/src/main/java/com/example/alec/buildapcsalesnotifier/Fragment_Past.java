package com.example.alec.buildapcsalesnotifier;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alec Reser on 3/6/2016.
 */
public class Fragment_Past extends Fragment {
    View fragment_past_view;
    public Fragment_Past(){}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragment_past_view = inflater.inflate(R.layout.fragment_past, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_past);
        refresh(getContext());
        return fragment_past_view;
    }

    private void refresh(final Context context)
    {
        LinearLayout entryLayout = (LinearLayout) fragment_past_view.findViewById(R.id.past_entries);
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

                View spacerView = new View(context);
                spacerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                spacerView.setBackgroundColor(Color.DKGRAY);
                entryLayout.addView(spacerView);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
