package com.unicam.dezio.theway;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

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

public class SaveActivity extends AppCompatActivity {

    private Path pathToSave;

    private File currentGPXFile;

    private Context context;

    private static boolean saveResult;


    private Spinner difficultySpinner;
    private Spinner vehicleSpinner;
    private CheckBox bikePossible;
    private CheckBox feetPossible;
    private EditText description;
    private RatingBar ratingBar;
    private RelativeLayout mainLayout;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        pref = getSharedPreferences(Constants.TAG, Context.MODE_PRIVATE);
        if (pref.getBoolean(Constants.IS_LOGGED_IN, false)) {
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

            //DEBUG
            Intent intent = getIntent();
            String pathJson = intent.getExtras().getString("path", "noExtra");
            if (pathJson != "noExtra")
                pathToSave = new Gson().fromJson(pathJson, Path.class);
            context = this.getApplicationContext();
        } else {
            goToLogin();
        }

    }

    private void goToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * It checks if the external storage is available for writing
     * @return
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * It returns the directory in which the path will be stored. It creates the directory
     * if it is not already there
     * @param dir, as the upper directory of the new directory
     * @param dirName, as the name of the new directory
     * @return the new directory itself as a File object
     * @throws IOException
     */
    private File getFileStorageDir(File dir, String dirName) throws IOException {
        File file = new File(dir, dirName);
        if (file.exists()) {
            return file;
        } else {
            if(!file.mkdirs()) {
                throw new IOException("Directory can't be created!");
            }
        }
        return file;
    }

    /**
     * The method called when the button "GET BACK!" is clicked. It returns to
     * the main page of the application (the WelcomeActivity).
     * @param button
     */
    public void getBack(View button) {

        Intent intent = new Intent(context, WelcomeActivity.class);
        startActivity(intent);
        finish();

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
        if(vehicleString == getString(R.string.bike_string))
            vehicleUsed = Bike;
        else if(vehicleString == getString(R.string.feet_string))
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
        } catch (IllegalArgumentException ex) {
            Snackbar.make(findViewById(R.id.save_layout), ex.getMessage(), Snackbar.LENGTH_LONG).show();
            return;
        }
        createFile(pathToSave);
        if (button.getId() == R.id.online_button)
            storePathOnline();
        else if (button.getId() == R.id.offline_button)
            storePathOffline();
        return;
    }

    /**
     * It sends the path to the server, that will store it to the db
     */
    private void storePathOnline()  {

        if(currentGPXFile != null) {

            pathToSave.setGpxName(currentGPXFile.getName());

            Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
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


                    //DEBUG
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
                        Retrofit retrofit2 = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .build();
                        RequestInterface requestInterface2 = retrofit2.create(RequestInterface.class);
                        //Preparing the request
                        ServerRequest request = new ServerRequest();
                        request.setOperation(Constants.SAVE_OPERATION);
                        request.setPath(pathToSave);
                        User pathOwner = new User();
                        pathOwner.setUsername(pref.getString(Constants.USERNAME, "none"));
                        request.setUser(pathOwner);
                        //Calling the server and processing the response
                        Call<ServerResponse> response2 = requestInterface2.operation(request);
                        response2.enqueue(new Callback<ServerResponse>() {
                            @Override
                            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                                Log.d(Constants.TAG, response.toString());
                                ServerResponse resp = response.body();
                                Log.d(Constants.TAG, resp.toString());
                                if (resp.getResult().equals(Constants.SUCCESS)) {
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
                                //DEBUG
                                Log.d(Constants.TAG, "failed");
                                Log.d(Constants.TAG, t.getLocalizedMessage());
                                Snackbar.make(findViewById(R.id.save_layout), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                                SaveActivity.saveResult = false;
                            }
                        });
                    }


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                    SaveActivity.saveResult = false;
                    //DEBUG
                    Log.d(Constants.TAG, t.getLocalizedMessage());
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
            if(isExternalStorageWritable()) {
                mainDirectory = getFileStorageDir(context.getExternalFilesDir(null), "GPXs");
            } else {
                mainDirectory = getFileStorageDir(context.getFilesDir(), "GPXs");
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
