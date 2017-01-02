package com.unicam.dezio.theway;

/**
 * It's a response that encapsulates the json-encoded response made by the server
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
        return result+message;
    }
}
