package com.unicam.dezio.theway;

/**
 *The object is a request made by the client for the server, and it will
 * be encoded in Json format.
 */

public class ServerRequest {

    private String operation;
    private User user;
    private Path path;
    private Area area;

    /**
     * Sets the operation of the request. This field will specify which operation
     * the server must perform
     * @param operation
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Sets the user object inside the request. It's not needed for a valid
     * Request object
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Sets the path object inside the request. It's not needed for a valid
     * Request object
     * @param path
     */
    public void setPath(Path path) {this.path = path; }

    /**
     * Sets the area object inside the request. It's not needed for a valid
     * area object
     * @param area
     */
    public void setArea(Area area) {
        this.area = area;
    }

    /**
     * @return the user object inside the request
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the path object inside the request
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return the area object inside the request
     */
    public Area getArea() {
        return area;
    }

    /**
     * @return the operation object inside the request
     */
    public String getOperation() {
        return operation;
    }

}
