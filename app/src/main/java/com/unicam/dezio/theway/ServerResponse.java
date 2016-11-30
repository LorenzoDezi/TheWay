package com.unicam.dezio.theway;

/**
 * Created by dezio on 22/11/16.
 */

public class ServerResponse {

    private String result;
    private String message;
    private User user;

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    @Override
    //DEBUG
    public String toString() {
        return result+message+user.toString();
    }
}
