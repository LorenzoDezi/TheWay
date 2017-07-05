package com.unicam.dezio.theway;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * This class extends Application in order to initialize the SDK
 * before executing any other operations
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