package com.unicam.dezio.theway;

/**
 * This class represents a registered "theWay" user
 */

public class User {


    private String username;
    private String email;
    private String password;

    /**
     * Sets the user's password
     * @param password
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * @return the user's password
     */
    public String getPassword() { return password; }

    /**
     * Sets the email's password
     * @param email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * @return the user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's username
     * @param username
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * @return the user's username
     */
    public String getUsername() { return username; }

}
