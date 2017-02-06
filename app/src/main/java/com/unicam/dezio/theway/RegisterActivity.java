package com.unicam.dezio.theway;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
            goToWelcome();
        } else {

            //setting the layout
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
            //buttonRegisterFB.setOnClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        if (pref.getBoolean(Utility.IS_LOGGED_IN, false)) {
            goToWelcome();
        } else {
            super.onResume();
        }
    }


    /**
     * Sends the user to the welcome activity as a logged user ({@link WelcomeActivity})
     */
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

    /**
     * This method register a user, starting the communication with the server
     * @param username
     * @param mail
     * @param pwd as the password
     */
    private void registerProcess(String username, String mail, String pwd) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Utility.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        final User user = new User();
        user.setUsername(username);
        user.setEmail(mail);
        user.setPassword(pwd);
        ServerRequest request = new ServerRequest();
        request.setOperation(Utility.REGISTER_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);
        response.enqueue(new Callback<ServerResponse>() {

            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                Snackbar.make(findViewById(R.id.registerLayout), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                if(resp.getResult().equals(Utility.SUCCESS)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Utility.IS_LOGGED_IN, true);
                    editor.putString(Utility.EMAIL, user.getEmail());
                    editor.putString(Utility.USERNAME, user.getUsername());
                    editor.apply();
                    goToWelcome();
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Snackbar.make(findViewById(R.id.registerLayout), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();

            }
        });
    }
}
