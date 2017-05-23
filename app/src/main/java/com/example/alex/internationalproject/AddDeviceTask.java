package com.example.alex.internationalproject;

import android.content.Context;
import android.os.AsyncTask;

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
 * Created by Alex on 22-5-2017.
 */

public class AddDeviceTask extends AsyncTask<URL, Void,  JSONObject> {
    private Context mContext;
    public AsyncResponse delegate = null;

    private String mName;
    private String mDeviceId;
    private String mToken;

    public AddDeviceTask(Context context, String name, String deviceId, String token) {
        mContext = context;
        mName = name;
        mDeviceId = deviceId;
        mToken = token;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(URL... urls) {
        JSONObject result = null;
        try {
            result = addDevice(urls[0]);
        } catch(IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(JSONObject json) {
        delegate.addDeviceFinish(json);
    }

    private JSONObject addDevice(URL url) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
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
        jsonParams.put("Name", mName);
        jsonParams.put("Serial", mDeviceId);

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

        JSONObject jo = new JSONObject(jsonString);
        return jo;
    }
}