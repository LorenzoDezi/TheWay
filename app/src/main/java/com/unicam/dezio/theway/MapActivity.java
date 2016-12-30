package com.unicam.dezio.theway;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Camera;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.AddressConstants;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        View.OnClickListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //flag used to know the activity modality
    private boolean newPath;
    private boolean isCreating;

    //Date objects used to register time employed
    private Date start;
    private Date end;

    //View objects
    private ImageView searchIcon;
    private ImageView startIcon;
    private LinearLayout toolbarLayout;

    //Google-related properties
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private GoogleMap mMap;
    private FusedLocationProviderApi locationProvider;

    //the property in which the path will be stored and
    //the PolylineOptions object used to draw the line on
    //the map
    private Path currentPath;
    private PolylineOptions polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarTop);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            toolbar.setTitle("TheWay");
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        startIcon = (ImageView) findViewById(R.id.play_PNG);
        searchIcon = (ImageView) findViewById(R.id.search_PNG);
        startIcon.setOnClickListener(this);
        toolbarLayout = (LinearLayout) findViewById(R.id.toolbarLayout);
        if (intent.getExtras().getInt("choice") == R.id.startButton) {
            toolbarLayout.removeView(searchIcon);
            newPath = true;
        } else {
            toolbarLayout.removeView(startIcon);
            newPath = false;
        }
        isCreating = false;

        currentPath = new Path();

        polyline = new PolylineOptions();
        polyline.color(R.color.colorAccent);
        polyline.width(10);
        polyline.visible(true);
        locationProvider = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).
                addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        locationRequest = new LocationRequest();
        //DEBUG
        locationRequest.setSmallestDisplacement(20);
        locationRequest.setInterval(1000 * 1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //DEBUG TODO: Continue with map adjustements (ZOOM, e varie)


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {

    }


    @Override
    public void onLocationChanged(Location location) {

        //DEBUG
        //Log.d(Constants.TAG, "isUpdating");
        if(isCreating) {
            //DEBUG
            //Log.d(Constants.TAG, "isUpdating");
            currentPath.addCoordinate(location);
            polyline.add(new LatLng(location.getLatitude(), location.getLongitude()));
            mMap.clear();
            mMap.addPolyline(polyline);
            //DEBUG
            Toast.makeText(this.getApplicationContext(), "location :" + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(isCreating) {
            requestLocationUpdates();
            //DEBUG
            //Log.d(Constants.TAG, "isCreating");
        }
    }

    private void requestLocationUpdates() {
        //DEBUG
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationProvider.requestLocationUpdates(googleApiClient,
                locationRequest,  this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationProvider.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //DEBUG
        Log.d(Constants.TAG, "Connection is failed");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_PNG: {
                if(!isCreating) {
                    isCreating = true;
                    //DEBUG
                    //Log.d(Constants.TAG, "isCreating");
                    googleApiClient.connect();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startIcon.setImageDrawable(getDrawable(R.drawable.stop));
                    } else {
                        startIcon.setImageDrawable(getResources().getDrawable(R.drawable.stop));
                    }
                    start = new Date();
                } else {
                    isCreating = false;
                    googleApiClient.disconnect();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startIcon.setImageDrawable(getDrawable(R.drawable.play));
                    } else {
                        startIcon.setImageDrawable(getResources().getDrawable(R.drawable.play));
                    }
                    end = new Date();
                    currentPath.setTime(start, end);
                    Intent intent = new Intent(this.getApplicationContext(), SaveActivity.class);
                    intent.putExtra("path", new Gson().toJson(currentPath));
                    startActivity(intent);
                }
                break;
            }

        }
    }

}
