package com.example.gpsmap;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TrackerService extends Service {

    Intent notificationIntent;
    NotificationCompat.Builder notifyBuilder;

    private final IBinder myBinder = new TrackerBinder();

    private final String tag = "G53MDP";

    private LatLng currentMarker, previousMarker = null;
    private double[] curLocationInfo = new double[2];
    private ArrayList<LatLng> latlang;
    private float totalDistance = 0;
    Date startTime;
    private boolean startTracking = false;

    private Intent locationIntent;

    LocationManager locationManager;
    MyLocationListener locationListener;

    @Override
    public void onCreate() {

        //create notification to show when tracker service is running
        notificationIntent = new Intent(this, MapsActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //notification builder
        notifyBuilder = new NotificationCompat.Builder(this)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Running Tracker")
                .setContentText("Tracking run...");

        //start a foreground service to provide a notification for the status bar,
        //the notification cannot be dismissed unless service is stopped or removed
        startForeground(1, notifyBuilder.build());
    }

    //get service in Tracker Binder
    public class TrackerBinder extends Binder {
        TrackerService getService() {
            return TrackerService.this;
        }
    }

    //implement the location listener
    private class MyLocationListener implements LocationListener {

        long curTime;

        @Override
        public void onLocationChanged(Location location) {

            curLocationInfo[0] = location.getLatitude();
            curLocationInfo[1] = location.getLongitude();
            curTime = location.getTime();

            //set action as com.example.gpsmap.LOCATION_RECEIVER
            locationIntent = new Intent();
            locationIntent.setAction("com.example.gpsmap.LOCATION_RECEIVER");

            //send the broadcast, current location information included
            locationIntent.putExtra("myLocation", curLocationInfo);
            sendBroadcast(locationIntent);
            Log.i(tag, "Normal Tracking in Location listener");

            //if the start tracking button is pressed
            //store the latitude & longitude, set distance and set previous marker
            if(startTracking){
                currentMarker = new LatLng(curLocationInfo[0], curLocationInfo[1]);
                latlang.add(currentMarker);
                if(previousMarker != null)
                    setDistance();
                previousMarker = currentMarker;
                Log.i(tag, "Start Tracking Button Pressed in Location listener");
                Log.i(tag, "LatLng length :"+ latlang.size());
            }

            //Log.d(tag, "My onChange location : " + location.getLatitude() + " " + location.getLongitude());
            //Log.i(tag, "Location.getTime :"+curTime);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(tag, "onStatusChanged: " + provider + " " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(tag, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            locationManager = null;
            locationListener = null;
            Log.d(tag, "onProviderDisabled: " + provider);
        }
    }


    //service on bind to Maps Activity
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(tag, "Service onBind");
        startTime = Calendar.getInstance().getTime();

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new TrackerService.MyLocationListener();

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5, // minimum time interval between updates
                    5, // minimum distance between updates, in metres
                    locationListener);
        } catch (SecurityException e) {
            Log.d(tag, e.toString());
        }

        return myBinder;        //return reference to the current service object instance
    }

    //initiate when start tracking button is pressed
    public void startRunningService(){
        startTracking = true;
        latlang = new ArrayList<LatLng>();
        totalDistance = 0;
        previousMarker = null;
    }

    //when stop tracking button is pressed
    public void stopRunningService(){
        startTracking = false;
    }

    //calculate the travelled distance in Metres using distanceTo()
    private void setDistance(){
        Location loc1 = new Location("");
        loc1.setLatitude(currentMarker.latitude);
        loc1.setLongitude(currentMarker.longitude);
        Log.i(tag, "Loc1 latlng: "+ currentMarker.latitude+", "+currentMarker.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(previousMarker.latitude);
        loc2.setLongitude(previousMarker.longitude);
        Log.i(tag, "Loc1 latlng: "+ previousMarker.latitude+", "+previousMarker.longitude);

        float distanceInMeters = loc1.distanceTo(loc2) ;
        Log.i(tag, "Distance in Km: "+ distanceInMeters);

        totalDistance += distanceInMeters;
        Log.i(tag, "Final Distance: "+ totalDistance);
    }

    //get travelled distance
    public float getDistance(){
        return totalDistance;
    }

    //get the total travelled distance
    public float getFinalDistance(){
        float distanceReturn = totalDistance;
        totalDistance = 0;
        previousMarker = currentMarker;
        return distanceReturn;
    }

    //get all latitude & longitude in form of string
    public String getLatlng(){

        String listString = "";
        for (LatLng s : latlang)
        {
            listString += s.toString() + "\t";
        }

        return listString;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        //stop updating when the service is stopped
        locationManager.removeUpdates(locationListener);
        Log.d(tag, "onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "onDestroy");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(tag, "onRebind");
    }
}
