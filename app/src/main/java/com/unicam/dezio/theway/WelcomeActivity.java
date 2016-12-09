package com.unicam.dezio.theway;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView logOut;
    private TextView welcomeText;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        logOut = (TextView) findViewById(R.id.logOut);
        logOut.setOnClickListener(this);
        welcomeText = (TextView) findViewById(R.id.welcomeText);
        pref = getSharedPreferences(Constants.TAG, Context.MODE_PRIVATE);
        if(pref.contains(Constants.USERNAME)) {
            String username = pref.getString(Constants.USERNAME, "");
            //DEBUG
            Log.d(Constants.TAG, username);
            if (!username.isEmpty()) {
                welcomeText.setText("Welcome " + username);
            } else {
                goToMain();
            }
        }
    }

    /**
     * This method clear the preferences(actually deleting saved user credentials) and goes back to
     * the main activity
     */
    private void logOut() {

        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        goToMain();

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logOut: {
                logOut();
                break;
            }
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        logOut();
    }
}
