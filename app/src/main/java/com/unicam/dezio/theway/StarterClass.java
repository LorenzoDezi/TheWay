package com.unicam.dezio.theway;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by Daniele on 03/01/2017.
 */
public class StarterClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}