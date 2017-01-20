package com.unicam.dezio.theway;

/**
 * Created by dezio on 22/11/16.
 */

public class Constants {

    public static final String BASE_URL = "http://192.168.1.46/";
    public static final String REGISTER_OPERATION = "register";
    public static final String LOGIN_OPERATION = "login";
    public static final String SAVE_OPERATION = "save_path";

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";
    public static final String IS_LOGGED_IN = "isLoggedIn";

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public static final String TAG = "TheWay";

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    //the minimum distance to change updates in meters
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    //the minimum time between updates in milliseconds
    public static final float MIN_TIME_BW_UPDATES = 1000;

}
