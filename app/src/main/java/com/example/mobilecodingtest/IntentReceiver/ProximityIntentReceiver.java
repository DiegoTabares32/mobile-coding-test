package com.example.mobilecodingtest.IntentReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.mobilecodingtest.Activity.MapActivity;
import com.example.mobilecodingtest.R;

/**
 * Created by Diego on 07/05/2016.
 */
public class ProximityIntentReceiver extends BroadcastReceiver {

    private static final String enteringMessage = "PELIGRO! ENTRANDO EN RADIO DE FUEGO DE ";

    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean entering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
        String locationName = "'"+ intent.getStringExtra("location") + "'";

        Log.v("Mensaje", "Llego el proximity receiver. Entering = " + entering);

        if(entering){

            showNotification(context, locationName);

        }else {
            Toast.makeText(context, "SALIENDO DEL RADIO DE FUEGO :)", Toast.LENGTH_SHORT).show();
        }

    }

    private void showNotification(Context context, String locationName){
        Intent notificationIntent = new Intent(context, MapActivity.class);

        /** Adding content to the notificationIntent, which will be displayed on
         * viewing the notification
         */
        notificationIntent.putExtra("content", enteringMessage + locationName);

        /** This is needed to make this intent different from its previous intents */
        notificationIntent.setData(Uri.parse("tel:/" + (int) System.currentTimeMillis()));

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        /** Getting the System service NotificationManager */
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        /** Configuring notification builder to create a notification */
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle("ALERTA!");
        bigTextStyle.bigText(enteringMessage + locationName);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setStyle(bigTextStyle)
                .setContentText(enteringMessage)
                .setContentTitle("ALERTA!")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setTicker(context.getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        /** Creating a notification from the notification builder */
        Notification notification = notificationBuilder.build();

        /** Sending the notification to system.
         * The first argument ensures that each notification is having a unique id
         * If two notifications share same notification id, then the last notification replaces the first notification
         * */
        nManager.notify((int)System.currentTimeMillis(), notification);

    }
}
