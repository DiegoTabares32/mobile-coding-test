package com.example.mobilecodingtest.Model;

/**
 * Created by Diego on 07/05/2016.
 */
public class Location {

    private double latitude;
    private double longitude;

    //empty constructor for Gson
    public Location(){
        super();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
