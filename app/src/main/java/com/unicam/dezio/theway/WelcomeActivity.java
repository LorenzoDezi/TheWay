package com.unicam.dezio.theway;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;



/**
 * This activity is the logged-in screen. It's a simple welcome text with two buttons,
 * that will help the user to select the proper action.
 */
public class WelcomeActivity extends BaseActivity {

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Setting the layout
        setContentView(R.layout.activity_welcome);
        super.onCreate(savedInstanceState);
        TextView logOut = (TextView) findViewById(R.id.logOut);
        logOut.setOnClickListener(this);
        TextView startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        TextView findButton = (Button) findViewById(R.id.findButton);
        findButton.setOnClickListener(this);
        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);

        //checking if the user is logged
        pref = getSharedPreferences(Utility.TAG, Context.MODE_PRIVATE);
        if(pref.contains(Utility.USERNAME)) {
            String username = pref.getString(Utility.USERNAME, "");
            if (!username.isEmpty()) {
                welcomeText.setText("Welcome " + username);
            } else {
                Utility.goToActivity(this, MainActivity.class, true);
            }
        }
    }



    /**
     * This method clear the preferences(actually deleting saved user credentials) and goes back to
     * the main activity
     */
    private void logOut() {
        //if the user logged in with facebook
        if(AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        Utility.goToActivity(this, MainActivity.class, true);

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.logOut:
                logOut();
                break;
            case R.id.startButton:
                Utility.goToMapActivity(this, R.id.startButton);
                break;
            case R.id.findButton:
                Utility.goToMapActivity(this, R.id.findButton);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        logOut();
    }


}
