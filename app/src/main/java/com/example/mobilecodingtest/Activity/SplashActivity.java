package com.example.mobilecodingtest.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.view.Window;

import com.example.mobilecodingtest.R;

/**
 * Created by Diego on 07/05/2016.
 */
public class SplashActivity extends Activity {

    private static final int CONNECTION_CODE = 123;
    private static final int GPS_CODE = 456;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /**
         * Show app logo for 1 second and then go to @MapActivity
          */
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                checkConnectionAndGPS();

            }
        }, SPLASH_TIME_OUT);

    }

    /**
     * Checks if device has internet connection. If not, show a dialog to open settings
     */
    public Boolean hasConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void goToMap(){
        Intent i = new Intent(SplashActivity.this, MapActivity.class);

        startActivity(i);

        finish();
    }

    private void showNoConnectionDialog(){
        new AlertDialog.Builder(this)
                .setTitle("SIN CONEXIÓN")
                .setMessage("Revisa que tienes conexión a internet para continuar")
                .setPositiveButton("Configuración", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), CONNECTION_CODE);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private Boolean isGPSOn() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void openGPSSettings(){
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(callGPSSettingIntent, GPS_CODE);
    }

    private void showGPSDialog(){
        new AlertDialog.Builder(this)
                .setTitle("UBICACIÓN")
                .setMessage("Revisa que tienes la ubicación de tu dispositivo encendida para continuar")
                .setPositiveButton("Configuración", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openGPSSettings();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_map)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case CONNECTION_CODE:
                checkConnectionAndGPS();
                break;

            case GPS_CODE:
                checkGPS();
                break;
        }
    }

    private void checkConnectionAndGPS(){
        if(hasConnection()){
            checkGPS();
        }else {
            //insist on turning on connection
            showNoConnectionDialog();
        }
    }

    /**
     * Checks if GPS is on. If not, shows a dialog to open settings
     */
    private void checkGPS(){
        if(isGPSOn()) {
            goToMap();
        }else {
            showGPSDialog();
        }
    }
}
