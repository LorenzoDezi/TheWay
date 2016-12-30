package com.unicam.dezio.theway;

/**
 *The object is a request made by the client for the server, and it will
 * be encoded in Json format.
 */

public class ServerRequest {

    private String operation;
    private User user;
    private Path path;

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPath(Path path) {this.path = path; }

    @Override
    //DEBUG
    public String toString() {
        return operation + user.getUsername() + user.getPassword() + user.getEmail();
    }
}
