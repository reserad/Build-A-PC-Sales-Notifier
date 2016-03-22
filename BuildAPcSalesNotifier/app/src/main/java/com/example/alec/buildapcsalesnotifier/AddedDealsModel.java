package com.example.alec.buildapcsalesnotifier;

import org.json.JSONObject;

/**
 * Created by Alec on 3/21/2016.
 */
public class AddedDealsModel
{
    public String Queries;
    public int Price;
    public Long Timestamp;
    public AddedDealsModel(int Price, Long Timestamp, String Queries)
    {
        this.Price = Price;
        this.Timestamp = Timestamp;
        this.Queries = Queries;
    }

    public static boolean isTypeAddedDealsModel(JSONObject json) {
        return json != null && json.has("Queries") && json.has("Price") && json.has("Timestamp") && !PastDealsModel.isTypePastDealsModel(json);
    }
}