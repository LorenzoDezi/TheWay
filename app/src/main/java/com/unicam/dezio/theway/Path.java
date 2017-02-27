package com.unicam.dezio.theway;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Time;
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
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.java.contract.*;


/**
 That class store the information about a particular path.
 **/
/* @Invariant({"difficulty >= 0 && difficulty <= 2",
            "valutation >= 0 and valutation <= 5"}) */
public class Path implements Parcelable {

    /** ArrayList of coordinates representing the path **/
    private ArrayList<Location> coordinates;

    /** the difficulty. 0 is for easy, 1 is for medium, 2 is for hard **/
    private int difficulty;

    /** the valutation is a five star rating going from 0 to 5 **/
    private int valutation;

    /** the vehicle used to make this path **/
    private Vehicle usedVehicle;

    /** a description made by the user who creates the path **/
    private String description;

    /** possible vehicles you can travel this path with **/
    private Vehicle[] possibleVehicles;

    /** the gpxName of the path, used to retrieve the corresponding
     * gpx file in the database/sd card **/
    private String gpxName;

    /**
     * the name of the user who created it
     */
    private String author;

    /** the time employed to complete the path **/
    private Time time;

    /** path length in meters **/
    private float length;

    /** the first coordinate of the path, also the starting point **/
    private Location start;

    /** the creator used to parcel this object **/
    public static final Parcelable.Creator<Path> CREATOR
            = new Parcelable.Creator<Path>() {


        @Override
        public Path createFromParcel(Parcel source) {
            return new Path(source);
        }

        @Override
        public Path[] newArray(int size) {
            return new Path[0];
        }
    };

    /**
     * private constructor used to reconstruct a parceled path
     * **/
    private Path(Parcel source) {

        Bundle bundle = source.readBundle();
        difficulty = bundle.getInt("difficulty");
        valutation = bundle.getInt("valutation");
        gpxName = bundle.getString("gpxName");
        time = (Time) bundle.getSerializable("time");
        length = bundle.getFloat("length");
        start = bundle.getParcelable("start");
        usedVehicle = (Vehicle) bundle.getSerializable("usedVehicle");
        possibleVehicles = (Vehicle[]) bundle.getSerializable("possibleVehicles");
        coordinates = bundle.getParcelableArrayList("coordinates");
        description = bundle.getString("description");
        author = bundle.getString("author");

    }

    /**
     * The default constructor for the class Path
     */
    public Path() {
        this.coordinates = new ArrayList<>();
    }

    /**
     * It returns the gpx filename
     * @return the gpx filename as a string
     */
    public String getGpxName() {
        return gpxName;
    }

    /**
     * It sets the name of the gpx file associated with the path in the server
     * @param gpxName as the name of the gpx file
     */
    public void setGpxName(String gpxName)  {
        this.gpxName = gpxName;
    }


    /**
     return the difficulty of the path.

     @return difficulty
     **/
    public int getDifficulty(){
        return difficulty;
    }

    /**
     * sets the difficulty of the path
     * @param difficulty as the integer difficulty of the path
     * @throws IllegalArgumentException
     */
    public void setDifficulty(int difficulty) throws IllegalArgumentException {
        //The difficulty must be between 1 and 5
        if (difficulty >= 0 && difficulty <= 2)
            this.difficulty = difficulty;
        else
            throw  new IllegalArgumentException("The difficulty must be between 0 and 2.");
    }

    /**
     * return the length of the path
     * @return the length as a float, in meters
     */
    public float getLenght() {

        return length;

    }

    /**
     * @return the author's name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author's name
     * @param author as the author's name
     */
    public void setAuthor(String author) {
        this.author = author;
    }


    /**
     * sets the length of the path, it has a little margin error of some
     * meters
     */
    public void setLength() {

        if(coordinates == null)
            throw new NullPointerException("You must first set coordinates, and then you can set" +
                    "the lenght!");

        int length = 0;
        int i;
        for (i=0; i < coordinates.size() - 1; i++) {
            length += coordinates.get(i).distanceTo(coordinates.get(i+1));
        }
        this.length = length;

    }

    /**
     return the valutation of the path.
     @return valutation as an integer between 0 and 5
     **/
    public int getValutation(){
        return valutation;
    }

    /**
     * sets the valutation of the path
     * @param valutation
     * @throws IllegalArgumentException
     */
    public void setValutation(int valutation) throws IllegalArgumentException {
        //The valutation must be between 1 and 5
        if (valutation >= 0 && valutation <= 5)
            this.valutation = valutation;
        else
            throw  new IllegalArgumentException("The valutation must be between 0 and 5.");
    }

    /**
     Return the used possibleVehicles.
     @return usedVehicle
     **/
    public Vehicle getUsedVehicle(){
        return usedVehicle;
    }

    /**
     * Sets the vehicle used to cross the path
     * @param usedVehicle as the used vehicle
     * @throws IllegalArgumentException
     */
    public void setUsedVehicle(Vehicle usedVehicle) throws IllegalArgumentException {
        //the used possibleVehicles must be specified
        if (usedVehicle != null)
            this.usedVehicle = usedVehicle;
        else
            throw new IllegalArgumentException("the used possibleVehicles must be specified.");
    }

