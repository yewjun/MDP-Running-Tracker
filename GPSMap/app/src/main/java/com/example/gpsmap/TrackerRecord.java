package com.example.gpsmap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;

public class TrackerRecord extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PolylineOptions myLines = new PolylineOptions();
    Marker currentLocMarker;
    SimpleDateFormat simpleDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm aaa");

    private String tag = "G53MDP";

    TextView showStartTime, showEndTime, showDuration, showDistance, showSpeed;
    private int selectedRecord;
    private Tracker record;
    private String[] latlng;
    private int selectedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_record);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        showStartTime = (TextView) findViewById(R.id.showStartTime);
        showEndTime = (TextView) findViewById(R.id.showEndTime);
        showDuration = (TextView) findViewById(R.id.showDuration);
        showDistance = (TextView) findViewById(R.id.showTotalTime);
        showSpeed = (TextView) findViewById(R.id.showSpeed);

        //get string bundle from Tracker List
        Bundle bundle = getIntent().getExtras();
        selectedRecord = bundle.getInt("myRecord");

        //initiate database and use find record function to check database with similar selected record
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
        record = dbHandler.findRecord(selectedRecord);

        //set the text box to the data saved in database
        selectedID = record.getID();
        showStartTime.setText(simpleDate.format(record.getStartdatetime()));
        showEndTime.setText(simpleDate.format(record.getEnddatetime()));
        showDuration.setText(myTimer(record.getDuration()));
        showDistance.setText(String.format("%.2f",record.getDistance()) + " km");
        showSpeed.setText(String.format("%.2f",record.getAveragespeed()) + " km/h");

        Log.i(tag, "Coodinates: "+record.getCoordinates());
        latlng = record.getCoordinates().split("\t");
        Log.i(tag, "Size of array Latlng: "+ latlng.length);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //split the coordinate in string form retrieved from data into latitude and longitude
        for(int i=0; i<latlng.length; i++){

            String[] coordinate = latlng[i].split(",");

            String[] getLat = coordinate[0].split("\\(");
            String[] getLng = coordinate[1].split("\\)");

            double latitude = Double.parseDouble(getLat[1]);
            double longitude = Double.parseDouble(getLng[0]);
            LatLng location = new LatLng(latitude, longitude);
            Log.i(tag, "My LatLng :" + location);

            //add polyline to map
            myLines.add(location);
            mMap.addPolyline(myLines);

            //add start and end running marker to map
            if(i == 0){
                MarkerOptions startMarker = new MarkerOptions().position(location).title("Start Location");
                currentLocMarker = mMap.addMarker(startMarker);
                builder.include(startMarker.getPosition());
            }else if(i == latlng.length-1){
                MarkerOptions endMarker = new MarkerOptions().position(location).title("End Location");
                currentLocMarker = mMap.addMarker(endMarker);
                myLines.add(location);
                builder.include(endMarker.getPosition());
            }
        }

        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200,200, padding);
        mMap.moveCamera(cu);
    }

    //convert int into HH:mm:ss form of string
    private String myTimer(int count){

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
            hourInString = String.valueOf(newCount / hour) + " hr ";
            //store the remainder
            newCount = newCount % hour;
        }

        //get the minute(s) and display
        if(newCount >= minute){
            //if less than 10minutes, display a '0' infront
            if(newCount / minute < 10) {
                minuteInString = "0" + String.valueOf(newCount / minute) + " min";
                newCount = newCount % minute;
            }else{
                minuteInString = String.valueOf(newCount / minute) + " min";
                newCount = newCount % minute;
            }
            //if no minute, display '00'
        }

        //get the second(s) and display
        if(newCount >= second){
            //if less than 10seconds, display a '0' infront
            if(newCount / second < 10) {
                secondInString = "0" + String.valueOf(newCount / second) + " sec";
                newCount = newCount % second;
            }else{
                secondInString = String.valueOf(newCount / second) + " sec";
                newCount = newCount % second;
            }
            //if no second, display '00'
        }else if(newCount < second){
            secondInString = "0sec";
        }

        //return string in form HH:mm:ss
        return hourInString + minuteInString + secondInString;
    }

    //when delete button is pressed
    public void onDelete(View view){
        //initiate database handler
        final MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);

        //a message box will pop out to confirm with user when delete the recipe
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //if Yes button clicked, delete record function is called to delete selected record
                //if No button clicked, back to Tracker Record interface
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        boolean result = dbHandler.deleteRecord(selectedID);
                        if(result){
                            Log.i(tag, "Deleted Record!");
                            finish();
                        }
                        Log.i(tag, "YES button clicked!");
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        Log.i(tag, "NO button clicked!");
                        break;
                }
            }
        };

        //A alert message box with button Yes & No
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("Are you sure delete this record ?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
        Log.i(tag, "Alert Message Box Pop Up!");
    }
}
