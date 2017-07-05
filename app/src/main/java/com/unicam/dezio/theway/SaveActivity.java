package com.unicam.dezio.theway;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.unicam.dezio.theway.Vehicle.Bike;
import static com.unicam.dezio.theway.Vehicle.Feet;

/**
 * This activity is used to set path infos and to save it online, offline or
 * both.
 */
public class SaveActivity extends AppCompatActivity {

    /** the path to be saved **/
    private Path pathToSave;

    /** the gpx file used to save the path **/
    private File currentGPXFile;

    /** the activity context **/
    private Context context;

    /** a flag used to store the result of the save **/
    private static boolean saveResult;

    //Some fields used to get views from the layout
    private Spinner difficultySpinner;
    private Spinner vehicleSpinner;
    private CheckBox bikePossible;
    private CheckBox feetPossible;
    private EditText description;
    private RatingBar ratingBar;
    private RelativeLayout mainLayout;
    private SharedPreferences pref;
    private TextView lengthTextView;
    private TextView timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //if the user is logged, than the path can be saved
        pref = getSharedPreferences(Utility.TAG, Context.MODE_PRIVATE);
        if (pref.getBoolean(Utility.IS_LOGGED_IN, false)) {

            //setting the layout
            setContentView(R.layout.activity_save);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            difficultySpinner = (Spinner) findViewById(R.id.difficulty_spinner);
            vehicleSpinner = (Spinner) findViewById(R.id.vehicle_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.difficulty_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            difficultySpinner.setAdapter(adapter);
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.vehicles, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vehicleSpinner.setAdapter(adapter);
            bikePossible = (CheckBox) findViewById(R.id.bike_check);
            feetPossible = (CheckBox) findViewById(R.id.feet_check);
            description = (EditText) findViewById(R.id.description_area);
            ratingBar = (RatingBar) findViewById(R.id.valutation_rating);
            mainLayout = (RelativeLayout) findViewById(R.id.save_layout);
            timeTextView = (TextView) findViewById(R.id.personal_time);
            lengthTextView = (TextView) findViewById(R.id.length);

            //Retrieving the path to be saved from the intent
            Intent intent = getIntent();
            pathToSave = intent.getExtras().getParcelable("path");
            if(pathToSave == null) {
                //FATAL ERROR
                Utility.goToActivity(this, MainActivity.class, true);
            }
            //timeTextView.setText(pathToSave.getTime().toString());
            timeTextView.setText(
                String.format("%d hour, %d min, %d sec",
                        TimeUnit.MILLISECONDS.toHours(pathToSave.getTime().getTime()),
                        TimeUnit.MILLISECONDS.toMinutes(pathToSave.getTime().getTime()) -
                        TimeUnit.HOURS.toSeconds(TimeUnit.MILLISECONDS.toHours(pathToSave.getTime().getTime())),
                        TimeUnit.MILLISECONDS.toSeconds(pathToSave.getTime().getTime()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(pathToSave.getTime().getTime()))
                )
            );
            context = this.getApplicationContext();
            try {
                pathToSave.setLength();
                lengthTextView.setText(pathToSave.getLenght() + " meters");
            } catch (Exception ex) {
                Toast.makeText(this.getApplicationContext(),
                        "FATAL ERROR" + ex.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

        } else {

            //the user is not logged
            Utility.goToActivity(this, MainActivity.class, true);
        }

    }





    /**
     * The method called when the button "GET BACK!" is clicked. It returns to
     * {@link MainActivity}
     * @param button
     */
    public void getBack(View button) {

        Utility.goToActivity(this, WelcomeActivity.class, true);

    }

    /**
     * This method is called when one of the submit buttons is clicked. It
     * saves the path online or offline depending on the button clicked
     * @param button
     */
    public void save(View button) {


        SaveActivity.saveResult = false;
        int rating = (int) ratingBar.getRating();
        String vehicleString = vehicleSpinner.getSelectedItem().toString();
        int difficulty = difficultySpinner.getSelectedItemPosition();
        Boolean isByciclePossible = bikePossible.isChecked();
        Boolean isFeetPossible = feetPossible.isChecked();
        String descriptionString = description.getText().toString();

        Vehicle vehicleUsed;
        if(vehicleString.equals(getString(R.string.bike_string)))
            vehicleUsed = Bike;
        else if(vehicleString.equals(getString(R.string.feet_string)))
            vehicleUsed = Feet;
        else
            vehicleUsed = null;
        Vehicle[] vehicles;
        if(isByciclePossible && isFeetPossible)
            vehicles = new Vehicle[]{Feet, Bike};
        else if(isByciclePossible)
            vehicles = new Vehicle[]{Bike};
        else if(isFeetPossible)
            vehicles = new Vehicle[]{Feet};
        else
            vehicles = new Vehicle[]{};

        try {

            pathToSave.setStart();
            pathToSave.setDescription(descriptionString);
            pathToSave.setDifficulty(difficulty);
            pathToSave.setValutation(rating);
            pathToSave.setUsedVehicle(vehicleUsed);
            pathToSave.setUsableVehicle(vehicles);

        } catch (Exception ex) {
            Snackbar.make(findViewById(R.id.save_layout), ex.getMessage(), Snackbar.LENGTH_LONG).show();
            return;
        }
        createFile(pathToSave);
        if (button.getId() == R.id.online_button)
            storePathOnline();
        else if (button.getId() == R.id.offline_button)
            storePathOffline();
    }

    /**
     * It sends the path to the server, that will store it to the db
     */
    private void storePathOnline()  {

        if(currentGPXFile != null) {


            pathToSave.setGpxName(currentGPXFile.getName());
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Utility.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RequestInterface requestInterface = retrofit.create(RequestInterface.class);
            // create RequestBody instance from file
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), currentGPXFile);

            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("gpx", currentGPXFile.getName(), requestFile);

            // add another part within the multipart request
            String descriptionString = currentGPXFile.getName();
            RequestBody description =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), descriptionString);

