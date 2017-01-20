package com.unicam.dezio.theway;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

public class WelcomeActivity extends BaseActivity {

    private TextView logOut;
    private TextView welcomeText;
    private Button startButton;
    private Button findButton;
    private SharedPreferences pref;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_welcome);
        super.onCreate(savedInstanceState);
        Intent comingIntent = getIntent();
        if(comingIntent.getExtras() != null)
            if(comingIntent.getExtras().getBoolean("LocationFail", false))
                Snackbar.make(findViewById(R.id.welcomeLayout),"I need your location to create new routes!", Snackbar.LENGTH_LONG).show();
        logOut = (TextView) findViewById(R.id.logOut);
        logOut.setOnClickListener(this);
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        findButton = (Button) findViewById(R.id.findButton);
        findButton.setOnClickListener(this);
        welcomeText = (TextView) findViewById(R.id.welcomeText);
        pref = getSharedPreferences(Constants.TAG, Context.MODE_PRIVATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.location.PROVIDERS_CHANGED");
        ObservableObject.getInstance().addObserver(this);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);
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
        super.onClick(view);
        switch (view.getId()) {
            case R.id.logOut:
                logOut();
                break;
            case R.id.startButton:
                goToMap(R.id.startButton);
                break;
            case R.id.findButton:
                goToMap(R.id.findButton);
                break;
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMap(int choice) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("choice", choice);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        logOut();
    }


}
