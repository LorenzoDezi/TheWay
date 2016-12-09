package com.unicam.dezio.theway;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //DEBUG - OVVERO SI PROVA A VEDERE SE FUNZIONA IL PARSER
        mMap = googleMap;
        io.ticofab.androidgpxparser.parser.GPXParser parser = new io.ticofab.androidgpxparser.parser.GPXParser();

        InputStream in = null;
        Gpx gpx = null;
        try {
            in = getAssets().open("casaMarchei.gpx");
            gpx = parser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        if(gpx != null) {

            //Definiamo le opzioni per la nostra Polyline
            PolylineOptions options = new PolylineOptions();
            options.color(Color.RED);
            options.width(10);
            options.visible(true);

            List<Track> tracks = gpx.getTracks();
            //DEBUG
            Log.d(Constants.TAG, tracks.toString());
            for(Track track : tracks) {
                List<TrackSegment> trackSegments =  track.getTrackSegments();
                for(TrackSegment trackSegment : trackSegments) {
                    List<TrackPoint> trackPoints = trackSegment.getTrackPoints();
                    for(TrackPoint trackPoint : trackPoints) {
                        options.add(new LatLng(trackPoint.getLatitude(), trackPoint.getLongitude()));
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(trackPoint.getLatitude(), trackPoint.getLongitude())).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            }
            mMap.addPolyline(options);

        } else {

        }
    }
}
