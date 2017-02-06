package com.unicam.dezio.theway;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Utility class, used to get some values/methods useful to the application
 */

class Utility {

    //TAG of the application
    static final String TAG = "TheWay";

    //Utility used in server communications
    //static final String BASE_URL = "http://192.168.1.46/";
    public static final String BASE_URL = "http://192.168.43.64/";
    static final String REGISTER_OPERATION = "register";
    static final String LOGIN_OPERATION = "login";
    static final String SAVE_OPERATION = "save_path";
    static final String REQUEST_OPERATION = "request_paths";
    static final String SUCCESS = "success";
    static final String FAILURE = "failure";


    //requestCodes for intent on MapActivity
    static final int PATH_INFO = 1;
    static final int FINISHED = 2;

    //Utility used for keys on intent's extras
    //todo: vedere quali ancora non ho settato per bene
    static final String IS_LOGGED_IN = "isLoggedIn";
    static final String USERNAME = "username";
    static final String EMAIL = "email";


    //Utility relative to the state of MapActivity
    static final int IS_SEARCHING = 0;
    static final int IS_TRAVELLING = 1;
    static final int IS_CREATING = 2;
    static final int IS_WAITING = 3;


    static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    //the minimum distance to change updates in meters
    static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 20;
    //the minimum time between updates in milliseconds
    static final long MIN_TIME_BW_UPDATES = 1000;
    //the maximum distance in which a user can go off path
    static final long MAX_DISTANCE_OFF = 50;

    /**
     * Sends the user from the source activity to the destination activity
     * @param isFinished as a flag indicating if the source activity must be finished or not
     */
    static void goToActivity(Activity srcActivity, Class dstActivityClass, boolean isFinished) {
        Intent intent = new Intent(srcActivity, dstActivityClass);
        srcActivity.startActivity(intent);
        if (isFinished)
            srcActivity.finish();
    }

    static void goToMapActivity(Activity srcActivity, int choice) {
        Intent intent = new Intent(srcActivity, MapActivity.class);
        intent.putExtra("choice", choice);
        srcActivity.startActivity(intent);
    }

    /**
     * It checks if the external storage is available for writing
     * @return true if the external Storage is mounted, false otherwise
     */
    static boolean isExternalStorageWritable() {
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
    static File getFileStorageDir(File dir, String dirName) throws IOException {
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


}
