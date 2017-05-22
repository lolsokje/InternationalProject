package com.example.alex.internationalproject;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SensorActivity extends AppCompatActivity implements AsyncResponse, DatePickerDialog.OnDateSetListener {
    boolean networkAvailable;

    Context context = this;

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

    int BACKGROUND_RED;
    int BACKGROUND_BLUE;
    int BACKGROUND_GREEN;

    Drawable BUTTON_RED;
    Drawable BUTTON_BLUE;
    Drawable BUTTON_GREEN;

    DatePickerDialog.OnDateSetListener from_dateListener, to_dateListener;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    final String TOKEN = "TOKEN";

    final int DELAY = 0;
    final int PERIOD = 5000;
    Timer timer;

    String token;

    @TargetApi(16)
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        sharedPref = this.getSharedPreferences(TOKEN, MODE_PRIVATE);
        editor = sharedPref.edit();

        timer = new Timer();

        BACKGROUND_RED = ContextCompat.getColor(context, R.color.red);
        BACKGROUND_BLUE = ContextCompat.getColor(context, R.color.blue);
        BACKGROUND_GREEN = ContextCompat.getColor(context, R.color.green);

        BUTTON_RED = ContextCompat.getDrawable(context, R.drawable.button_border_red);
        BUTTON_BLUE = ContextCompat.getDrawable(context, R.drawable.button_border_blue);
        BUTTON_GREEN = ContextCompat.getDrawable(context, R.drawable.button_border_green);

        Intent intent = getIntent();

        if(sharedPref.contains("tokenString") || intent.hasExtra(TOKEN)) {
            if(sharedPref.contains("tokenString")) {
                token = sharedPref.getString("tokenString", "tokenString");
            } else if(intent.hasExtra(TOKEN)) {
                token = intent.getStringExtra(TOKEN);
            }
            Calendar calendar = Calendar.getInstance();
            startYear = calendar.get(Calendar.YEAR);
            startMonth = calendar.get(Calendar.MONTH);
            startDay = calendar.get(Calendar.DAY_OF_MONTH);

            fromDate = "";
            toDate = "";

            logOutButton = (Button) findViewById(R.id.logOutButton);
            logOutButton.setBackground(BUTTON_GREEN);

            fromDateTextView = (TextView) findViewById(R.id.beginDateTextView);
            toDateTextView = (TextView) findViewById(R.id.endDateTextView);
            getAverageTempButton = (Button) findViewById(R.id.averageTempButton);
            getAverageTempButton.setBackground(BUTTON_GREEN);

            networkAvailable = isNetworkAvailable();

            setOnClickListeners();

            if (networkAvailable) {
                try {
                    temperatureUrl = new URL("http://141.135.5.117:3500/temp/fever");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                relLayout = (RelativeLayout) findViewById(R.id.activity_sensor);
                temperatureView = (TextView) findViewById(R.id.temperatureView);

                temperature = 0;
                temperatureView.setText(String.valueOf(temperature) + "°C");
                checkTemperature();

                timer.scheduleAtFixedRate(new MyTimerTask(token) {
                    @Override
                    public void run() {
                        getLastTemperature(token);
                    }
                }, DELAY, PERIOD);
            } else {
                buildAlertDialog(getResources().getString(R.string.network_error_title), getResources().getString(R.string.network_error_message));
            }
        } else {
            goToSignUpActivity();
        }
    }

    private void getLastTemperature(String token) {
        RetrieveTemperatureTask retrieveTemperatureTask = new RetrieveTemperatureTask(SensorActivity.this, token);
        retrieveTemperatureTask.delegate = this;
        retrieveTemperatureTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, temperatureUrl);
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

    @TargetApi(16)
    private void checkTemperature() {
        if(temperature < 38 == temperature > 36) {
            relLayout.setBackgroundColor(BACKGROUND_GREEN);
            getAverageTempButton.setBackground(BUTTON_GREEN);
            logOutButton.setBackground(BUTTON_GREEN);
        } else if(temperature >= 38) {
            relLayout.setBackgroundColor(BACKGROUND_RED);
            getAverageTempButton.setBackground(BUTTON_RED);
            logOutButton.setBackground(BUTTON_RED);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle("High temperature");
            mBuilder.setContentText("Your baby's temperature seems high. Please keep an eye on it");

            Intent notifIntent = new Intent(this, SensorActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(SensorActivity.class);
            stackBuilder.addNextIntent(notifIntent);
            PendingIntent notifPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(notifPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());

        } else if(temperature <= 36) {
            relLayout.setBackgroundColor(BACKGROUND_BLUE);
            getAverageTempButton.setBackground(BUTTON_BLUE);
            logOutButton.setBackground(BUTTON_BLUE);
        }
    }

    @Override
    public void processFinish(JSONObject json) {
        try {
            if(json != null) {
//                String temp;
//                Iterator<String> keys = json.keys();
//                while(keys.hasNext()) {
//                    String key = keys.next();
//                    String value = json.getString(key);
//                    JSONObject jObject = new JSONObject(value);
//                    temp = jObject.getString("value");
//                    temperature = Integer.parseInt(temp);
//                    temperatureView.setText(temp + "°C");
//                    checkTemperature();
//                }
                String temp;
                temp = json.getString("value");
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

    private void getTemperatureInRange() {
        if(fromDate == "" || toDate == "") {
//            Toast.makeText(this, "No dates selected", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, TemperatureGraph.class);
            startActivity(intent);
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
        editor.clear();
        editor.apply();
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
                getTemperatureInRange();
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

                fromDate = year + "-" + stringValues[1] + "-" + stringValues[0] + "T00:00:00.000Z";
                Log.v("DATE", fromDate);
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

                toDate = year + "-" + stringValues[1] + "-" + stringValues[0] + "T23:59:59.999Z";
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

    @Override
    public void processFinish(JSONArray json) {}
}