    /**
     Return the usable possibleVehicles

     @return UsableVehicle
     **/
    public Vehicle[] getUsableVehicle(){
        if(possibleVehicles != null)
            return Arrays.copyOf(possibleVehicles, possibleVehicles.length);
        else
            return null;
    }

    /**
     * sets the vehicles that can be used to travel this path
     * @param usableVehicle as an array of vehicles
     * @throws IllegalArgumentException
     */
    public void setUsableVehicle(Vehicle[] usableVehicle) throws IllegalArgumentException {
        //The list of the usable possibleVehicles must be specified
        //In the list of the usable possibleVehicles, there must be the possibleVehicles used
        if (usableVehicle != null && usableVehicle.length >= 1 && usableVehicle.length <= Vehicle.values().length){
            boolean b = false;
            for (int i = 0; i < usableVehicle.length && !b; i++)
                b = usableVehicle[i] == usedVehicle;
            if (b)
                possibleVehicles = Arrays.copyOf(usableVehicle,usableVehicle.length);
            else
                throw new IllegalArgumentException("insert the possibleVehicles used in the list of usable vehicles");
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
    public Time getTime(){
        return time;
    }

    /**
     * It sets the time employed to complete the path
     * @param start, as the time of beginning
     * @param end, as the time of end
     */
    public void setTime(Date start, Date end) throws IllegalArgumentException {

        long time;
        if (end != null)
            if(start != null)
                if(end.getTime() > start.getTime())
                    //time = (end.getTime() - start.getTime()) * 60000;
                    time = (end.getTime() - start.getTime());
                else
                    throw new IllegalArgumentException("the time of end must be higher then start.");
            else
                throw new IllegalArgumentException("the time of start must not be a null value");
        else
            throw new IllegalArgumentException("the time of end must not be a null value");
        this.time = new Time(time);

    }

    /**
     * It sets the time employed to complete the path
     * @param time, as the time employed
     * @throws IllegalArgumentException
     */
    public void setTime(long time) throws IllegalArgumentException {
        this.time = new Time(time);
    }

    /**
     Return the first coordinate of the path,
     the start point
     @return the first coordinate
     **/
    public Location getStart(){
        return start;
    }

    /**
     * It sets the first coordinate of the path, the
     * start point.
     */
    public void setStart() {
        if(coordinates == null)
            throw new NullPointerException("You must first set the coordinates to retrieve" +
                    "the start point");
        this.start = this.coordinates.get(0);
    }

    /**
     Return the last coordinate of the path

     @return the last coordinate
     **/
    public Location getEnd() {
        return this.coordinates.get(this.coordinates.size()-1);
    }

    /**
     Returns path's coordinates

     @return coordinates of path as an ArrayList of Location objects
     **/
    public ArrayList<Location> getCoordinates(){
        return new ArrayList<Location>(this.coordinates);
    }


    /**
     * set path coordinates using an ArrayList of coordinates
     * @param coordinates, as an ArrayList of Location objects
     * @throws IllegalArgumentException
     */
    public void setCoordinates(ArrayList<Location> coordinates) throws IllegalArgumentException {

        //There must be at least 2 coordinatess
        if(coordinates.size() >= 2)
            this.coordinates = new ArrayList<Location>(coordinates);
        else
            throw new IllegalArgumentException("there's the need of 2 coordinates.");

    }

    /**
     * This method takes as input a gpx file and uses it to set the path's coordinates
     * @param gpx
     *
     */
    public void setCoordinates(File gpx) throws ParserConfigurationException, IOException,
            SAXException, IllegalArgumentException {

        if (gpx == null)
            throw new IllegalArgumentException("specify the file gpx.");
        this.coordinates = new ArrayList<>();
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
            this.coordinates.add(coordinate);
        }
        this.gpxName = gpx.getName();
    }

    /**
     * Add a single coordinate to the path's list
     * @param coordinate, as a Location object
     */
    public void addCoordinate(Location coordinate) {
        coordinates.add(coordinate);
    }


    /**
     return a GPX in string format to the relative path.

     @return GPX
     **/
    public String getGPXString() {
        String container = "<?xml  version='1.0'?><gpx xmlns='http://www.topografix.com/GPX/1/1'" +
                " version='1.1' creator='TheWay'><rte>%s</rte></gpx>";
        String body = "";
        String append = "<rtept lat='%s' lon='%s' />";
        for(Location coor : coordinates){
            body += String.format(append,coor.getLatitude(), coor.getLongitude());
        }
        return String.format(container,body);
    }

    /**
    Return the path length in meters
     @return length
     **/
    public double getLength(){

        return length;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        Bundle bundle = new Bundle();
        bundle.putInt("difficulty", difficulty);
        bundle.putInt("valutation", valutation);
        bundle.putFloat("length", length);
        bundle.putSerializable("usedVehicle", usedVehicle);
        bundle.putSerializable("possibleVehicles",possibleVehicles);
        bundle.putSerializable("description", description);
        bundle.putSerializable("time", time);
        bundle.putParcelable("start", start);
        bundle.putSerializable("gpxName", gpxName);
        bundle.putString("author", author);
        bundle.putParcelableArrayList("coordinates", coordinates);
        dest.writeBundle(bundle);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Path path = (Path) o;

        return gpxName != null ? gpxName.equals(path.gpxName) : path.gpxName == null;

    }

    @Override
    public int hashCode() {
        return gpxName != null ? gpxName.hashCode() : 0;
    }
}