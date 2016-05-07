package com.example.mobilecodingtest.Parser;

import com.example.mobilecodingtest.Model.WeaponsLocation;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diego on 07/05/2016.
 */
public class LocationsParser {

    private static final String ITEMS = "items";

    /**
     * Parse @jsonString to get @WeaponsLocation objects
     * @param jsonString
     * @return The parsed list of @WeaponsLocation
     */
    public List<WeaponsLocation> parse(String jsonString){

        List<WeaponsLocation> weaponLocations = new ArrayList<WeaponsLocation>();

        WeaponsLocation weaponLocation = new WeaponsLocation();

        JsonArray jsonArray = new JsonParser().parse(jsonString).getAsJsonObject().getAsJsonArray(ITEMS);

        for (int i = 0; i<jsonArray.size(); i++) {
            weaponLocation = new Gson().fromJson(jsonArray.get(i), WeaponsLocation.class);

            weaponLocations.add(weaponLocation);
        }

        return weaponLocations;
    }

}
