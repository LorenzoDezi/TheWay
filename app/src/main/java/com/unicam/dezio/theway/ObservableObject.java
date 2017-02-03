package com.unicam.dezio.theway;

import android.util.Log;

import java.util.Observable;

/**
 * This class is a Singleton. It is "observed" by
 * the class {@link BaseActivity}, and every updates made by
 * {@link NetworkChangeReceiver} is notified.
 */

public class ObservableObject extends Observable {

    /** it's the only instance of the class **/
    private static ObservableObject instance = new ObservableObject();

    /**
     *@return the single instance of this class
     **/
    public static ObservableObject getInstance() {
        return instance;
    }

    /**
     * updates the state of this object
     */
    public void updateValue() {
        synchronized (this) {
            setChanged();
            notifyObservers();
        }
    }

}
