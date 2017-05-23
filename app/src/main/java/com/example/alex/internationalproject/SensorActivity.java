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
import android.hardware.Sensor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SensorActivity extends AppCompatActivity implements AsyncResponse, DatePickerDialog.OnDateSetListener {
    boolean networkAvailable;

    final String BASE = "http://141.135.5.117:3500/";
    final String FEVER = "temp/fever";
    final String REGISTER = "device/register";
    final String LIST = "device/list";
    final String SERIAL = "temp/serial"; // Get all temps for certain serial

    Context context = this;

    int temperature;

    URL temperatureUrl;
    URL averageTempUrl;
    URL addDeviceURL;
    URL getDeviceListURL;

    Button logOutButton;
    Button addDeviceButton;

    RelativeLayout relLayout;
    TextView temperatureView;

    TextView fromDateTextView;
    TextView toDateTextView;
    Button getAverageTempButton;

    TextView babyNameTextView;

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

    Spinner deviceSpinner;

    List<String> spinnerList;
    List<String> deviceIDList;
    ArrayAdapter<String> adapter;

    int selectedIndex;

    @TargetApi(16)
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        selectedIndex = 0;

        sharedPref = this.getSharedPreferences(TOKEN, MODE_PRIVATE);
        editor = sharedPref.edit();

        timer = new Timer();

        deviceSpinner = (Spinner)findViewById(R.id.deviceSpinner);
        spinnerList = new ArrayList<>();
        deviceIDList = new ArrayList<>();

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

            babyNameTextView = (TextView) findViewById(R.id.babyNameTextView);

            logOutButton = (Button) findViewById(R.id.logOutButton);
            logOutButton.setBackground(BUTTON_GREEN);

            addDeviceButton = (Button) findViewById(R.id.addDeviceButton);
            addDeviceButton.setBackground(BUTTON_GREEN);

            fromDateTextView = (TextView) findViewById(R.id.beginDateTextView);
            toDateTextView = (TextView) findViewById(R.id.endDateTextView);
            getAverageTempButton = (Button) findViewById(R.id.averageTempButton);
            getAverageTempButton.setBackground(BUTTON_GREEN);

            networkAvailable = isNetworkAvailable();

            setOnClickListeners();

            if (networkAvailable) {
                try {
                    temperatureUrl = new URL(BASE + SERIAL);
                    getDeviceListURL = new URL(BASE + LIST);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                getDeviceList();

                relLayout = (RelativeLayout) findViewById(R.id.activity_sensor);
                temperatureView = (TextView) findViewById(R.id.temperatureView);

                temperature = 0;
                temperatureView.setText(String.valueOf(temperature) + "°C");
                checkTemperature();

                timer.scheduleAtFixedRate(new MyTimerTask(token) {
                    @Override
                    public void run() {
                        if(deviceIDList.size() > 0) {
                            getLastTemperature(token);
                        }
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
        String serial = deviceIDList.get(selectedIndex).toString();
        RetrieveTemperatureTask retrieveTemperatureTask = new RetrieveTemperatureTask(SensorActivity.this, token, serial);
        retrieveTemperatureTask.delegate = this;
        retrieveTemperatureTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, temperatureUrl);
    }

    private void getDeviceList() {
        GetDeviceListTask getDeviceList = new GetDeviceListTask(SensorActivity.this, token);
        getDeviceList.delegate = this;
        getDeviceList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getDeviceListURL);
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

        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(SensorActivity.this).create();
                alertDialog.setTitle("Add device");

                LinearLayout layout = new LinearLayout(SensorActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText nameInput = new EditText(context);
                nameInput.setHint("Name");
                layout.addView(nameInput);

                final EditText deviceIdInput = new EditText(context);
                deviceIdInput.setHint("Serial Number");
                layout.addView(deviceIdInput);

                alertDialog.setView(layout);

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name;
                        String deviceID;
                        
                        name = nameInput.getText().toString();
                        deviceID = deviceIdInput.getText().toString();

                        try {
                            addDeviceURL = new URL(BASE + REGISTER);
                        } catch(MalformedURLException e) {
                            e.printStackTrace();
                        }

                        AddDeviceTask addDeviceTask = new AddDeviceTask(SensorActivity.this, name, deviceID, token);
                        addDeviceTask.delegate = SensorActivity.this;
                        addDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, addDeviceURL);

                        dialog.dismiss();
                    }
                });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alertDialog.show();
            }
        });

        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                babyNameTextView.setText(adapter.getItem(position).toString());
                selectedIndex = position;
                getLastTemperature(token);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void processFinish(String result) {}

    @Override
    public void addDeviceFinish(JSONObject json) {
        String resultMessage;
        try {
            resultMessage = json.getString("msg");
            Toast.makeText(context, resultMessage, Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            e.printStackTrace();
        }

        getDeviceList();
    }

    @Override
    public void getDeviceListFinish(JSONArray json) {
        spinnerList.clear();
        String bName;
        String dId;
        try {
            for (int i = 0; i < json.length(); i++) {
                JSONObject row = json.getJSONObject(i);
                bName = row.getString("ChildName");
                dId = row.getString("SerialNumber");
                spinnerList.add(bName);
                deviceIDList.add(dId);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<>(
            SensorActivity.this,
            R.layout.spinner_item,
            spinnerList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);;
        deviceSpinner.setAdapter(adapter);

        babyNameTextView.setText(adapter.getItem(0));
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
            addDeviceButton.setBackground(BUTTON_GREEN);
        } else if(temperature >= 38) {
            relLayout.setBackgroundColor(BACKGROUND_RED);
            getAverageTempButton.setBackground(BUTTON_RED);
            logOutButton.setBackground(BUTTON_RED);
            addDeviceButton.setBackground(BUTTON_RED);
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
            addDeviceButton.setBackground(BUTTON_BLUE);
        }
    }

    @Override
    public void processFinish(JSONArray json) {
        try {
            if(json != null) {
                int index = (json.length() - 1);
                JSONObject jObject = json.getJSONObject(index);
                String temp = jObject.getString("value");
                temperature = Integer.parseInt(temp);
                temperatureView.setText(temp + "°C");
                checkTemperature();
            } else {
                buildAlertDialog(getResources().getString(R.string.server_error_title), getResources().getString(R.string.server_error_message));
            }
        } catch(Exception e) {
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
}