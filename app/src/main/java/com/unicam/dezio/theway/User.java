package com.unicam.dezio.theway;

/**
 * Created by dezio on 22/11/16.
 */

public class User {

    private String username;
    private String email;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;

    }

    @Override
    public String toString() {
        return username + password + email;
    }
}
