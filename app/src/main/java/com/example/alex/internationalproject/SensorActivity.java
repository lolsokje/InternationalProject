package com.example.alex.internationalproject;

import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class SensorActivity extends AppCompatActivity implements AsyncResponse {
    int temperature;

    URL url;

    RelativeLayout relLayout;
    TextView temperatureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        try {
            url = new URL("http://141.135.5.117:3000/userTemps");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        RetrieveTemperatureTask retrieveTemperatureTask = new RetrieveTemperatureTask(SensorActivity.this);
        retrieveTemperatureTask.delegate = this;
        retrieveTemperatureTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

        relLayout = (RelativeLayout)findViewById(R.id.activity_sensor);
        temperatureView = (TextView)findViewById(R.id.temperatureView);

        temperature = 37;
        temperatureView.setText(String.valueOf(temperature) + "°C"  );
        checkTemperature();
    }

    private void checkTemperature() {
        if(temperature < 38 == temperature > 36) {
            relLayout.setBackgroundColor(0xFF00CC00);
        } else if(temperature >= 38) {
            relLayout.setBackgroundColor(0xFFCC0000);
        } else if(temperature <= 36) {
            relLayout.setBackgroundColor(0xFF0066FF);
        }
    }

    @Override
    public void processFinish(JSONArray json) {
        try {
            JSONObject jo = json.getJSONObject(json.length()-1);
            String temp = jo.getString("val");
            temperature = Integer.parseInt(temp);
            temperatureView.setText(temp + "°C");
            checkTemperature();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
}
