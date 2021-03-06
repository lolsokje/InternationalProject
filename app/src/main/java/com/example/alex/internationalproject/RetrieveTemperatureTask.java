package com.example.alex.internationalproject;

import android.content.Context;
import android.os.AsyncTask;

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

/**
 * Created by Alex on 15-3-2017.
 */

public class RetrieveTemperatureTask extends AsyncTask<URL, Void, JSONArray> {
    private Context mContext;
    private String mToken;
    private String mSerial;
    public AsyncResponse delegate = null;

    public RetrieveTemperatureTask(Context context, String token, String serial) {
        mContext = context;
        mToken = token;
        mSerial = serial;
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
        HttpURLConnection urlConnection;
        InputStream iStream;

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-type", "application/json");
        urlConnection.setRequestProperty("Authorization", mToken);
        urlConnection.setRequestProperty("charset", "UTF-8");

        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);

        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        JSONObject jsonParams = new JSONObject();
        jsonParams.put("Serial", mSerial);

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
        }
        br.close();

        jsonString = sb.toString();

        JSONArray ja = new JSONArray(jsonString);
        return ja;
    }
}
