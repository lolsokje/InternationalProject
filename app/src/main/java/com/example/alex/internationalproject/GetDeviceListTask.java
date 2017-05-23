package com.example.alex.internationalproject;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Alex on 22-5-2017.
 */

public class GetDeviceListTask extends AsyncTask<URL, Void, JSONArray> {
    private String mToken;
    private Context mContext;

    public AsyncResponse delegate = null;

    public GetDeviceListTask(Context context, String token) {
        mContext = context;
        mToken = token;
    }

    @Override
    protected void onPreExecute() { super.onPreExecute(); }

    @Override
    protected JSONArray doInBackground(URL... urls) {
        JSONArray result = null;
        try {
            result = getDeviceList(urls[0]);
        } catch(IOException e) {
            e.printStackTrace();
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(JSONArray json) {
        delegate.getDeviceListFinish(json);
    }

    private JSONArray getDeviceList(URL url) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String forecastJsonString = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("charset", "UTF-8");
            urlConnection.setRequestProperty("Authorization", mToken);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0) {
                return null;
            }

            forecastJsonString = buffer.toString();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
            if(reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        JSONArray ja = new JSONArray(forecastJsonString);
        return ja;
    }
}
