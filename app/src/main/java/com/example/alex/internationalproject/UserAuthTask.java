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

import static android.content.ContentValues.TAG;

/**
 * Created by Alex on 4-5-2017.
 */

public class UserAuthTask extends AsyncTask<URL, Void, String> {
    public AsyncResponse delegate = null;

    private Context mContext;

    private String mName;
    private String mPassword;
    private String mAction;

    public UserAuthTask(Context context, String name, String password, String action) {
        mContext = context;
        mName = name;
        mPassword = password;
        mAction = action;
    }

    @Override
    protected void onPreExecute() { super.onPreExecute();}

    @Override
    protected String doInBackground(URL... urls) {
        String result = null;

        try {
            result = doSomething(urls[0]);
        } catch(IOException e) {
            e.printStackTrace();
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) { delegate.processFinish(result);}

    private String doSomething(URL url) throws IOException, JSONException{
        HttpURLConnection urlConnection;
        InputStream iStream;

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");


        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);

        urlConnection.setDoInput(true);
        urlConnection.setDoInput(true);

        JSONObject jsonParams = new JSONObject();
        jsonParams.put("name", mName);
        jsonParams.put("password", mPassword);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
        writer.write(jsonParams.toString());
        writer.flush();
        writer.close();

        if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            iStream = urlConnection.getInputStream();
        } else {
            iStream = urlConnection.getErrorStream();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(iStream, "UTF-8"), 8);

        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        return sb.toString();
    }
}
