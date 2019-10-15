package com.example.mcanny;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.mcanny.map.MapFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Gui_Manager.getInstance().setFragmentManager(getSupportFragmentManager());
        Gui_Manager.getInstance().setContext(this);

        MapFragment mapFragment = new MapFragment();
        Gui_Manager.getInstance().setCurrentFragment(mapFragment);


    }
}
