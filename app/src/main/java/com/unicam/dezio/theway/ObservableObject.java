package com.unicam.dezio.theway;

import android.util.Log;

import java.util.Observable;

/**
 * This class is a Singleton. It's used as an Observer
 */

public class ObservableObject extends Observable {

    private static ObservableObject instance = new ObservableObject();
    public static ObservableObject getInstance() {
        return instance;
    }

    private ObservableObject() {

    }

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers();
        }
    }

}
