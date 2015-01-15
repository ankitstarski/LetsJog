package com.example.ankit.gpstest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service {

    /**
     * GPS helping variables
     */
    private LocationManager locationManager;
    private LocationFinder locationFinder;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in m

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000;//1000*15; // ms


    public LocationService() {
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationManager = ( LocationManager ) getSystemService ( Context.LOCATION_SERVICE );
        locationFinder = new LocationFinder(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationFinder);
        Log.i("fos","Created");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
