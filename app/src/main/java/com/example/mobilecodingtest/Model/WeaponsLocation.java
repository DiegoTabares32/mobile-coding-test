package com.example.mobilecodingtest.Model;

/**
 * Created by Diego on 07/05/2016.
 */
public class WeaponsLocation {

    private Location location;
    private int radius;
    private String code;
    private int radiusInMeter;

    //empty constructor for Gson
    public WeaponsLocation(){
        super();
    }

    public Location getLocation() {
        return location;
    }

    public String getCode() {
        return code;
    }
}
