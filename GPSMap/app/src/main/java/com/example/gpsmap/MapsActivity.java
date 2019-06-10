package com.example.gpsmap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

//Activity that shows a Google Map on screen
public class MapsActivity extends AppCompatActivity {

    private Timer timer;
    private Tracker record;

    private GoogleMap mMap;
    PolylineOptions myLines;

    private Boolean mLocationPermissionGranted = false;

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =1;

    private Intent mapIntent;
    TrackerService.TrackerBinder binder;
    TrackerService trackerService;
    boolean isBound = false;

    BroadcastReceiver receiver;

    private String tag = "G53MDP";
    private Integer count = 1;
    private double[] curLocationInfo = new double[2];
    private Boolean startTracking = false;
    private float averageSpeed;

    Date startTimeDate;
    Date endTimeDate;
    private Button startBtn;
    private TextView showStartTime, showEndTime, showDuration, showDistance, showSpeed;

    ArrayList<Polyline> lines = new ArrayList<Polyline>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        startBtn = (Button) findViewById(R.id.Start);
        showStartTime = (TextView) findViewById(R.id.showStartTime);
        showEndTime = (TextView) findViewById(R.id.showEndTime);
        showDuration = (TextView) findViewById(R.id.showDuration);
        showDistance = (TextView) findViewById(R.id.showTotalTime);
        showSpeed = (TextView) findViewById(R.id.showSpeed);

        //Ask for permission to access location
        //If permission granted, start binding service
        //If permission not granted, ask user for accessing location
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapIntent = new Intent(this, TrackerService.class);
            bindService(mapIntent, trackerConnection, Context.BIND_AUTO_CREATE);    //bind to the music service and establish connection

