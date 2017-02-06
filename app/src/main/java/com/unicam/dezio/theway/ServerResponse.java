package com.unicam.dezio.theway;

import java.util.List;

/**
 * It's a response that encapsulates the json-encoded response made by the server
 */

public class ServerResponse {

    private String result;
    private String message;
    private User user;
    private Path[] paths;

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() { return user; }

    public Path[] getPaths() { return paths; }

}
