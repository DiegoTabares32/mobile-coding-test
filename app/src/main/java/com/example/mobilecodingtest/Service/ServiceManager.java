package com.example.mobilecodingtest.Service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mobilecodingtest.Interface.LocationsListener;
import com.example.mobilecodingtest.Model.WeaponsLocation;
import com.example.mobilecodingtest.Parser.LocationsParser;
import com.google.gson.Gson;

/**
 * Created by Diego on 07/05/2016.
 *
 *  Singleton class that manages apis request
 */
public class ServiceManager {

    private static ServiceManager INSTANCE;

    private ServiceManager(){
        super();
    }

    public static ServiceManager getINSTANCE(){
        if(INSTANCE == null){
            INSTANCE = new ServiceManager();
        }
        return INSTANCE;
    }

    /**
     * Makes http connection with Volley to retrieve locations json
     * @param context
     * @param locationsListener
     */
    public void retrieveWeaponsLocations(final Context context, final LocationsListener locationsListener){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url ="https://redarmyserver.appspot.com/_ah/api/myApi/v1/torretinfocollection";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.v("Mensaje", "RetrieveLocations onResponse = " + response);

                        locationsListener.onResponse(new LocationsParser().parse(response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.v("Mensaje", "RetrieveLocations onError = " + error);

                locationsListener.onError(error.getMessage());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
