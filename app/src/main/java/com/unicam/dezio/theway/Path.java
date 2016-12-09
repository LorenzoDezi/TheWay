package com.unicam.dezio.theway;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.util.Date;

/**
 La classe <b>Coordinate</b> permette di immagazzinare due valori <i>double</i>
 indicanti la latitudine e la longitudine di una coordinata e fornisce un metodo
 per calcolare la distance in metri tra due coordinate.
 **/
class Coordinate {
    double lat;
    double lon;

    /**
     Costruttore parametrico di una coordinata.

     @param lat latitudine
     @param lon longitudine
     **/
    Coordinate(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    /**
     Ritorna la latitudine della coordinata.

     @return Latitudine
     **/
    public double getLat(){
        return lat;
    }

    /**
     Ritorna la longitudine della coordinata.

     @return Longitudine
     **/
    public double getLon(){
        return lon;
    }

    /**
     Ritorna la distance in metri (con un margine di errore di qualche metro)
     tra due coordinate.

     @param coord La seconda coordinata
     @return La distance tra la coordinata attuale e la coordinata in input
     **/
    public double distance(Coordinate coord){
        double LATM = 110574.61087757687;
        double LONM = 111302.61697430261;
        Coordinate ca = new Coordinate(lat*LATM,lon*LONM);
        Coordinate cb = new Coordinate(coord.lat*LATM,coord.lon*LONM);
        double a = ca.lat - cb.lat;
        double b = ca.lon - cb.lon;
        double c = Math.sqrt(a*a+b*b);
        return c;
    }

    public String toString(){
        return "("+lat+","+lon+")";
    }
}

enum Vehicle {Feet, Bike};

/**
 Classe che immagazzina le informazioni di un percorso.
 **/
public class Path {

    private ArrayList<Coordinate> coordinate;
    private byte difficolta;
    private byte valutazione;
    private Vehicle mezzoUsato;
    private String descrizione;
    private Vehicle[] mezzi;
    private long tempo;

    /**
     Costruttore parametrico che crea un nuovo oggetto <b>Path</b> a partire
     da un file gpx.

     @param gpx il <b>File</b> gpx da leggere
     @throws IllegalArgumentException
     **/
    Path(File gpx) throws IllegalArgumentException{
        if (gpx == null)
            throw new IllegalArgumentException("Specificare il file gpx.");
        coordinate = new ArrayList<>();
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(gpx);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("rtept");

            for (int i = 0; i < nList.getLength(); i++){
                Element e = (Element) nList.item(i);
                double lat = Double.parseDouble(e.getAttribute("lat"));
                double lon = Double.parseDouble(e.getAttribute("lon"));
                coordinate.add(new Coordinate(lat,lon));
            }
        }catch(Exception e){
            throw new IllegalArgumentException("Errore nella lettura del file gpx.");
        }
    }

    /**
     Costruttore parametrico che crea un nuovo oggetto <b>Path</b> a partire
     da un <b>ArrayList</b> di <b>Coordinata</b>.

     @param coordinate l' <b>ArrayList</b> di <b>Coordinata</b>
     @throws IllegalArgumentException
     **/
    Path(ArrayList<Coordinate> coordinate)throws IllegalArgumentException{
        if(coordinate.size() >= 2)
            this.coordinate = new ArrayList<Coordinate>(coordinate);
        else
            throw new IllegalArgumentException("Ci vogliono almeno 2 coordinate");
    }

