package com.unicam.dezio.theway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This activity shows a form in which the user prompt its data, and registers himself
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    //fields of views retrived from the layout
    private EditText editTextUser;
    private EditText editTextMail;
    private EditText editTextPwd;
    private EditText editTextRePwd;
    private Button buttonRegister;
    private Button buttonRegisterFB;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //checking the fatal case in which the user is logged
        pref = getSharedPreferences(Utility.TAG, Context.MODE_PRIVATE);
        if(pref.getBoolean(Utility.IS_LOGGED_IN, false)) {
            Utility.goToActivity(this, WelcomeActivity.class, true);
        } else {
            //setting the layout
            //Used to permit the single-thread retrieving of paths. Considering the small size of the area
            //and various tests, it doesn't impact on performance
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            editTextUser = (EditText) findViewById(R.id.editTextUser);
            editTextMail = (EditText) findViewById(R.id.editTextEmail);
            editTextPwd = (EditText) findViewById(R.id.editTextPwd);
            editTextRePwd = (EditText) findViewById(R.id.editTextRePwd);
            buttonRegister = (Button) findViewById(R.id.buttonRegister);
            buttonRegister.setOnClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        if (pref.getBoolean(Utility.IS_LOGGED_IN, false)) {
            Utility.goToActivity(this, WelcomeActivity.class, true);
        } else {
            super.onResume();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonRegister: {

                String username = editTextUser.getText().toString();
                String email = editTextMail.getText().toString();
                String pwd = editTextPwd.getText().toString();
                String repwd = editTextRePwd.getText().toString();
                if(!username.isEmpty() && !email.isEmpty() && !pwd.isEmpty()) {
                    if(pwd.equals(repwd)) {
                        if(Utility.registerProcess(username, email, pwd, this)) {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean(Utility.IS_LOGGED_IN, true);
                            editor.putString(Utility.EMAIL, email);
                            editor.putString(Utility.USERNAME, username);
                            editor.apply();
                            Utility.goToActivity(this, WelcomeActivity.class, true);
                        } else
                            Snackbar.make(findViewById(R.id.registerLayout),"Problem with the server, try again", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(findViewById(R.id.registerLayout),"Fields are empty", Snackbar.LENGTH_LONG).show();
                    }
                }
                else {
                    Snackbar.make(findViewById(R.id.registerLayout), "One of the fields is empty", Snackbar.LENGTH_LONG).show();
                }
                break;

            }


        }
    }


}
