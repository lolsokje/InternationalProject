package com.example.alex.internationalproject;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class SensorActivity extends AppCompatActivity implements AsyncResponse, DatePickerDialog.OnDateSetListener {
    boolean networkAvailable;

    int temperature;

    URL temperatureUrl;
    URL averageTempUrl;

    Button logOutButton;

    RelativeLayout relLayout;
    TextView temperatureView;

    TextView fromDateTextView;
    TextView toDateTextView;
    Button getAverageTempButton;

    String fromDate;
    String toDate;

    int startYear, startMonth, startDay;
    int fromYear, fromMonth, fromDay, toYear, toMonth, toDay;

    DatePickerDialog.OnDateSetListener from_dateListener, to_dateListener;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        sharedPref = this.getSharedPreferences("TOKEN", MODE_PRIVATE);
        editor = sharedPref.edit();

        Intent intent = getIntent();
        String messageExtra = intent.getStringExtra("AUTHENTICATED");

        if(sharedPref.contains("token") || messageExtra.equals("true")) {
            Calendar calendar = Calendar.getInstance();
            startYear = calendar.get(Calendar.YEAR);
            startMonth = calendar.get(Calendar.MONTH);
            startDay = calendar.get(Calendar.DAY_OF_MONTH);

            fromDate = "";
            toDate = "";

            logOutButton = (Button) findViewById(R.id.logOutButton);

            fromDateTextView = (TextView) findViewById(R.id.beginDateTextView);
            toDateTextView = (TextView) findViewById(R.id.endDateTextView);
            getAverageTempButton = (Button) findViewById(R.id.averageTempButton);

            networkAvailable = isNetworkAvailable();

            setOnClickListeners();

            if (networkAvailable) {
                try {
                    temperatureUrl = new URL("http://141.135.5.117:3500/user/temps");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                RetrieveTemperatureTask retrieveTemperatureTask = new RetrieveTemperatureTask(SensorActivity.this);
                retrieveTemperatureTask.delegate = this;
                retrieveTemperatureTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, temperatureUrl);

                relLayout = (RelativeLayout) findViewById(R.id.activity_sensor);
                temperatureView = (TextView) findViewById(R.id.temperatureView);

                temperature = 0;
                temperatureView.setText(String.valueOf(temperature) + "°C");
                checkTemperature();
            } else {
                buildAlertDialog(getResources().getString(R.string.network_error_title), getResources().getString(R.string.network_error_message));
            }
        } else {
            goToSignUpActivity();
        }
    }

    private void createDialog(int id) {
        switch(id) {
            case 0:
                new DatePickerDialog(this, from_dateListener, startYear, startMonth, startDay).show();
                break;
            case 1:
                new DatePickerDialog(this, to_dateListener, startYear, startMonth, startDay).show();
                break;
        }
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
            if(json != null) {
                JSONObject jo = json.getJSONObject(json.length() - 1);
                String temp = jo.getString("val");
                temperature = Integer.parseInt(temp);
                temperatureView.setText(temp + "°C");
                checkTemperature();
            } else {
                buildAlertDialog(getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_message));
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void buildAlertDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(SensorActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        TextView beginDate = (TextView) findViewById(R.id.beginDateTextView);
        beginDate.setText(day + "/" + (month + 1) + "/" + year);
    }

    private String[] getStringValue(int day, int month) {
        String dayString;
        String monthString;

        String[] strings = new String[2];

        if(day < 10)
            dayString = "0"+day;
        else
            dayString = Integer.toString(day);

        if(month < 10)
            monthString = "0"+ (month + 1);
        else
            monthString = Integer.toString((month + 1));

        strings[0] = dayString;
        strings[1] = monthString;

        return strings;
    }

    private void getAverageTemperature() {
        if(fromDate == "" || toDate == "") {
            Toast.makeText(this, "No dates selected", Toast.LENGTH_SHORT).show();
        } else {
            try {
                averageTempUrl = new URL("http://141.135.5.117:3500/temp/test");
            } catch (Exception e) {
                e.printStackTrace();
            }
            RetrieveAverageTemperatureTask retrieveAverageTemperatureTask = new RetrieveAverageTemperatureTask(SensorActivity.this, fromDate, toDate);
            retrieveAverageTemperatureTask.delegate = this;
            retrieveAverageTemperatureTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, averageTempUrl);
        }
    }

    private void goToSignUpActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void logOut() {
        editor.remove("token");
        editor.commit();
        goToSignUpActivity();
    }

    private void setOnClickListeners() {
        fromDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(0);
            }
        });

        toDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(1);
            }
        });

        getAverageTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAverageTemperature();
            }
        });

        from_dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                fromDateTextView = (TextView) findViewById(R.id.beginDateTextView);
                fromDateTextView.setText(day + "/" + (month + 1) + "/" + year);

                fromYear = year;
                fromMonth = month;
                fromDay = day;

                String[] stringValues = getStringValue(day, month);

                fromDate = year + "-" + stringValues[1] + "-" + stringValues[0] + " 00:00:00.000Z";
            }
        };

        to_dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                toDateTextView = (TextView) findViewById(R.id.endDateTextView);
                toDateTextView.setText(day + "/" + (month + 1) + "/" + year);

                toYear = year;
                toMonth = month;
                toDay = day;

                String[] stringValues = getStringValue(day, month);

                toDate = year + "-" + stringValues[1] + "-" + stringValues[0] + " 00:00:00.000Z";
            }
        };

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
    }

    @Override
    public void processFinish(String result) {}
}
