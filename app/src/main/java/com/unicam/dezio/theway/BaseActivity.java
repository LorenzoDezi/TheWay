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

/**
 * The BaseActivity class is a super class extended by MapActivity and SaveActivity, comprending
 * some shared functions, like checking GPS or connectivity
 */
public abstract class BaseActivity extends AppCompatActivity  implements
        Observer, View.OnClickListener {

    /** The receiver used to intercept connectivity/gps change **/
    protected BroadcastReceiver receiver;

    /** connectivityManager used to check connection **/
    protected ConnectivityManager connectivityManager;

    /** locationManager used to check GPS **/
    protected LocationManager locationManager;

    /** connectivity ImageView, used to change the icon of connectivity **/
    protected ImageView connectivity;

    /** gps ImageView, used to change the icon of gps **/
    protected ImageView gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Initializing various fields
        connectivity = (ImageView) findViewById(R.id.connect_PNG);
        connectivity.setOnClickListener(this);
        gps = (ImageView) findViewById(R.id.GPS_PNG);
        gps.setOnClickListener(this);
        connectivityManager = (ConnectivityManager)this.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        //Preparing the receiver and setting the observable object
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

        switch (view.getId()) {

            case R.id.connect_PNG: {
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
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    Toast.makeText(this.getApplicationContext(), "GPS Ok!",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this.getApplicationContext(), "No GPS",
                            Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * check if the connection is enabled
     * @return a boolean value representing the connection
     */
    protected boolean isConnected() {
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     *  check if the gps is enabled
     *  @return a boolean value representing the gps
     * **/
    protected boolean isGPSEnabled() {

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    @Override
    /**
     * this method updates the activity every time the networkChangeReceiver updates the observable
     * object, changing its state and causing this method to be called. Image resources
     * corresponding to connectivity and gps are changed
     */
    public void update(Observable observable, Object o) {

        //C'è connessione
        if (this.isConnected()) {
            connectivity.setImageResource(R.drawable.ic_connectivity_true);
        }
        //Non c'è connessione
        else {
            connectivity.setImageResource(R.drawable.ic_connectivity_false);
        }

        if(isGPSEnabled())
            gps.setImageResource(R.drawable.ic_gps_true);
        else {
            gps.setImageResource(R.drawable.ic_gps_false);
            if(this instanceof MapActivity) {
                Toast.makeText(this.getApplicationContext(), "Your GPS is missing!", Toast.LENGTH_SHORT).show();
            }

        }
    }





}
