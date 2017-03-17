package com.unicam.dezio.theway;

import android.content.Context;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PathAndroidUnitTest {

    private Path path;

    @Before
    public void setUp() {
        path = new Path();
    }

    @Test
    public void setLengthTest() throws Exception {

        ArrayList<Location> coordinates = new ArrayList<>();
        Location loc1 = new Location("");
        loc1.setLatitude(12);
        loc1.setLongitude(43);
        Location loc2 = new Location("");
        loc2.setLatitude(15);
        loc2.setLongitude(60);
        coordinates.add(loc1);
        coordinates.add(loc2);
        path.setCoordinates(coordinates);
        path.setLength();

    }

    @Test
    public void setLengthBadTest() throws Exception {

        path.setLength();

    }
}
