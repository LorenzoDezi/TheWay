package com.unicam.dezio.theway;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Utility class, used to get some values/methods useful to the application
 */

class Utility {

    //TAG of the application
    static final String TAG = "TheWay";

    //Utility used in server communications
    static final String BASE_URL = "http://192.168.1.46/";
    //static final String BASE_URL = "http://10.0.11.243/";
    static final String REGISTER_OPERATION = "register";
    static final String LOGIN_OPERATION = "login";
    static final String CHECK_OPERATION = "check_user";
    static final String SAVE_OPERATION = "save_path";
    static final String DELETE_OPERATION = "remove_path";
    static final String REQUEST_OPERATION = "request_paths";
    static final String SUCCESS = "success";

    //requestCodes for intent on MapActivity
    static final int PATH_INFO = 1;
    static final int FINISHED = 2;
    static final int DELETED = 3;

    //Utility used for keys on intent's extras
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

    /**
     * This method sends the user to the MapActivity, specifying the choice of
     * starting a new path or searching an existing one
     * @param srcActivity as the source activity
     * @param choice as the choice taken
     */
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

    /**
     * This method register a user, starting the communication with the server
     * @param username
     * @param mail
     * @param pwd as the password
     */
    static boolean registerProcess(String username, String mail, String pwd, final Activity callingActivity) {

        final View layout;
        if(callingActivity instanceof RegisterActivity) {
            layout = callingActivity.findViewById(R.id.registerLayout);
        }
        else
            layout = callingActivity.findViewById(R.id.mainLayout);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Utility.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        final User user = new User();
        user.setUsername(username);
        user.setEmail(mail);
        user.setPassword(pwd);
        ServerRequest request = new ServerRequest();
        request.setOperation(Utility.REGISTER_OPERATION);
        request.setUser(user);
        Call<ServerResponse> call = requestInterface.operation(request);
        Response response = null;
        Boolean result = false;
        try {
            response = call.execute();
            if(response.isSuccessful()) {
                ServerResponse resp = (ServerResponse) response.body();
                if(resp.getResult().equals(Utility.SUCCESS))
                    result = true;

            }
        } catch (IOException e) {

        }
        return result;
    }

    /**
     * Checks if the user specified by the username as parameter is in the
     * database
     * @param username
     * @return true if exists, false otherwise
     */
    static boolean checkUserExist(String username) {

        //Preparing the server communication
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Utility.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        User user = new User();
        user.setUsername(username);
        ServerRequest request = new ServerRequest();
        request.setOperation(Utility.CHECK_OPERATION);
        request.setUser(user);
        Call<ServerResponse> call = requestInterface.operation(request);
        Response<ServerResponse> response = null;
        Boolean result = false;
        try {
            response = call.execute();
            if(response.isSuccessful()) {
                ServerResponse resp = response.body();
                if(resp.getResult().equals(Utility.SUCCESS)) {
                    result = true;
                }
            }
        } catch (IOException ex) {

        }
        return result;
    }


}