            // finally, execute the request
            Call<ResponseBody> call = requestInterface.upload(description, body);
            call.enqueue(new Callback<ResponseBody>() {


                @Override
                public void onResponse(Call<ResponseBody> call,
                                       Response<ResponseBody> response) {

                    try {
                        String Result = response.body().string();
                        if (Result.equals("OK")) {
                            SaveActivity.saveResult = true;
                        } else {
                            SaveActivity.saveResult = false;
                            Snackbar.make(findViewById(R.id.save_layout), "Error uploading the file!", Snackbar.LENGTH_LONG).show();
                        }
                    } catch (IOException ex) {

                        SaveActivity.saveResult = false;
                        Snackbar.make(findViewById(R.id.save_layout), ex.getMessage(), Snackbar.LENGTH_LONG).show();

                    }

                    if (saveResult) {

                        //Preparing the server comunication
                        Gson gson = new GsonBuilder()
                                .setLenient()
                                .create();
                        Retrofit retrofit2 = new Retrofit.Builder().baseUrl(Utility.BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .build();
                        RequestInterface requestInterface2 = retrofit2.create(RequestInterface.class);
                        //Preparing the request
                        ServerRequest request = new ServerRequest();
                        request.setOperation(Utility.SAVE_OPERATION);
                        request.setPath(pathToSave);
                        User pathOwner = new User();
                        pathOwner.setUsername(pref.getString(Utility.USERNAME, "none"));
                        request.setUser(pathOwner);
                        //Calling the server and processing the response
                        Call<ServerResponse> response2 = requestInterface2.operation(request);
                        response2.enqueue(new Callback<ServerResponse>() {
                            @Override
                            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                                ServerResponse resp = response.body();
                                if (resp.getResult().equals(Utility.SUCCESS)) {
                                    SaveActivity.saveResult = true;
                                    mainLayout.removeView(findViewById(R.id.online_button));
                                } else {
                                    SaveActivity.saveResult = false;
                                }
                                Snackbar.make(findViewById(R.id.save_layout), resp.getMessage(),
                                        Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(Call<ServerResponse> call, Throwable t) {
                                Snackbar.make(findViewById(R.id.save_layout), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                                SaveActivity.saveResult = false;
                            }
                        });
                    }


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                    SaveActivity.saveResult = false;
                    Snackbar.make(findViewById(R.id.save_layout), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();


                }
            });
        }
    }

    /**
     * It stores the path in the external storage if present, else
     * it uses the main directory
     */
    private void storePathOffline() {

        if(currentGPXFile != null) {
            SaveActivity.saveResult = true;
            mainLayout.removeView(findViewById(R.id.offline_button));
        } else
            SaveActivity.saveResult = false;

    }

    /**
     * Creates a file and returns its pointer, used both
     * in offline and online saving
     * @param path
     */
    private void createFile(Path path) {

        //Preparing the gpx file
        String gpx = path.getGPXString();
        String filename = path.hashCode()+".gpx";
        FileOutputStream writer;

        try {

            File mainDirectory;
            //Priority is saving to the external storage
            if(Utility.isExternalStorageWritable()) {
                mainDirectory = Utility.getFileStorageDir(context.getExternalFilesDir(null), "GPXs");
            } else {
                mainDirectory = Utility.getFileStorageDir(context.getFilesDir(), "GPXs");
            }
            currentGPXFile = new File(mainDirectory, filename);
            //Creating phisically the gpx file
            currentGPXFile.createNewFile();
            writer = new FileOutputStream(currentGPXFile);
            writer.write(gpx.getBytes());
            writer.close();

        } catch (IOException ex) {

            Snackbar.make(findViewById(R.id.save_layout), ex.getMessage(), Snackbar.LENGTH_LONG).show();
            currentGPXFile = null;
        }
    }



}
