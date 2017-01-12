package com.unicam.dezio.theway;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.SyncStateContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText editTextUser;
    private EditText editTextPwd;
    private Button buttonLogin;
    private Button buttonRegister;

    private SharedPreferences pref;

    private CallbackManager cbm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences(Constants.TAG, Context.MODE_PRIVATE);
        if (pref.getBoolean(Constants.IS_LOGGED_IN, false)) {
            //L'utente è loggato, non ha bisogno di registrarsi e va alla pagina di Welcome
            goToWelcome();
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

            //set fb button things
            LoginButton lb = (LoginButton) this.findViewById(R.id.FBLogin);
            List<String> permissions = new ArrayList<>();
            permissions.add("email");
            lb.setReadPermissions(permissions);

            cbm = CallbackManager.Factory.create();
            lb.registerCallback(cbm, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.println(Log.DEBUG,"Token",loginResult.getAccessToken().getToken());
                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response){
                                    String email, name;
                                    try {
                                        email = object.getString("email");
                                        name = object.getString("name");
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putBoolean(Constants.IS_LOGGED_IN, true);
                                        editor.putString(Constants.EMAIL, email);
                                        editor.putString(Constants.USERNAME, name);
                                        editor.apply();
                                        goToWelcome();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }
                    );
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "name,email");
                    request.setParameters(parameters);
                    request.executeAsync();

                }

                @Override
                public void onCancel() {
                    Snackbar.make(findViewById(R.id.mainLayout),"Login aborted", Snackbar.LENGTH_LONG).show();
                    LoginManager.getInstance().logOut();
                }

                @Override
                public void onError(FacebookException error) {
                    Snackbar.make(findViewById(R.id.mainLayout),"Oops! Error occurred! :(", Snackbar.LENGTH_LONG).show();
                    LoginManager.getInstance().logOut();
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        cbm.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pref.getBoolean(Constants.IS_LOGGED_IN, false)) {
            //L'utente è loggato, non ha bisogno di registrarsi e va alla pagina di Welcome
            goToWelcome();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.buttonRegister:{
                goToRegister();
                break;
            }

            case R.id.buttonLogin: {
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
    }

    private void loginProcess(String username, String password) {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.LOGIN_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                Log.d(Constants.TAG, response.toString());
                ServerResponse resp = response.body();
                Snackbar.make(findViewById(R.id.mainLayout), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                if(resp.getResult().equals(Constants.SUCCESS)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN, true);
                    editor.putString(Constants.EMAIL, resp.getUser().getEmail());
                    editor.putString(Constants.USERNAME, resp.getUser().getUsername());
                    editor.apply();
                    goToWelcome();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.d(Constants.TAG,"failed");
                Log.d(Constants.TAG, t.getLocalizedMessage());
                Snackbar.make(findViewById(R.id.mainLayout), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }



    private void goToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void goToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }


}
