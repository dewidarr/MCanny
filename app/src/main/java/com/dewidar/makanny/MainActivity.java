package com.dewidar.makanny;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.dewidar.makanny.map.MapFragment;
import com.google.android.gms.ads.MobileAds;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Gui_Manager.getInstance().setFragmentManager(getSupportFragmentManager());
        Gui_Manager.getInstance().setContext(this);

        MobileAds.initialize(this);

        MapFragment mapFragment = new MapFragment();
        Gui_Manager.getInstance().setCurrentFragment(mapFragment);


    }
}
