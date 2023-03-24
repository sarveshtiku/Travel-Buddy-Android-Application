package com.example.travelbuddy;

import com.google.android.gms.maps.model.LatLng;

public class TripPlace {
    public String name;
    public String address;
    public LatLng latLng;

    public TripPlace(String name, String address, LatLng latLng) {
        this.name = name;
        this.address = address;
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}