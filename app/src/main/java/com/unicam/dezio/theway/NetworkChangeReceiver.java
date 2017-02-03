package com.unicam.dezio.theway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * this receiver is triggered when there is a change of connectivity or
 * the gps is disabled. {@link BaseActivity} registers a {@link NetworkChangeReceiver}
 * to this task
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
           ObservableObject.getInstance().updateValue();
    }
}
