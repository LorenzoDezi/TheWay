package com.unicam.dezio.theway;

import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.RatingBar;
import android.widget.TextView;

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
            }
            difficultyTextView.setText(difficultyText);

            reviewBar.setRating(selectedPath.getValutation());

            String lengthText = "Lenght: " + selectedPath.getLength() + " meters";
            lengthTextView.setText(lengthText);

            //todo: vedere se i secondi funzionano e in caso trovare un metodo più consono
            String timeText = "Time: " + selectedPath.getTime().getTime() + " seconds";
            timeTextView.setText(timeText);

            String vehicleUsed = "Vehicle used: " + selectedPath.getUsedVehicle().toString();
            vehicleUsedTextView.setText(vehicleUsed);

            String possibleVehicles = "You can travel by: ";
            if (selectedPath.getUsableVehicle().length > 1)
                possibleVehicles += "Bike, Feet";
            else
                possibleVehicles += selectedPath.getUsableVehicle()[0].toString();
            vehiclesPossibleTextView.setText(possibleVehicles);
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
     * It simply close the dialog if "OK" is clicked when finished a path
     * @param v
     */
    public void finish(View v) {
        finish();
    }




}
