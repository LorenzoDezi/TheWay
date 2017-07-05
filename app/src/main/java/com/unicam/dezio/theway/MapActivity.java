package com.unicam.dezio.theway;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.maps.android.PolyUtil;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This is the map activity, where the user is actually tracked and the main tasks of the application
 * are performed. This activity has different states, each of them indicating a particular mode of
 * operation. You can create a path, ic_search_gps for a path online/offline in a particular area, and
 * actually travel a path
 */
public class MapActivity extends BaseActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * flag used to know the activity modality
     **/
    private int state;

    //Date objects used to register time employed to perform a path
    private Date start;
    private Date end;

    //View objects from the layout
    private ImageView searchIcon;
    private ImageView searchGPSIcon;
    private ImageView startIcon;
    private LinearLayout toolbarLayout;

    //Google-related fields
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private GoogleMap mMap;
    private FusedLocationProviderApi locationProvider;

    //the property in which the created/travelled path will be stored,
    //the PolylineOptions/MarkerOptions objects used to draw on the map
    private Path currentPath;
    private PolylineOptions[] searchPolylines;
    private PolylineOptions newPolyline;
    private MarkerOptions userMarker;
    private MarkerOptions pathMarker;

    //the list of paths founds during searching
    private List<Path> pathsFound;

    //Various fields
    private SharedPreferences pref;
    private Context context;

    //user's location
    private Location userLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Used to permit the single-thread retrieving of paths. Considering the small size of the area
        //and various tests, it doesn't impact on performance
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Handling the fatal case in which the user is not logged
        pref = getSharedPreferences(Utility.TAG, Context.MODE_PRIVATE);
        if (!pref.getBoolean(Utility.IS_LOGGED_IN, false)) {
            //back to the login
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //Adding the map fragment
        MapFragment fragment = new MapFragment();
        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.map, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        //set the layout and retrieving all necessary objects
        setContentView(R.layout.activity_map);
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
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
        fragment.getMapAsync(this);
        startIcon = (ImageView) findViewById(R.id.play_PNG);
        searchIcon = (ImageView) findViewById(R.id.search_PNG);
        searchGPSIcon = (ImageView) findViewById(R.id.search_gps_PNG);
        searchIcon.setOnClickListener(this);
        searchGPSIcon.setOnClickListener(this);
        startIcon.setOnClickListener(this);
        toolbarLayout = (LinearLayout) findViewById(R.id.toolbarLayout);

        //Set the map
        Intent intent = getIntent();
        if (intent.getExtras().getInt("choice") == R.id.startButton) {
            toolbarLayout.removeView(searchIcon);
            toolbarLayout.removeView(searchGPSIcon);
        } else {
            toolbarLayout.removeView(startIcon);
        }
        state = Utility.IS_WAITING;
        currentPath = new Path();
        pathsFound = new ArrayList<>();
        searchPolylines = new PolylineOptions[0];

        //Using LocationServices to use GPS functionalities
        locationProvider = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).
                addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(Utility.MIN_DISTANCE_CHANGE_FOR_UPDATES);
        locationRequest.setInterval(Utility.MIN_TIME_BW_UPDATES);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Setting the map
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng clickCoords) {
                int i = 0;
                for (PolylineOptions polyline : searchPolylines) {
                    if (PolyUtil.isLocationOnPath(clickCoords, polyline.getPoints(), true, 100)) {
                        currentPath = pathsFound.get(i);
                        showPath(pathsFound.get(i));
                    }
                    i++;
                }
            }
        });
        userMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_user));
        pathMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.path_marker));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {

        if (requestCode == Utility.MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this.getApplicationContext(), "Permission granted!", Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this.getApplicationContext(), "Permission denied! You can't do nothing!", Toast.LENGTH_SHORT).show();

            }

        }

    }


    @Override
    public void onLocationChanged(Location location) {

        userLocation = location;
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 15,
                0, 0));
        mMap.animateCamera(update);

        if (state == Utility.IS_CREATING) {

            currentPath.addCoordinate(location);
            newPolyline.add(new LatLng(location.getLatitude(), location.getLongitude()));
            mMap.clear();
            mMap.addPolyline(newPolyline);
            mMap.addMarker(userMarker.position(new LatLng(location.getLatitude(),
                    location.getLongitude())).title("You"));

        } else if (state == Utility.IS_SEARCHING) {

            mMap.clear();
            pathsFound.clear();
            mMap.addMarker(userMarker.position(new LatLng(location.getLatitude(),
                    location.getLongitude())).title("You"));
            Area coveredArea = new Area();
            coveredArea.setCenter(location);
            coveredArea.setRadius(1000);
            searchForPaths(coveredArea);


        } else if (state == Utility.IS_TRAVELLING) {

            mMap.clear();
            //Let's remove ic_search_gps icons
            while (toolbarLayout.getChildCount() > 2)
                toolbarLayout.removeViewAt(toolbarLayout.getChildCount() - 1);
            mMap.addMarker(userMarker.position(new LatLng(location.getLatitude(), location.getLongitude())));
            PolylineOptions polyline = new PolylineOptions();
            polyline.color(R.color.colorAccent);
            polyline.width(10);
            ArrayList<Location> currentCoordinates = currentPath.getCoordinates();
            for (Location coordinate : currentCoordinates) {
                LatLng currentLatLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                polyline.add(currentLatLng);
            }
            mMap.addPolyline(polyline);
            if (!userInside(currentPath, location)) {
                Snackbar.make(findViewById(R.id.mapLayout), "You're off the path!", Snackbar.LENGTH_LONG).show();
            } else {
                //if the user is at the end of the path
                if ((currentPath.getEnd().distanceTo(userLocation))
                        <= Utility.MAX_DISTANCE_OFF) {
                    Intent intent = new Intent(this, WayDialogActivity.class);
                    intent.putExtra("EndOfPath", true);
                    startActivityForResult(intent, Utility.PATH_INFO);
                }
            }
        }
    }


    /**
     * This function ic_search_gps for paths inside the coveredArea param,
     * and populates pathsFound and searchPolylines, printing them on map
     *
     * @param coveredArea
     */
    private void searchForPaths(Area coveredArea) {

        pathsFound.clear();
        if (super.isConnected()) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .registerTypeAdapter(Time.class, new JsonDeserializer<Time>() {

                                private static final String TIME_FORMAT = "HH:mm:ss";

                                @Override
                                public Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                                    try {

                                        String s = json.getAsString();
                                        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.US);
                                        sdf.parse(s);
                                        long ms = sdf.parse(s).getTime();
                                        Time t = new Time(ms);
                                        return t;
                                    } catch (ParseException e) {
                                    }
                                    throw new JsonParseException("Unparseable time: \"" + json.getAsString()
                                            + "\". Supported formats: " + TIME_FORMAT);
                                }
                            }
                    )
                    .create();
            Retrofit retrofit2 = new Retrofit.Builder().baseUrl(Utility.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            RequestInterface requestInterface2 = retrofit2.create(RequestInterface.class);
            //Preparing the request
            ServerRequest request = new ServerRequest();
            request.setOperation(Utility.REQUEST_OPERATION);
            request.setArea(coveredArea);

            //Calling the server and processing the response
            Call<ServerResponse> call = requestInterface2.operation(request);
            try {
                Response response = call.execute();
                if (response.isSuccessful()) {
                    ServerResponse respBody = (ServerResponse) response.body();
                    if (respBody.getResult().equals(Utility.SUCCESS)) {
                        Path[] paths = respBody.getPaths();
                        Retrofit retrofit = new Retrofit.Builder().baseUrl(Utility.BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
                        for (int i = 0; i < paths.length; i++) {

                            Path currentPath = paths[i];
                            pathsFound.add(i, currentPath);
                            Call<ResponseBody> innerCall = requestInterface.downloadGPX("theWayServer/GPXs/" + currentPath.getGpxName());
                            try {
                                Response currentResp = innerCall.execute();
                                if (currentResp.isSuccessful()) {

                                    File currentGpx = createFile((ResponseBody) currentResp.body(), currentPath.getGpxName());
                                    try {

                                        currentPath.setCoordinates(currentGpx);
                                        currentPath.setStart();

                                    } catch (Exception ex) {
                                        throw ex;
                                    }
                                    if (!currentGpx.delete())
                                        throw new IOException("");
                                } else {
                                    throw new IOException();
                                }

                            } catch (IOException | NullPointerException | ParserConfigurationException
                                    |SAXException ex) {
                                Toast.makeText(context, "Can't download the gpx file, caused by"
                                        + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        throw new IOException(respBody.getMessage());
                    }
                } else {
                    throw new IOException("Error reaching the server, message: "
                            + response.message() + ", code: " + response.code()
                            );
                }
            } catch (IOException exception) {
                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        //Retrieving offline paths
        File dir;
        ArrayList<File> gpxs = new ArrayList<File>();
        try

        {
            if (Utility.isExternalStorageWritable()) {
                dir = Utility.getFileStorageDir(context.getExternalFilesDir(null), "GPXs");
                gpxs.addAll(Arrays.asList(dir.listFiles()));
            }
            dir = Utility.getFileStorageDir(context.getFilesDir(), "GPXs");
            gpxs.addAll(Arrays.asList(dir.listFiles()));
        }

        catch(
                IOException ex
                )

        {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        for(
                File gpx
                :gpxs)

        {
            if (gpx.getName().endsWith(".gpx")) {
                Path path = new Path();
                try {
                    path.setCoordinates(gpx);
                    if (!pathsFound.contains(path))
                        pathsFound.add(path);
                } catch (ParserConfigurationException | IOException |
                        SAXException ex) {
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        if(pathsFound.size()!=0)

        {
            mMap.clear();
            searchPolylines = new PolylineOptions[pathsFound.size()];
            int index = 0;
            for (Path path :
                    pathsFound) {
                int innerIndex = 0;
                LatLng currentLatLng;
                searchPolylines[index] = new PolylineOptions();
                PolylineOptions currentPolyline = searchPolylines[index];
                currentPolyline.color(R.color.colorAccent);
                currentPolyline.width(10);
                for (Location coordinate : path.getCoordinates()) {
                    currentLatLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                    if (innerIndex == 0)
                        mMap.addMarker(pathMarker.position(currentLatLng));
                    currentPolyline.add(currentLatLng);
                    innerIndex++;
                }
                mMap.addPolyline(currentPolyline);
                index++;
            }

        }

        else

        {
            Toast.makeText(context, "No paths found!", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (state != Utility.IS_WAITING) {
            requestLocationUpdates();
        }

    }


    /**
     * Creates a file and returns its pointer, used both
     * in offline and online saving
     *
     * @param body
     * @param filename
     */
    private File createFile(ResponseBody body, String filename) {

        OutputStream writer;
        File GPXFile;

        try {

            File mainDirectory;
            //Priority is saving to the external storage
            if (Utility.isExternalStorageWritable()) {
                mainDirectory = Utility.getFileStorageDir(context.getExternalFilesDir(null), "GPXs");
            } else {
                mainDirectory = Utility.getFileStorageDir(context.getFilesDir(), "GPXs");
            }
            GPXFile = new File(mainDirectory, filename);
            //Creating phisically the gpx file
            if (GPXFile.createNewFile()) {
                writer = new FileOutputStream(GPXFile);
                writer.write(body.bytes());
                writer.close();
            }
            return GPXFile;


        } catch (IOException ex) {

            Snackbar.make(findViewById(R.id.mapLayout), ex.getMessage(), Snackbar.LENGTH_LONG).show();
            return null;
        }
    }


    /**
     * It request location updates using the Google client, but first check if the permission are
     * enabled by the user, otherwise it requests them
     **/
    private void requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //The explanation is needed because the user block in some way the permission
                //for location for this app
                Toast.makeText(this.getApplicationContext(), "Location permission needed!", Toast.LENGTH_SHORT).show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Utility.MY_PERMISSIONS_REQUEST_LOCATION);
            }

        }
        locationProvider.requestLocationUpdates(googleApiClient,
                locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

        locationProvider.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this.getApplicationContext(), "Connection to google maps failed!", Toast.LENGTH_SHORT).show();

    }

    /**
     * It start the {@link WayDialogActivity} showing informations about the path selected.
     * the result will be handled by this activity, and if the user has started the path, than
     * the state of the activity is changed
     *
     * @param path
     */
    public void showPath(Path path) {

        Intent intent = new Intent(this, WayDialogActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtra("Path", path);
        this.startActivityForResult(intent, Utility.PATH_INFO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PATH_INFO)
            if (resultCode == RESULT_OK) {
                currentPath = data.getParcelableExtra("Path");
                toolbarLayout.removeView(searchIcon);
                toolbarLayout.removeView(searchGPSIcon);
                state = Utility.IS_TRAVELLING;
            } else if (resultCode == Utility.DELETED) {
                requestLocationUpdates();
            } else if (resultCode == Utility.FINISHED) {
                Utility.goToActivity(this, WelcomeActivity.class, true);
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (state == Utility.IS_TRAVELLING) {
            mMap.clear();
            PolylineOptions currentPolyline = new PolylineOptions();
            for (Location coordinate : currentPath.getCoordinates()) {
                LatLng currentLatLng = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                currentPolyline.add(currentLatLng);
            }
            mMap.addPolyline(currentPolyline);
            if (userLocation != null)
                mMap.addMarker(userMarker.position(new LatLng(userLocation.getLatitude(), userLocation.getLongitude())));
            requestLocationUpdates();

        }
    }

    @Override
    protected void onDestroy() {
        locationProvider.removeLocationUpdates(googleApiClient, this);
        googleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        switch (state) {

            case Utility.IS_WAITING:
                finish();
                break;

            case Utility.IS_SEARCHING:
                toolbarLayout.addView(searchGPSIcon);
                mMap.clear();
                pathsFound.clear();
                state = Utility.IS_WAITING;
                requestLocationUpdates();
                break;

            case Utility.IS_CREATING:
                startIcon.setImageResource(R.drawable.play);
                mMap.clear();
                currentPath = new Path();
                state = Utility.IS_WAITING;
                requestLocationUpdates();
                break;

            case Utility.IS_TRAVELLING:
                toolbarLayout.addView(searchIcon);
                toolbarLayout.addView(searchGPSIcon);
                mMap.clear();
                state = Utility.IS_WAITING;
                currentPath = new Path();
                requestLocationUpdates();
        }

    }

    @Override
    public void onClick(View view) {

        super.onClick(view);
        int viewId = view.getId();
        switch (viewId) {

            case R.id.play_PNG:
                if (state == Utility.IS_WAITING) {
                    //Let's start creating
                    //There is no GPS, the function returns and a error message appears
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(this.getApplicationContext(), "You need your GPS enabled to start a path!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    state = Utility.IS_CREATING;
                    newPolyline = new PolylineOptions();
                    newPolyline.color(R.color.colorAccent);
                    newPolyline.width(10);
                    startIcon.setImageResource(R.drawable.stop);
                    start = new Date();
                    requestLocationUpdates();

                } else {

                    //We stop creating and we pass to the path-definition activity
                    if (currentPath.getCoordinates().size() < 2) {
                        Toast.makeText(this.getApplicationContext(), "Your path is too short!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    state = Utility.IS_WAITING;
                    startIcon.setImageResource(R.drawable.play);
                    end = new Date();
                    currentPath.setTime(start, end);
                    Intent intent = new Intent(this.getApplicationContext(), SaveActivity.class);
                    intent.putExtra("path", currentPath);
                    startActivity(intent);
                }
                break;

            case R.id.search_PNG:
                if (state == Utility.IS_WAITING) {
                    //Let's start searching
                    //There is no GPS, the function returns and a error message appears
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(this.getApplicationContext(), "You need your GPS enabled to ic_search_gps for paths around you!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    toolbarLayout.removeView(searchGPSIcon);
                    state = Utility.IS_SEARCHING;
                    requestLocationUpdates();
                }
                break;

            case R.id.search_gps_PNG:
                if (state == Utility.IS_WAITING) {

                    LatLng targetLatLng = mMap.getCameraPosition().target;
                    Location targetLocation = new Location("");
                    targetLocation.setLatitude(targetLatLng.latitude);
                    targetLocation.setLongitude(targetLatLng.longitude);
                    Area coveredArea = new Area();
                    coveredArea.setCenter(targetLocation);
                    coveredArea.setRadius(1000);
                    searchForPaths(coveredArea);
                }
                break;

        }
    }

    /**
     * This method check if the user is inside the path chosen to travel
     *
     * @param path,         as the path travelled
     * @param userLocation, as the user's location
     * @return true if the user is inside the path, false otherwise
     */
    private boolean userInside(Path path, Location userLocation) {

        ArrayList<Location> coordinates = path.getCoordinates();
        boolean inside = false;
        for (int i = 0; i < coordinates.size() - 1 && !inside; i++) {

            if (userLocation.distanceTo(coordinates.get(i))
                    <= Utility.MAX_DISTANCE_OFF) {
                inside = true;
            }

        }
        return inside;

    }


}