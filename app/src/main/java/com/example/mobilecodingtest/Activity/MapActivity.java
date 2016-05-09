package com.example.mobilecodingtest.Activity;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends FragmentActivity {

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

    private void lookForWeaponsLocations(){

        ServiceManager.getINSTANCE().retrieveWeaponsLocations(this, new LocationsListener() {
            @Override
            public void onError(String error) {
                cancelDialog();

                retryButton.setVisibility(View.VISIBLE);


                Toast.makeText(MapActivity.this, "Ocurrió un error buscando las coordenadas. Intenta nuevamente", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(List<WeaponsLocation> response) {

                response.clear();
                //todo para probar
                WeaponsLocation weaponsLocation = new WeaponsLocation();

                Location location = new Location();
//casa                location.setLongitude(-58.412261);
//                location.setLatitude(-34.623976);

                location.setLongitude(-58.438159);// vege
                location.setLatitude(-34.597962);

                weaponsLocation.setLocation(location);
                weaponsLocation.setCode("Vege");
                weaponsLocation.setRadiusInMeter(60);

                response.add(weaponsLocation);


                weaponsLocation = new WeaponsLocation();

                location = new Location();

                location.setLongitude(-58.438342);// kiosco scalabrini
                location.setLatitude(-34.599556);

                weaponsLocation.setLocation(location);
                weaponsLocation.setCode("Kiosco");
                weaponsLocation.setRadiusInMeter(40);

                response.add(weaponsLocation);

                weaponsLocation = new WeaponsLocation();

                location = new Location();

                location.setLongitude(-58.437623);// esquina offi
                location.setLatitude(-34.599071);

                weaponsLocation.setLocation(location);
                weaponsLocation.setCode("Esquina");
                weaponsLocation.setRadiusInMeter(10);

                response.add(weaponsLocation);

                //todo hasta aca

                drawLocationsOnMap(response);

                cancelDialog();

            }
        });
    }

    public void showLoadingDialog(){
        if (dialog == null) {
            dialog = new Dialog(this, R.style.Theme_Transparent);
            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loading_dialog);
            ((ProgressBar) dialog.findViewById(R.id.dialog_progress)).getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.app_color), PorterDuff.Mode.MULTIPLY);
        }
        dialog.show();
    }

    public void cancelDialog(){
        dialog.dismiss();
    }


    private CircleOptions getCircle(Location location, int radius){
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
    private void drawLocationsOnMap(List<WeaponsLocation> locations){
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
            registerProximityReceiver();

            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude())).title(location.getCode()));

            builder.include(marker.getPosition());

            requestCode++;
        }


        LatLngBounds bounds = builder.build();

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()); // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cu);
    }

    private void registerProximityReceiver(){
        if(!receiverRegistered) {
            IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
            proximityReceiver = new ProximityIntentReceiver();
            registerReceiver(proximityReceiver, filter);
            receiverRegistered = true;
        }
    }


    private void setRetryButton(){
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

    //and then add a proximity alert to it calling the following method:
    // Proximity alert
    private void addProximityAlert(double lat, double lng, int radius, int id, String locationName){
        Intent intent = new Intent(PROX_ALERT_INTENT);
        intent.putExtra("location", locationName);
        PendingIntent proximityIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        locationManager.addProximityAlert(lat, lng, radius, EXPIRATION, proximityIntent);

//        IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
//        registerReceiver(new ProximityIntentReceiver(), filter);

    }

//    Then you need to add a proximity alert listener to the map:
//    IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
//    registerReceiver(new ProximityIntentReceiver(), filter);

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
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
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
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(proximityReceiver != null){
            unregisterReceiver(proximityReceiver);
        }
    }
}
