package com.example.ankit.letsjog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ankit on 1/12/15.
 */
public class LocationFinder implements LocationListener {

    private final long MIN_JOGGING_SPEED = 1;
    private final long MAX_JOGGING_SPEED = 3;

    private static double lat =0.0;
    private static double lon = 0.0;
    private static double alt = 0.0;
    private static double speed = 0.0;

    private Context context;

    public LocationFinder(Context ctx) {
        context = ctx;
    }

    public static double getLat()
    {
        return lat;
    }

    public static double getLon()
    {
        return lon;
    }

    public static double getAlt()
    {
        return alt;
    }

    public static double getSpeed()
    {
        return speed;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        lat = location.getLatitude();
        lon = location.getLongitude();
        alt = location.getAltitude();
        speed = location.getSpeed();
        Log.i("fos",lat+", "+lon+", "+speed);
        //MainActivity.printLoc(lat+", "+lon+", "+speed);

        if(speed>=MIN_JOGGING_SPEED && speed <= MAX_JOGGING_SPEED) {

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int icon = R.drawable.ic_launcher;
            CharSequence notificationText = "Your coords: " + lat + ", " + lon + " speed: " + speed;
            long meow = System.currentTimeMillis();

            Notification notification = new Notification(icon, notificationText, meow);


            CharSequence contentTitle = "Seems Like you are Jogging";
            CharSequence contentText = "Would You like to share a Song to other Joggers";
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

            int SERVER_DATA_RECEIVED = 1;
            notificationManager.notify(SERVER_DATA_RECEIVED, notification);
        }

    }

    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
