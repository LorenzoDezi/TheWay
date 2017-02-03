package com.unicam.dezio.theway;

import android.location.Location;

/**
 * Created by dezio on 21/01/17.
 */

public class Area {

    private Location center;
    private double radius;

    public Location getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

}
