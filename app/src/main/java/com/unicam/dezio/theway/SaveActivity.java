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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SaveActivity extends AppCompatActivity {

    private Path pathToSave;

    private Context context;

    private static boolean saveResult;


    private Spinner difficultySpinner;
    private Spinner vehicleSpinner;
    private CheckBox bikePossible;
    private CheckBox feetPossible;
    private EditText description;
    private RatingBar ratingBar;
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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
        if(pathJson != "noExtra")
            pathToSave = new Gson().fromJson(pathJson, Path.class);
        context = this.getApplicationContext();

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
            return null;
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

    }

    /**
     * This method is called when one of the submit buttons is clicked. It
     * saves the path online or offline depending on the button clicked
     * @param button
     */
    public void save(View button) {


        int rating = (int) ratingBar.getRating();
        String vehicleUsed = vehicleSpinner.getSelectedItem().toString();
        int difficulty = difficultySpinner.getSelectedItemPosition();
        Boolean isByciclePossible = bikePossible.isActivated();
        Boolean isFeetPossible = feetPossible.isActivated();
        String descriptionString = description.toString();



        if (button.getId() == R.id.online_button)
            storePathOnline(pathToSave);
        else if (button.getId() == R.id.offline_button)
            storePathOffline(pathToSave);
        if(saveResult) {
            //the button disappear from the layout
            mainLayout.removeView(button);
            saveResult = false;
        }

    }

    /**
     * It sends the path to the server, that will store it to the db
     * @param path
     */
    private void storePathOnline(Path path)  {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.SAVE_OPERATION);
        request.setPath(path);
        Call<ServerResponse> response = requestInterface.operation(request);
        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                Log.d(Constants.TAG, response.toString());
                ServerResponse resp = response.body();
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    SaveActivity.saveResult = true;
                    Snackbar.make(findViewById(R.id.save_layout), "The path is correctly saved!", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                //DEBUG
                Log.d(Constants.TAG,"failed");
                Log.d(Constants.TAG, t.getLocalizedMessage());

                Snackbar.make(findViewById(R.id.mainLayout), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                SaveActivity.saveResult = false;
            }
        });
    }

    /**
     * It stores the path in the external storage if present, else
     * it uses the main directory
     * @param path
     */
    private void storePathOffline(Path path) {

        String gpx = path.getGPX();
        String filename = path.hashCode()+".gpx";
        FileOutputStream outputStream;
        
        try {

            File mainDirectory;
            //Priority is saving to the external storage
            if(isExternalStorageWritable()) {
                mainDirectory = getFileStorageDir(context.getExternalFilesDir(null), "GPXs");
            } else {
                mainDirectory = getFileStorageDir(context.getFilesDir(), "GPXs");
            }
            File currentGPXfile = new File(mainDirectory, filename);
            currentGPXfile.createNewFile();
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(gpx.getBytes());
            outputStream.close();

        } catch (IOException ex) {

            Snackbar.make(findViewById(R.id.mainLayout), ex.getMessage(), Snackbar.LENGTH_LONG).show();
            SaveActivity.saveResult = false;
        }
        SaveActivity.saveResult = true;
    }

}
