package com.unicam.dezio.theway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentHostCallback;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.gson.Gson;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public abstract class BaseActivity extends AppCompatActivity  implements
        Observer, View.OnClickListener {

    //The receiver used to intercept connectivity/gps change
    protected BroadcastReceiver receiver;
    protected ConnectivityManager connectivityManager;
    protected LocationManager locationManager;
    protected ImageView connectivity;
    protected ImageView gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(this instanceof MapActivity) {

        }

        connectivity = (ImageView) findViewById(R.id.connect_PNG);
        connectivity.setOnClickListener(this);
        gps = (ImageView) findViewById(R.id.GPS_PNG);
        gps.setOnClickListener(this);

        //Preparing the receiver and setting the observable object
        connectivityManager = (ConnectivityManager)this.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        ObservableObject.getInstance().addObserver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.location.PROVIDERS_CHANGED");
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        ObservableObject.getInstance().deleteObserver(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        //DEBUG
        Log.d(Constants.TAG, "onClick called");
        switch (view.getId()) {

            case R.id.connect_PNG: {
                //DEBUG
                Log.d(Constants.TAG, "connect_PNG is there");
                NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
                if(ni != null && ni.isConnectedOrConnecting()) {
                    Toast.makeText(this.getApplicationContext(), "Connection ok!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this.getApplicationContext(), "No connection",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.GPS_PNG: {
                //C'è il gps
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    Toast.makeText(this.getApplicationContext(), "GPS Ok!",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this.getApplicationContext(), "No GPS",
                            Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void update(Observable observable, Object o) {

        final ConnectivityManager connectivityManager = (ConnectivityManager)this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        //C'è connessione
        if (ni != null && ni.isConnectedOrConnecting()) {
            connectivity.setImageResource(R.drawable.ic_connectivity_true);
        }
        //Non c'è connessione
        else {
            connectivity.setImageResource(R.drawable.ic_connectivity_false);
        }

        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            gps.setImageResource(R.drawable.ic_gps_true);
        else
            gps.setImageResource(R.drawable.ic_gps_false);
    }





}