            configureReceiver();
        }else {
            getLocationPermission();
        }
    }

    //configure the receiver
    private void configureReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.gpsmap.LOCATION_RECEIVER");
        receiver = new mapReceiver();
        registerReceiver(receiver, filter);
    }

    //set broadcast receiver to get location information
    class mapReceiver extends BroadcastReceiver implements OnMapReadyCallback {

        Integer polylineCounter = 0;
        Marker currentLocMarker;
        LatLng currentLocation, prevLocation = null;

        @Override
        public void onReceive(Context context, Intent intent) {

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            if(mMap == null) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            }

            curLocationInfo = intent.getDoubleArrayExtra("myLocation");
            currentLocation = new LatLng(curLocationInfo[0], curLocationInfo[1]);
            Log.i(tag, String.valueOf(currentLocation));

            //show the route on the map
            //update the marker on the map
            if(mMap != null && prevLocation != null) {
                if (startTracking) {
                    currentLocMarker.setPosition(currentLocation);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    myLines.add(currentLocation);

                    Polyline polyline = mMap.addPolyline(myLines);

                    if(polyline != null){
                        lines.add(polyline);
                    }

                    polylineCounter++;

                } else {
                    currentLocMarker.setPosition(currentLocation);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                }
            }
            prevLocation = currentLocation;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            MarkerOptions marker = new MarkerOptions().position(currentLocation).title("Current Location");
            currentLocMarker = mMap.addMarker(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
        }
    }

    //ask permission to access location
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //if location permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){

                //bind to the map service and establish connection
                mapIntent = new Intent(this, TrackerService.class);
                bindService(mapIntent, trackerConnection, Context.BIND_AUTO_CREATE);

                configureReceiver();
            }
        }
    }

    //When Service Connection is connected and disconnected
    private ServiceConnection trackerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (TrackerService.TrackerBinder)service;
            trackerService = binder.getService();     //get reference to the current service object instance
            isBound = true;
            Log.d(tag, "connect to tracker service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            trackerService = null;
            isBound = false;
            Log.d(tag, "disconnect to service");
        }
    };

    //navigate to Tracker List Page
    public void toTrackerList(View view){
        Intent listIntent = new Intent(MapsActivity.this, TrackerList.class);
        startActivity(listIntent);
    }

    //navigate to Detail Page
    public void toDetailPage(View view){
        Intent detailIntent = new Intent(MapsActivity.this, TrackerDetail.class);
        startActivity(detailIntent);
    }

    //When start tracking OR stop tracking button clicked
    public void startTracking(View view){

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy h:mm a");
        String timeString = dateTimeFormat.format(currentTime);

        //start updating map marker, route, counting duration, speed, distance
        if(!startTracking){
            trackerService.startRunningService();
            startTimeDate = setStartTimeDate(currentTime);
            myLines = new PolylineOptions();

            startTracking = true;
            startBtn.setText("Stop Tracking");
            showStartTime.setText(timeString);
            showEndTime.setText("-- : -- : --");
            showDuration.setText("-- : -- : --");
            showDistance.setText("--- km");
            showSpeed.setText("--- km/h");
            timer=new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            showDuration.setText(myTimer());
                            float h = (float) count /60/60;
                            float km = trackerService.getDistance()/1000;
                            averageSpeed = km/h;
                            showSpeed.setText(String.format("%.2f", averageSpeed)+" km/h");
                            showDistance.setText(String.format("%.2f", km)+" km");
                            count++;
                        }
                    });
                }
            }, 1000, 1000);
            Toast.makeText(getApplicationContext(), "Start Tracking", Toast.LENGTH_SHORT).show();

         //set the stop time, clean route, update map marker, stop counting duration, speed and distance
         //insert recorded data into database
        }else{
            trackerService.stopRunningService();
            endTimeDate = setEndTimeDate(currentTime);

            if(lines != null){
                for (Polyline line : lines){
                    line.remove();
                }
            }

            lines.clear();

            startTracking = false;
            startBtn.setText("Start Tracking");
            showEndTime.setText(timeString);
            float finalDistance = trackerService.getFinalDistance()/1000;
            showDistance.setText(String.format("%.2f", finalDistance)+" km");

            //initiate database handler
            MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
            record = new Tracker(startTimeDate, endTimeDate, count-1, finalDistance, averageSpeed, trackerService.getLatlng());
            dbHandler.addRecord(record);
            Log.i(tag, "Added tracking record!");
            Toast.makeText(getApplicationContext(), "Tracking Recorded", Toast.LENGTH_SHORT).show();

            timer.cancel();
            count=1;
            Toast.makeText(getApplicationContext(), "Stop Tracking", Toast.LENGTH_SHORT).show();
            Log.i(tag, "Final LatLng Array"+ trackerService.getLatlng());
        }
    }

    private Date setStartTimeDate(Date _now){
        return _now;
    }

    private Date setEndTimeDate(Date _now){
        return _now;
    }

    //convert the count in second into HH:mm:ss form
    private String myTimer(){

        int newCount = count;
        String hourInString = "";
        String minuteInString = "";
        String secondInString = "";

        //convert hour, minute, second in millisecond
        int hour = 60*60;
        int minute = 60;
        int second = 1;

        //get the hour(s) and display
        if(newCount >= hour){
            hourInString = String.valueOf(newCount / hour) + ":";
            //store the remainder
            newCount = newCount % hour;
        }

        //get the minute(s) and display
        if(newCount >= minute){
            //if less than 10minutes, display a '0' infront
            if(newCount / minute < 10) {
                minuteInString = "0" + String.valueOf(newCount / minute) + ":";
                newCount = newCount % minute;
            }else{
                minuteInString = String.valueOf(newCount / minute) + ":";
                newCount = newCount % minute;
            }
            //if no minute, display '00'
        }else if(newCount < minute){
            minuteInString = "00:";
        }

        //get the second(s) and display
        if(newCount >= second){
            //if less than 10seconds, display a '0' infront
            if(newCount / second < 10) {
                secondInString = "0" + String.valueOf(newCount / second);
                newCount = newCount % second;
            }else{
                secondInString = String.valueOf(newCount / second);
                newCount = newCount % second;
            }
            //if no second, display '00'
        }else if(newCount < second){
            secondInString = "00";
        }

        //return string in form HH:mm:ss
        return hourInString + minuteInString + secondInString;
    }

    @Override
    public void onResume() {
        super.onResume();
        //get intent of the location from receiver
        IntentFilter filter = new IntentFilter("com.example.gpsmap.LOCATION_RECEIVER");
        Log.d(tag, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(tag, "onPause");
    }

    @Override
    public void onDestroy() {
        //stop service and unbind from service
        unbindService(trackerConnection);
        stopService(mapIntent);
        super.onDestroy();
        Log.d(tag, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(tag, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(tag, "onStop");
    }
}
