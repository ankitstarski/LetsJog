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

    // Constant value for 2 minutes
    private static final int TWO_MINUTES = 1000 * 60 * 2;


    private final long MIN_JOGGING_SPEED = 1;
    private final long MAX_JOGGING_SPEED = 3;

    private static Location last;

    private Context context;

    public LocationFinder(Context ctx) {
        context = ctx;
        last=null;
    }

    public static double getLat()
    {
        return last.getLatitude();
    }

    public static double getLon()
    {
        return last.getLongitude();
    }

    public static double getSpeed()
    {
        return last.getSpeed();
    }

    @Override
    public void onLocationChanged(Location location)
    {

        if(isBetterLocation(location,last)){
            last = location;
        }

        double lat,lon,speed;

        lat = getLat();
        lon = getLon();
        speed = getSpeed();

        Log.i("fos", "Location changed: "+lat+" "+lon+" "+speed);

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
            notificationIntent.putExtra("fragment_number",1);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

            int SERVER_DATA_RECEIVED = 1;
            notificationManager.notify(SERVER_DATA_RECEIVED, notification);
        }

    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 100;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
