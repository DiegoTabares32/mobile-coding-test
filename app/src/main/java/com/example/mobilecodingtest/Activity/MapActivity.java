package com.example.mobilecodingtest.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mobilecodingtest.IntentReceiver.ProximityIntentReceiver;
import com.example.mobilecodingtest.Interface.LocationsListener;
import com.example.mobilecodingtest.Model.Location;
import com.example.mobilecodingtest.Model.WeaponsLocation;
import com.example.mobilecodingtest.R;
import com.example.mobilecodingtest.Service.ServiceManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends Activity {

    private static final long EXPIRATION = -1; //-1 indicates it no expires
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Dialog dialog;
    private Button retryButton;
    private static final String PROX_ALERT_INTENT = "ALERT_INTENT";
    private LocationManager locationManager;
    private static Boolean receiverRegistered = false;
    private ProximityIntentReceiver proximityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setLocationManager();

        //if error retrieving locations, retry button to gather them again
        setRetryButton();

        showLoadingDialog();

        setUpMapIfNeeded();

        lookForWeaponsLocations();
    }

    private void lookForWeaponsLocations() {

        ServiceManager.getINSTANCE().retrieveWeaponsLocations(this, new LocationsListener() {
            @Override
            public void onError(String error) {
                cancelDialog();

                retryButton.setVisibility(View.VISIBLE);


                Toast.makeText(MapActivity.this, "Ocurrió un error buscando las coordenadas. Intenta nuevamente", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(List<WeaponsLocation> response) {

                drawLocationsOnMap(response);

                cancelDialog();

            }
        });
    }

    public void showLoadingDialog() {
        if (dialog == null) {
            dialog = new Dialog(this, R.style.Theme_Transparent);
            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading_dialog);
            ((ProgressBar) dialog.findViewById(R.id.dialog_progress)).getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.app_color), PorterDuff.Mode.MULTIPLY);
        }
        dialog.show();
    }

    public void cancelDialog() {
        dialog.dismiss();
    }


    private CircleOptions getCircle(Location location, int radius) {
        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .strokeColor(getResources().getColor(R.color.orange))
                .fillColor(getResources().getColor(R.color.light_orange))
                .radius(radius); // In meters

        return circleOptions;
    }

    /**
     * Clear map and add markers for all locations
     * @param locations
     */
    private void drawLocationsOnMap(List<WeaponsLocation> locations) {
        mMap.clear();

        //this is to calculate the best camera zoom in order to see all markers at a time
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        int requestCode = 1; // for alert proximity

        for (WeaponsLocation location : locations) {
            Location loc = location.getLocation();

            //sets orange circles to draw on each location
            CircleOptions circleOptions = getCircle(loc, location.getRadiusInMeter());

            // Get back the mutable Circle
            mMap.addCircle(circleOptions);

            addProximityAlert(loc.getLatitude(), loc.getLongitude(), location.getRadiusInMeter(), requestCode, location.getCode());

            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude())).title(location.getCode()).snippet("Radio de alcance = " + location.getRadiusInMeter() + " metros"));

            builder.include(marker.getPosition());

            requestCode++;
        }

        registerProximityReceiver();



        LatLngBounds bounds = builder.build();

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()); // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
    }

    private void registerProximityReceiver() {
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
            proximityReceiver = new ProximityIntentReceiver();
            registerReceiver(proximityReceiver, filter);
            receiverRegistered = true;
        }
    }


    private void setRetryButton() {
        retryButton = (Button) findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryButton.setVisibility(View.GONE);

                showLoadingDialog();
                lookForWeaponsLocations();
            }
        });
    }

    /**
     * Add the proximity alert to indicated params.
     * @param lat
     * @param lng
     * @param radius
     * @param id
     * @param locationName The name of the location. It will be displayed on the notification
     */
    private void addProximityAlert(double lat, double lng, int radius, int id, String locationName) {
        Intent intent = new Intent(PROX_ALERT_INTENT);
        intent.putExtra("location", locationName);
        PendingIntent proximityIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.addProximityAlert(lat, lng, radius, EXPIRATION, proximityIntent);
        }else {
            Toast.makeText(this, "Debes encender la ubicación de tu dispositivo para usar la app", Toast.LENGTH_LONG).show();
        }


    }

    private void setLocationManager(){
        locationManager =  (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated..
     * <p/>
     * If it isn't installed {@link MapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the MapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mMap != null){
            mMap.clear();
        }

        //unregister the proximity alert
        if(proximityReceiver != null){
            unregisterReceiver(proximityReceiver);
        }
    }
}
