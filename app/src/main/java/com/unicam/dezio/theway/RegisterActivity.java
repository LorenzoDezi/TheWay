package com.unicam.dezio.theway;

import android.content.Context;
import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextUser;
    private EditText editTextMail;
    private EditText editTextPwd;
    private EditText editTextRePwd;
    private Button buttonRegister;
    private Button buttonRegisterFB;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        pref = getSharedPreferences(Constants.TAG, Context.MODE_PRIVATE);
        if(pref.getBoolean(Constants.IS_LOGGED_IN, false)) {
            //L'utente è loggato, non ha bisogno di registrarsi e va alla pagina di Welcome
            goToWelcome();
        } else {
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
        if (pref.getBoolean(Constants.IS_LOGGED_IN, false)) {
            //L'utente è loggato, non ha bisogno di registrarsi e va alla pagina di Welcome
            goToWelcome();
        } else {
            super.onResume();
        }
    }


    private void goToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
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
                        registerProcess(username, email, pwd);
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

    private void registerProcess(String username, String mail, String pwd) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        final User user = new User();
        user.setUsername(username);
        user.setEmail(mail);
        user.setPassword(pwd);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.REGISTER_OPERATION);
        request.setUser(user);
        Log.d(Constants.TAG, request.toString());
        Call<ServerResponse> response = requestInterface.operation(request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse resp = response.body();
                Snackbar.make(findViewById(R.id.registerLayout), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                if(resp.getResult().equals(Constants.SUCCESS)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN, true);
                    editor.putString(Constants.EMAIL, user.getEmail());
                    editor.putString(Constants.USERNAME, user.getUsername());
                    editor.apply();
                    goToWelcome();
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                Log.d(Constants.TAG,"failed");
                Snackbar.make(findViewById(R.id.registerLayout), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
