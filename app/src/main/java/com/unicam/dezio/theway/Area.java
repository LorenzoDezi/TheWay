package com.unicam.dezio.theway;

import android.location.Location;

/**
 * Defines an area used to find paths inside it. It's a circular area with a center and a radius
 */
public class Area {

    private Location center;
    private double radius;

    /**
     * @return the center of the area
     */
    public Location getCenter() {
        return center;
    }

    /**
     * @return the radius of the area
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius as the radius to be set
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @param center as the center to be set
     */
    public void setCenter(Location center) {
        this.center = center;
    }

}
