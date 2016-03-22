package com.example.alec.buildapcsalesnotifier;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alec Reser on 3/6/2016.
 */
public class Fragment_Past extends Fragment
{
    View fragment_past_view;
    public Fragment_Past(){}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        fragment_past_view = inflater.inflate(R.layout.fragment_past, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_past);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        refresh(getContext());
        return fragment_past_view;
    }

    private void refresh(final Context context)
    {
        LinearLayout entryLayout = (LinearLayout) fragment_past_view.findViewById(R.id.past_entries);
        entryLayout.removeAllViewsInLayout();
        final TinyDB tinydb = new TinyDB(context);


        Iterator it = tinydb.getAll().entrySet().iterator();
        ArrayList<PastDealsModel> pastDealsModelArrayList = new ArrayList<>();

        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            PastDealsModel pastDealsModel;
            try
            {
                if (PastDealsModel.isTypePastDealsModel(new JSONObject(tinydb.getString(pair.getKey().toString()))))
                {
                    pastDealsModel = (PastDealsModel) tinydb.getObject(pair.getKey().toString(), PastDealsModel.class);
                    pastDealsModelArrayList.add(pastDealsModel);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        Collections.sort(pastDealsModelArrayList, new Comparator<PastDealsModel>()
        {
            @Override
            public int compare(PastDealsModel p1, PastDealsModel p2)
            {
                return Long.compare(p1.Timestamp, p2.Timestamp); // Ascending
            }
        });

        for (final PastDealsModel pastDealsModel: pastDealsModelArrayList)
        {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2);
            LinearLayout nested_li = new LinearLayout(context);
            nested_li.setOrientation(LinearLayout.HORIZONTAL);
            nested_li.setLayoutParams(lp);
            nested_li.setPadding(5, 5, 5, 5);

            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            imageView.setPadding(0,0,10,0);

            if (Notification.isNetworkAvailable(context))
            {
                Bitmap thumbnail = Notification.getThumbnailFromURL(pastDealsModel.ThumbnailUrl);
                imageView.setImageBitmap(thumbnail);
            }
            nested_li.addView(imageView);

            final TextView url = new TextView(context);
            url.setText(pastDealsModel.Queries + " for $" + pastDealsModel.Price);
            url.setTextColor(Color.DKGRAY);
            url.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            nested_li.addView(url);

            url.setOnClickListener(new TextView.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("http://www.reddit.com" + pastDealsModel.Url));
                    startActivity(i);
                }
            });
            entryLayout.addView(nested_li);

            View spacerView = new View(context);
            spacerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            spacerView.setBackgroundColor(Color.LTGRAY);
            entryLayout.addView(spacerView);
        }
    }
}