    /**
     Costruttore parametrico che crea un nuovo oggetto <b>Path</b> a partire
     da diversi parametri.

     @param coordinate l' <b>ArrayList</b> di <b>Coordinata</b>
     @param inizio il tempo iniziale del percorso
     @param fine il tempo di fine del percorso
     @param difficolta difficoltà del percorso
     @param valutazione valutazione del percorso
     @param mezzoUsato il mezzo usato per il percorso
     @param mezziUsabili un array di mezzi usabili sul percorso
     @param descrizione la descrizione del percorso
     @throws IllegalArgumentException
     **/
    Path(ArrayList<Coordinate> coordinate, Date inizio, Date fine, byte difficolta, byte valutazione,
         Vehicle mezzoUsato, Vehicle[] mezziUsabili, String descrizione)throws IllegalArgumentException{

        //Ci devono essere almeno due coordinate
        if(coordinate.size() >= 2)
            this.coordinate = new ArrayList<Coordinate>(coordinate);
        else
            throw new IllegalArgumentException("Ci vogliono almeno 2 coordinate.");

        //La difficoltà deve essere compresa tra 1 e 5
        if (difficolta >= 1 && difficolta <= 5)
            this.difficolta = difficolta;
        else
            throw  new IllegalArgumentException("La difficoltà deve essere tra 1 e 5.");

        //La valutazione deve essere compresa tra 1 e 5
        if (valutazione >= 1 && valutazione <= 5)
            this.valutazione = valutazione;
        else
            throw  new IllegalArgumentException("La valutazione deve essere tra 1 e 5.");

        //Il mezzo usato deve essere specificato
        if (mezzoUsato != null)
            this.mezzoUsato = mezzoUsato;
        else
            throw new IllegalArgumentException("Specificare il mezzo usato.");

        //La lista dei mezzi usabili deve essere specificata
        //Nella lista dei mezzi usabili, ci deve essere il mezzo usato
        if (mezziUsabili != null && mezziUsabili.length >= 1 && mezziUsabili.length <= Vehicle.values().length){
            boolean b = false;
            for (int i = 0; i < mezziUsabili.length && !b; i++)
                b = mezziUsabili[i] == mezzoUsato;
            if (b)
                mezzi = Arrays.copyOf(mezziUsabili,mezziUsabili.length);
            else
                throw new IllegalArgumentException("Immettere il mezzo usato nella lista dei mezzi usabili.");
        }
        else
            throw new IllegalArgumentException("Specificare i mezzi che è possibilie usare.");

        //La descrizione può essere vuota, ma non nulla
        //La descrizione non può suprare i 256 caratteri
        if(descrizione != null)
            if(descrizione.length() <= 256)
                this.descrizione = descrizione;
            else
                throw new IllegalArgumentException("Descrizione troppo lunga");
        else
            this.descrizione = "";

        //Il tempo di inizio e fine percorso devono essere specificati
        //Il tempo di fine deve essere maggiore del tempo di inizio
        //Il tempo trascorso è espresso in minuti
        if (fine != null)
            if(inizio != null)
                if(fine.getTime() > inizio.getTime())
                    this.tempo = (fine.getTime() - inizio.getTime()) * 60000;
                else
                    throw new IllegalArgumentException("Il tempo di fine deve essere maggiore del tempo di inizio.");
            else
                throw new IllegalArgumentException("Il tempo di inizio non deve essere nullo");
        else
            throw new IllegalArgumentException("Il tempo di fine non deve essere nullo");
    }

    /**
     Costruttore parametrico che clona un <b>Path</b>

     @param p il <b>Path</b> da clonare
     **/
    Path(Path p){
        this.coordinate = p.getCoordinate();
        this.difficolta = p.getDifficolta();
        this.valutazione = p.getValutazione();
        this.mezzoUsato = p.getmezzoUsato();
        this.mezzi = p.getmezziUsabili();
        this.descrizione = p.getDescrizione();
        this.tempo = p.getTempo();
    }

    /**
     Ritorna la difficoltà del percorso.

     @return difficolta
     **/
    public byte getDifficolta(){
        return difficolta;
    }

    /**
     Ritorna la valutazione del percorso.

     @return valutazione
     **/

    public byte getValutazione(){
        return valutazione;
    }

    /**
     Ritorna il mezzo usato.

     @return mezzoUsato
     **/
    public Vehicle getmezzoUsato(){
        return mezzoUsato;
    }

    /**
     Ritorna i mezzi usabili.

     @return mezziUsabili
     **/
    public Vehicle[] getmezziUsabili(){
        return Arrays.copyOf(mezzi,mezzi.length);
    }

    /**
     Ritorna la descrizione del percorso.

     @return descrizione
     **/
    public String getDescrizione(){
        return descrizione;
    }

    /**
     Ritorna il tempo impiegato a percorrere il percorso

     @return tempo
     **/
    public long getTempo(){
        return tempo;
    }

    /**
     Ritorna la prima coordinata del percorso.

     @return la prima coordinata
     **/
    public Coordinate getInizio(){
        return this.coordinate.get(0);
    }

    /**
     Ritorna l'ultima coordinata del percorso.

     @return l'ultima coordinata
     **/
    public Coordinate getFine(){
        return this.coordinate.get(this.coordinate.size()-1);
    }

    /**
     Ritorna la lista delle coordinate del percorso.

     @return <b>ArrayList</b> di <b>Coordinata</b>
     **/
    public ArrayList<Coordinate> getCoordinate(){
        return new ArrayList<Coordinate>(this.coordinate);
    }

    /**
     Ritorna una stringa in GPX relativa al percorso.

     @return GPX
     **/
    public String getGPX(){
        String contenitore = "<?xml version='1.0'><gpx version='1.1' creator='TheWay'><rte>%s</rte></gpx>";
        String body = "";
        String append = "<rtept lat='%s' lon='%s' />";
        for(Coordinate coor : coordinate){
            body += String.format(append,coor.getLat(),coor.getLon());
        }
        String gpxString = String.format(contenitore,body);

        return gpxString;
    }

    /**
     Ritorna la lunghezza del percorso in metri (con un margine di errore di
     qualche metro).

     @return lunghezza
     **/
    public double getLunghezza(){
        double length = 0;
        for (int i = 0; i < coordinate.size()-1; i++){
            length += coordinate.get(i).distance(coordinate.get(i+1));
        }
        return length;
    }

    public String toString(){
        String s = "";
        for (Coordinate l : coordinate){
            s += l.toString()+"\n";
        }
        return s;
    }
}