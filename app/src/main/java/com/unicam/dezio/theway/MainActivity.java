package com.unicam.dezio.theway;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is the main activity, where the user can log in or register himself
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    //All fields used to retrieve views from the layout
    private EditText editTextUser;
    private EditText editTextPwd;
    private Button buttonLogin;
    private Button buttonRegister;
    private SharedPreferences pref;
    private Activity activityInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Checking if the user is already logged
        pref = getSharedPreferences(Utility.TAG, Context.MODE_PRIVATE);
        if (pref.getBoolean(Utility.IS_LOGGED_IN, false)) {
            Utility.goToActivity(this, WelcomeActivity.class, false);
        } else {
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            editTextUser = (EditText) findViewById(R.id.editTextUser);
            editTextPwd = (EditText) findViewById(R.id.editTextPwd);
            buttonLogin = (Button) findViewById(R.id.buttonLogin);
            buttonRegister = (Button) findViewById(R.id.buttonRegister);
            buttonLogin.setOnClickListener(this);
            buttonRegister.setOnClickListener(this);
            activityInstance = this;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pref.getBoolean(Utility.IS_LOGGED_IN, false)) {
            Utility.goToActivity(this, WelcomeActivity.class, false);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.buttonRegister:
                Utility.goToActivity(this, RegisterActivity.class, false);
                break;

            case R.id.buttonLogin:
                String username = editTextUser.getText().toString();
                String password = editTextPwd.getText().toString();
                if (!username.isEmpty() && !password.isEmpty()) {
                    loginProcess(username, password);
                } else {
                    Snackbar.make(findViewById(R.id.mainLayout), "Fields are empty!", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * It starts the communication with the server to check if the username/password inserted in
     * the form are valid, and in that case logs the user, otherwise it notifies the error
     * @param username
     * @param password
     */
    private void loginProcess(String username, String password) {

        //Preparing the server communication
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Utility.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Utility.LOGIN_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);
        response.enqueue(new Callback<ServerResponse>() {

            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                Log.d(Utility.TAG, response.toString());
                ServerResponse resp = response.body();
                Snackbar.make(findViewById(R.id.mainLayout), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                if(resp.getResult().equals(Utility.SUCCESS)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Utility.IS_LOGGED_IN, true);
                    editor.putString(Utility.EMAIL, resp.getUser().getEmail());
                    editor.putString(Utility.USERNAME, resp.getUser().getUsername());
                    editor.apply();
                    Utility.goToActivity(activityInstance, WelcomeActivity.class, true);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Snackbar.make(findViewById(R.id.mainLayout), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }



}
