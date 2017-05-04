package com.example.alex.internationalproject;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;

import java.net.URL;

/**
 * Created by Alex on 4-5-2017.
 */

public class RetrieveAverageTemperatureTask extends AsyncTask<URL, Void, JSONArray> {
    public AsyncResponse delegate = null;

    private String mFromDate;
    private String mToDate;

    private Context mContext;

    public RetrieveAverageTemperatureTask(Context context, String fromDate, String toDate) {
        mContext = context;
        mFromDate = fromDate;
        mToDate = toDate;
    }

    @Override
    protected void onPreExecute() { super.onPreExecute();}

    @Override
    protected JSONArray doInBackground(URL... urls) {
        getAverageTemperature(urls[0], "", "");
        return new JSONArray();
    }

    @Override
    protected void onPostExecute(JSONArray json) {delegate.processFinish(json);}

    private JSONArray getAverageTemperature(URL url, String fromDate, String toDate) {
      return new JSONArray();
    }
}
