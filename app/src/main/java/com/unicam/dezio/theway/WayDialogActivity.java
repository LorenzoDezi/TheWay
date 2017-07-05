package com.unicam.dezio.theway;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Rating;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The class used to show a Dialog to the user, that shows information about the path clicked on
 * the {@link MapActivity} map during searching, or notifies the user that he has completed the path
 * chosen.
 */
public class WayDialogActivity extends AppCompatActivity {

    /** the path selected by the user on {@link MapActivity} map **/
    Path selectedPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setting the layout
        Intent intent = getIntent();
        if(intent.getExtras().getBoolean("EndOfPath", false)) {

            setContentView(R.layout.activity_path_finish);

        } else {

            setContentView(R.layout.activity_path_info);
            this.selectedPath = (Path) intent.getExtras().get("Path");
            //Retrieving all necessary views
            TextView difficultyTextView = (TextView) findViewById(R.id.difficulty_spec);
            RatingBar reviewBar = (RatingBar) findViewById(R.id.review_spec);
            TextView lengthTextView = (TextView) findViewById(R.id.length_spec);
            TextView timeTextView = (TextView) findViewById(R.id.time_spec);
            TextView vehicleUsedTextView = (TextView) findViewById(R.id.vehicle_used_spec);
            TextView vehiclesPossibleTextView = (TextView) findViewById(R.id.vehicles_possible_spec);
            TextView descriptionTextView = (TextView) findViewById(R.id.description_spec);
            Button deleteButton = (Button) findViewById(R.id.removeButton);
            SharedPreferences pref = getSharedPreferences(Utility.TAG, Context.MODE_PRIVATE);
            if(!pref.getString(Utility.USERNAME, "no").equals(selectedPath.getAuthor())) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.info_layout);
                layout.removeView(deleteButton);
            }
            //Setting all views properly to the input path
            this.setTitle("Path infos");
            String difficultyText = "Difficulty: ";
            int difficulty = selectedPath.getDifficulty();
            switch (difficulty) {
                case 0:
                    difficultyText += "Easy";
                    break;
                case 1:
                    difficultyText += "Medium";
                    break;
                case 2:
                    difficultyText += "Hard";
                    break;

            }
            difficultyTextView.setText(difficultyText);

            reviewBar.setRating(selectedPath.getValutation());

            String lengthText = "Lenght: " + selectedPath.getLength() + " meters";
            lengthTextView.setText(lengthText);

            String timeText = "Time: NULL";
            if(selectedPath.getTime() != null)
                timeText = "Time: " + selectedPath.getTime().toString();
            timeTextView.setText(timeText);

            String vehicleUsed = "Vehicle used: NULL";
            if(selectedPath.getUsedVehicle() != null)
                vehicleUsed = "Vehicle used: " + selectedPath.getUsedVehicle().toString();
            vehicleUsedTextView.setText(vehicleUsed);

            String possibleVehicles = "You can travel by: ";
            if(selectedPath.getUsableVehicle() != null)
                if (selectedPath.getUsableVehicle().length > 1)
                    possibleVehicles += "Bike, Feet";
                else
                    possibleVehicles += selectedPath.getUsableVehicle()[0].toString();
            else
                possibleVehicles += "NULL";
            vehiclesPossibleTextView.setText(possibleVehicles);

            String description = "Description: ";
            if(selectedPath.getDescription() != null)
                description += selectedPath.getDescription();
            else
                description += "NULL";
            descriptionTextView.setText(description);
        }

    }

    /**
     * This method returns a result to the calling MapActivity, that will
     * start a new Path for the user to travel
     * @param v as the button clicked
     */
    public void startPath(View v) {

        Intent intent = this.getIntent();
        intent.putExtra("Path", this.selectedPath);
        this.setResult(RESULT_OK, intent);
        finish();

    }

    /**
     * This method remove a path from the server
     * @param v
     */
    public void removePath(View v) {

        //Preparing the server communication
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Utility.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        ServerRequest request = new ServerRequest();
        request.setOperation(Utility.DELETE_OPERATION);
        request.setPath(selectedPath);
        Call<ServerResponse> call = requestInterface.operation(request);
        try {
            Response response = call.execute();
            if(response.isSuccessful()) {
                ServerResponse resp = (ServerResponse) response.body();
                Snackbar.make(findViewById(R.id.info_layout), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                if(resp.getResult().equals(Utility.SUCCESS)) {
                    Intent intent = this.getIntent();
                    intent.putExtra("Path", this.selectedPath);
                    this.setResult(Utility.DELETED, intent);
                    finish();
                }

            } else
                throw new IOException(response.message());
        } catch (IOException e) {
            Toast.makeText(this.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * It simply close the dialog if "OK" is clicked when finished a path
     * @param v
     */
    public void finish(View v) {

        Intent intent = this.getIntent();
        this.setResult(Utility.FINISHED, intent);
        finish();

    }




}
