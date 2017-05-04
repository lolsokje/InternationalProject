package com.example.alex.internationalproject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by Alex on 15-3-2017.
 */

public class RetrieveTemperatureTask extends AsyncTask<URL, Void, JSONArray> {
    private Context mContext;
    private String mUrl;
    public AsyncResponse delegate = null;

    public RetrieveTemperatureTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONArray doInBackground(URL... urls) {
        JSONArray result = null;
        try {
            result = getSingleTemperature(urls[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(JSONArray json) {
        delegate.processFinish(json);
    }

    private JSONArray getSingleTemperature(URL url) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        InputStream iStream;

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-type", "application/json");
        urlConnection.setRequestProperty("charset", "UTF-8");

        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);

        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        JSONObject jsonParams = new JSONObject();
        jsonParams.put("name", "Taart");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
        writer.write(jsonParams.toString());
        writer.flush();
        writer.close();

        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            iStream = urlConnection.getInputStream();
        } else {
            iStream = urlConnection.getErrorStream();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(iStream, "UTF-8"), 8);

        String jsonString;

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
            Log.d("TAG", "getJSONObjectFromURL: " + line);
        }
        br.close();

        jsonString = sb.toString();

        JSONArray ja = new JSONArray(jsonString);
        return ja;
    }

    private void getTemperatureInRange(URL url, Date begin, Date end) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);

            int data = isw.read();
            while(data != -1) {
                char current = (char) data;
                data = isw.read();
                BufferedReader br = new BufferedReader(isw, 8);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                    Log.d("TAG", "getJSONObjectFromURL: " + line);
                }
                br.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
