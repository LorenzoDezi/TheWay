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

    /**
     * @return the result string that defines the success or the failure of the request
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the result of the response
     * @param result
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the message of the response
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message of the response
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the user associated with the request, it can be NULL
     */
    public User getUser() { return user; }

    /**
     * Sets the user of the response
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the paths associated with the requests they can be NULL
     */
    public Path[] getPaths() { return paths; }

    /**
     * Sets the paths for the response
     * @param paths
     */
    public void setPaths(Path[] paths) {
        this.paths = paths;
    }

}
