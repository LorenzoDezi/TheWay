package com.unicam.dezio.theway;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Date;
import android.location.Location;
import android.location.LocationManager;



/**
 That class store the information about a particular path.
 **/
public class Path {

    private ArrayList<Location> coordinates;
    private int difficulty;
    private int valutation;
    private Vehicle usedVehicle;
    private String description;
    private Vehicle[] vehicle;
    private File gpx;
    private long time;

    /**
     * The default constructor for the class Path
     */
    public Path() {
        this.coordinates = new ArrayList<>();
    }

    /**
     * This method sets the gpx file as an attribute and defines
     * the list of coordinates as it is specificied in the input file (using
     * gpx notation) if it is not already done in the path. This is the case when you
     * create the path for the first time, otherwise coordinates need to be set.
     * @param gpx
     */
    public void setGPX(File gpx, Boolean areCoordinatesSet) throws ParserConfigurationException, IOException,
            SAXException, IllegalArgumentException {
        if (gpx == null)
            throw new IllegalArgumentException("specify the file gpx.");
        if(!areCoordinatesSet) {
            coordinates = new ArrayList<>();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(gpx);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("rtept");
            if (nList.getLength() < 2)
                throw new IllegalArgumentException("We need at least two coordinates");
            for (int i = 0; i < nList.getLength(); i++) {
                Element e = (Element) nList.item(i);
                double lat = Double.parseDouble(e.getAttribute("lat"));
                double lon = Double.parseDouble(e.getAttribute("lon"));
                Location coordinate = new Location(LocationManager.GPS_PROVIDER);
                coordinate.setLatitude(lat);
                coordinate.setLongitude(lon);
                coordinates.add(coordinate);
            }
        }
        this.gpx = gpx;
    }

    public File getGPX() {
        return gpx;
    }
    /**
     return the difficulty of the path.

     @return difficulty
     **/
    public int getDifficulty(){
        return difficulty;
    }

    public void setDifficulty(int difficulty) throws IllegalArgumentException {
        //The difficulty must be between 1 and 5
        if (difficulty >= 0 && difficulty <= 2)
            this.difficulty = difficulty;
        else
            throw  new IllegalArgumentException("The difficulty must be between 0 and 2.");
    }

    /**
     return the valutation of the path.

     @return valutation
     **/

    public int getValutation(){
        return valutation;
    }

    public void setValutation(int valutation) throws IllegalArgumentException {
        //The valutation must be between 1 and 5
        if (valutation >= 1 && valutation <= 5)
            this.valutation = valutation;
        else
            throw  new IllegalArgumentException("The valutation must be between 1 and 5.");
    }

    /**
     Return the used vehicle.

     @return usedVehicle
     **/
    public Vehicle getUsedVehicle(){
        return usedVehicle;
    }

    public void setUsedVehicle(Vehicle usedVehicle) throws IllegalArgumentException {
        //the used vehicle must be specified
        if (usedVehicle != null)
            this.usedVehicle = usedVehicle;
        else
            throw new IllegalArgumentException("the used vehicle must be specified.");
    }

    /**
     Return the usable vehicle

     @return UsableVehicle
     **/
    public Vehicle[] getUsableVehicle(){
        return Arrays.copyOf(vehicle,vehicle.length);
    }

    public void setUsableVehicle(Vehicle[] usableVehicle) throws IllegalArgumentException {
        //The list of the usable vehicle must be specified
        //In the list of the usable vehicle, there must be the vehicle used
        if (usableVehicle != null && usableVehicle.length >= 1 && usableVehicle.length <= Vehicle.values().length){
            boolean b = false;
            for (int i = 0; i < usableVehicle.length && !b; i++)
                b = usableVehicle[i] == usedVehicle;
            if (b)
                vehicle = Arrays.copyOf(usableVehicle,usableVehicle.length);
            else
                throw new IllegalArgumentException("insert the vehicle used in the list of usable vehicles");
        }
        else
            throw new IllegalArgumentException("Specify the vehicles that are usable.");
    }

    /**
     Return the description of the path.

     @return description
     **/
    public String getDescription(){
        return description;
    }

    public void setDescription(String description) throws IllegalArgumentException {
        //The description can be empty, but not null
        //The description can not exceed 256 characters
        if(description != null)
            if(description.length() <= 256)
                this.description = description;
            else
                throw new IllegalArgumentException("description too long");
        else
            this.description = "";
    }

    /**
    Return the time taken to travel the Path

     @return time
     **/
    public long getTime(){
        return time;
    }

    /**
     * It sets the time employed to complete the path
     * @param start
     * @param end
     */
    public void setTime(Date start, Date end) throws IllegalArgumentException {

        if (end != null)
            if(start != null)
                if(end.getTime() > start.getTime())
                    this.time = (end.getTime() - start.getTime()) * 60000;
                else
                    throw new IllegalArgumentException("the time of end must be higher then start.");
            else
                throw new IllegalArgumentException("the time of start must not be a null value");
        else
            throw new IllegalArgumentException("the time of end must not be a null value");

    }

    public void setTime(long time) throws IllegalArgumentException {
        this.time = time;
    }

    /**
     Return the frist coordinate of the path

     @return the frist coordinate
     **/
    public Location getStart(){
        return this.coordinates.get(0);
    }

    /**
     Return the last coordinate of the path

     @return the last coordinate
     **/
    public Location getEnd() {
        return this.coordinates.get(this.coordinates.size()-1);
    }

    /**
     Return the list og coordinates of the path.

     @return <b>ArrayList</b> of <b>Coordinata</b>
     **/
    public ArrayList<Location> getCoordinates(){
        return new ArrayList<Location>(this.coordinates);
    }

    public void setCoordinates(ArrayList<Location> coordinates) throws IllegalArgumentException {

        //There must be at least 2 coordinatess
        if(coordinates.size() >= 2)
            this.coordinates = new ArrayList<Location>(coordinates);
        else
            throw new IllegalArgumentException("there's the need of 2 coordinates.");

    }

    public void addCoordinate(Location coordinate) {
        coordinates.add(coordinate);
    }


    /**
     return a GPX in string format to the relative path.

     @return GPX
     **/
    public String getGPXString() {
        String container = "<?xml version='1.0'><gpx version='1.1' creator='TheWay'><rte>%s</rte></gpx>";
        String body = "";
        String append = "<rtept lat='%s' lon='%s' />";
        for(Location coor : coordinates){
            body += String.format(append,coor.getLatitude(), coor.getLongitude());
        }
        return String.format(container,body);
    }

    /**
    Return the path length in meters (with a margin of error of
    Some meters).

     @return length
     **/
    public double getLength(){
        double length = 0;
        for (int i = 0; i < coordinates.size()-1; i++){
            length += coordinates.get(i).distanceTo(coordinates.get(i+1));
        }
        return length;
    }

}