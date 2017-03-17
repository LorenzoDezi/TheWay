package com.unicam.dezio.theway;

import android.location.Location;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class PathUnitTest {

    private Path path;

    @Before
    public  void setUp() {

        path = new Path();

    }


    @Test
    public void setDescriptionNullTest() throws Exception {

        String description = null;
        path.setDescription(description);
        assertTrue(path.getDescription().equals(""));

    }

    @Test
    public void setDescriptionTest() throws Exception {

        String description = "testDescription";
        path.setDescription(description);
        assertTrue(path.getDescription().equals(description));

    }

    @Test
    public void setBadDescriptionTest() throws Exception {

        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 258; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String description = sb.toString();
        path.setDescription(description);
        assertTrue(path.getDescription().equals(description));

    }

    @Test
    public void setTimeTest() throws Exception {
        Date start = new Date();
        Date end = new Date(start.getTime() + 10);
        long timeExpected = end.getTime() - start.getTime();
        path.setTime(start, end);
        assertEquals(timeExpected, path.getTime().getTime());
    }

    @Test
    public void setBadTimeTest() throws Exception {

        Date start = new Date();
        Date end = new Date(start.getTime() - 10);
        long timeExpected = end.getTime() - start.getTime();
        path.setTime(start, end);
        assertEquals(timeExpected, path.getTime().getTime());

    }

    @Test
    public void setValutationTest() throws Exception {

        int valutationExpected = 2;
        path.setValutation(valutationExpected);
        assertEquals(valutationExpected, path.getValutation());

    }

    @Test
    public void setBadValutationTest() throws Exception {

        int badValutation = 6;
        path.setValutation(badValutation);
        assertEquals(badValutation, path.getValutation());

    }

    @Test
    public void setUsedVehicleTest() throws Exception {

        Vehicle usedVehicleExpected = Vehicle.Bike;
        path.setUsedVehicle(usedVehicleExpected);
        assertEquals(path.getUsedVehicle(), usedVehicleExpected);

    }

    @Test
    public void setBadUsedVehicleTest() throws Exception {

        Vehicle[] usableVehicle = new Vehicle[1];
        usableVehicle[0] = Vehicle.Feet;
        Vehicle usedVehicleExpected = Vehicle.Bike;
        path.setUsableVehicle(usableVehicle);
        path.setUsedVehicle(usedVehicleExpected);
        assertEquals(path.getUsedVehicle(), usedVehicleExpected);

    }

    @Test
    public void setStartTest() throws Exception {

        ArrayList<Location> coordinates = new ArrayList<>();
        coordinates.add(new Location(""));
        coordinates.add(new Location(""));
        path.setCoordinates(coordinates);
        path.setStart();
        assertEquals(path.getStart(), path.getCoordinates().get(0));

    }

    @Test
    public void setBadStartTest() throws Exception {

        path.setStart();
        assertEquals(path.getStart(), path.getCoordinates().get(0));

    }

    @Test
    public void setCoordinatesTest() throws Exception {

        ArrayList<Location> coordinates = new ArrayList<>();
        coordinates.add(new Location(""));
        coordinates.add(new Location(""));
        path.setCoordinates(coordinates);
        assertEquals(coordinates, path.getCoordinates());

    }

    @Test
    public void setBadCoordinatesTest() throws Exception {

        ArrayList<Location> coordinates = new ArrayList<>();
        coordinates.add(new Location(""));
        path.setCoordinates(coordinates);
        assertEquals(coordinates, path.getCoordinates());

    }

    @Test
    public void setUsableVehiclesTest() throws Exception {

        Vehicle[] usableVehicle = new Vehicle[2];
        usableVehicle[0] = Vehicle.Bike;
        usableVehicle[1] = Vehicle.Feet;
        path.setUsedVehicle(Vehicle.Bike);
        path.setUsableVehicle(usableVehicle);
        assertTrue(Arrays.equals(usableVehicle,(path.getUsableVehicle())));

    }

    @Test
    public void setBadUsableVehiclesTest() throws Exception {

        Vehicle[] usableVehicle = new Vehicle[1];
        usableVehicle[0] = Vehicle.Feet;
        path.setUsedVehicle(Vehicle.Bike);
        path.setUsableVehicle(usableVehicle);
        assertTrue(Arrays.equals(usableVehicle,(path.getUsableVehicle())));

    }

    @Test
    public void setDifficultyTest() throws Exception {

        path.setDifficulty(2);
        assertEquals(2, path.getDifficulty());

    }

    @Test
    public void setBadDifficultyTest() throws Exception {

        path.setDifficulty(4);
        assertEquals(4, path.getDifficulty());

    }





    }