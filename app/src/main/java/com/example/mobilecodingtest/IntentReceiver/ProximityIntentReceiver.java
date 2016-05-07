package com.example.mobilecodingtest.IntentReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Diego on 07/05/2016.
 */
public class ProximityIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean entering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);

        Log.v("Mensaje", "Llego el proximity receiver. Entering = " + entering);

        if(entering){
            Toast.makeText(context, "PELIGRO! ENTRANDO EN RADIO DE FUEGO!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "SALIENDO DEL RADIO DE FUEGO :)", Toast.LENGTH_SHORT).show();
        }
    }
}
