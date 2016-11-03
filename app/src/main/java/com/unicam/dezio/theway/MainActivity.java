package com.unicam.dezio.theway;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.app.ActionBar actionBar = getActionBar();
        //actionBar.setIcon(Logo qui!!);
        //E poi le varie configurazioni dell'actionBar
    }
}
