package com.example.mcanny.map;

import org.osmdroid.util.GeoPoint;

public interface SearchListener {
    public void callback(GeoPoint geoPoint, String desc);
}
