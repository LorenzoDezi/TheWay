package com.unicam.dezio.theway;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

public class NetworkChangeReceiver extends BroadcastReceiver {
    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
           ObservableObject.getInstance().updateValue(intent);
    }
}
