package com.example.alec.buildapcsalesnotifier;

import org.json.JSONObject;

/**
 * Created by Alec on 3/21/2016.
 */
public class PastDealsModel
{
    public String Queries;
    public String Url;
    public String ThumbnailUrl;
    public int Timestamp;
    public int Price;
    public PastDealsModel(String Url, int Timestamp, String Queries, int Price, String ThumbnailUrl)
    {
        this.Url = Url;
        this.Timestamp = Timestamp;
        this.Queries = Queries;
        this.Price = Price;
        this.ThumbnailUrl = ThumbnailUrl;
    }

    public static boolean isTypePastDealsModel(JSONObject json) {
        return json != null && json.has("Url") && json.has("Queries") && json.has("Timestamp") && json.has("Price") && json.has("ThumbnailUrl");
    }
}
