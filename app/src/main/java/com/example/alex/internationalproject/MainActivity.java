package com.example.alex.internationalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
    private RelativeLayout signUpLayout;
    private EditText signUpNameEditText;
    private EditText signUpPasswordEditText;
    private Button signUpButton;
    private TextView alreadySignedUpTextView;

    private RelativeLayout signInLayout;
    private EditText signInNameEditText;
    private EditText signInPasswordEditText;
    private Button signInButton;
    private CheckBox signInRememberCheckBox;
    private TextView notSignedUpYetTextView;
    private boolean rememberMe;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private URL url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = this.getSharedPreferences("TOKEN", MODE_PRIVATE);
        editor = sharedPref.edit();

        if(sharedPref.contains("token")) {
            goToNewActivity();
        } else {
            signUpLayout = (RelativeLayout) findViewById(R.id.signUpLayout);
            signUpNameEditText = (EditText) findViewById(R.id.signUpNameEditText);
            signUpPasswordEditText = (EditText) findViewById(R.id.signUpPasswordEditText);

            signUpButton = (Button) findViewById(R.id.signUpButton);

            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signUp();
                }
            });

            alreadySignedUpTextView = (TextView) findViewById(R.id.alreadySignedUpTextView);

            alreadySignedUpTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSignInLayout();
                }
            });

            signInLayout = (RelativeLayout) findViewById(R.id.signInLayout);
            signInNameEditText = (EditText) findViewById(R.id.signInNameEditText);
            signInPasswordEditText = (EditText) findViewById(R.id.signInPasswordEditText);
            signInRememberCheckBox = (CheckBox) findViewById(R.id.signInRememberCheckBox);

            signInButton = (Button) findViewById(R.id.signInButton);

            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });

            notSignedUpYetTextView = (TextView) findViewById(R.id.notSignedUpYetTextView);

            notSignedUpYetTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSignUpLayout();
                }
            });
        }
    }

    private void signUp() {
        String name = signUpNameEditText.getText().toString();
        String password = signUpPasswordEditText.getText().toString();
        
        if(name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.empty_field_error), Toast.LENGTH_SHORT).show();
        } else {
            try {
                url = new URL("http://141.135.5.117:3500/user/signup");
            } catch (Exception e) {
                e.printStackTrace();
            }
            UserAuthTask userAuthTask = new UserAuthTask(this, name, password, "signup");
            userAuthTask.delegate = this;
            userAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        }
    }

    private void signIn() {
        String name = signInNameEditText.getText().toString();
        String password = signInPasswordEditText.getText().toString();

        rememberMe = signInRememberCheckBox.isChecked();
        
        if(name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.empty_field_error), Toast.LENGTH_SHORT).show();
        } else {
            try {
                url = new URL("http://141.135.5.117:3500/user/authenticate");
            } catch(Exception e) {
                e.printStackTrace();
            }
            UserAuthTask userAuthTask = new UserAuthTask(this, name, password, "signin");
            userAuthTask.delegate = this;
            userAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        }
    }

    private void showSignInLayout() {
        signInLayout.setVisibility(View.VISIBLE);
        signUpLayout.setVisibility(View.INVISIBLE);
    }

    private void showSignUpLayout() {
        signInLayout.setVisibility(View.INVISIBLE);
        signUpLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void processFinish(JSONArray jsonArray) { }

    @Override
    public void processFinish(String result) {
        String resultString;
        String messageString = null;
        String token = null;
        try {
            JSONObject mainObject = new JSONObject(result);
            resultString = mainObject.getString("success");
            if(mainObject.has("msg")) {
                messageString = mainObject.getString("msg");
            } else if(mainObject.has("token")) {
                token = mainObject.getString("token");
            }

            if(resultString == "true" && token == null) {
                Toast.makeText(this, "Successfully signed up, you can now log in.", Toast.LENGTH_SHORT).show();
                showSignInLayout();
            } else if(resultString == "true" && token != null) {
                if(rememberMe) {
                    editor.putString("token", token);
                    editor.commit();
                    goToNewActivity();
                } else {
                    goToNewActivity();
                }
            } else
            {
                if(messageString != null) {
                    Toast.makeText(this, messageString, Toast.LENGTH_SHORT).show();
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void goToNewActivity() {
        Intent intent = new Intent(this, SensorActivity.class);
        String authenticated = "true";
        intent.putExtra("AUTHENTICATED", authenticated);
        startActivity(intent);
    }
}
