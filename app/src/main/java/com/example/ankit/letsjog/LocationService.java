package com.example.ankit.letsjog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Google Location Strategies used
 * http://developer.android.com/guide/topics/location/strategies.html
 */

public class LocationService extends Service {

    /**
     * GPS helping variables
     */
    private LocationManager locationManager;
    private LocationFinder locationFinder;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in m

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES_GPS = 1000*60*5; // ms
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES_NETWORK = 1000*60; // ms


    public LocationService() {
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationManager = ( LocationManager ) getSystemService ( Context.LOCATION_SERVICE );
        locationFinder = new LocationFinder(this);


        // For GPS
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES_GPS,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationFinder);

        // For Network
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES_NETWORK,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationFinder);

        Log.i("fos","Location Service Created");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
