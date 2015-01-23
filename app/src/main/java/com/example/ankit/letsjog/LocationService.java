package com.example.ankit.letsjog;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
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
    private Location location;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in m
    private boolean canGetLocation;

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


        try {
            locationFinder = new LocationFinder(this);
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            if (!isGPSEnabled && !isNetworkEnabled) {
                // none available
                Toast.makeText(this, "Neither GPS nor network is available right now...", Toast.LENGTH_LONG).show();

            } else {
                this.canGetLocation = true;
                Log.i("fos",isGPSEnabled+" "+isNetworkEnabled);
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES_NETWORK,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationFinder);
                    Log.d("fos", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES_GPS,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationFinder);
                    Log.d("fos", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }

                }
                if(location!=null){
                    locationFinder.onLocationChanged(location);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("fos","Location Service Error");
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_LONG).show();
        }
        Log.i("fos","Location Service Created");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return Service.START_STICKY;
    }

    private  void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
