package com.example.gpsmap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TrackerDetail extends AppCompatActivity {

    private final String tag = "G53MDP";

    TextView showLongestDistance, showFastestSpeed, showTotalTime, showTotalDistance, showAverageSpeed;
    ArrayList<Tracker> allRecord;
    private float longestDistance, fastestSpeed ;
    private int todayTotalDuration, countToday;
    private float todayTotalDistance, todayTotalSpeed;

    SimpleDateFormat dateFormat;
    Calendar calender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_detail);

        showLongestDistance = (TextView) findViewById(R.id.showLongestDistance);
        showFastestSpeed = (TextView) findViewById(R.id.showFastestSpeed);
        showTotalTime = (TextView) findViewById(R.id.showTotalTime);
        showTotalDistance = (TextView) findViewById(R.id.showTotalDistance);
        showAverageSpeed = (TextView) findViewById(R.id.showAverageSpeed);

        dateFormat = new SimpleDateFormat("yyyyMMdd");    //date format
        calender = Calendar.getInstance();
        longestDistance = 0;
        fastestSpeed = 0;
        todayTotalDistance = 0;
        todayTotalDuration = 0;
        todayTotalSpeed = 0;
        countToday = 0;

        //database handler
        final MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);

        //array list to hold recipe titles
        allRecord = new ArrayList<Tracker>();

        //call find all recipe function to check database
        allRecord = dbHandler.findAllRecords();

        Log.d(tag, String.valueOf(allRecord));

        //check if the record retrieved is null or not
        if(String.valueOf(allRecord) != "null") {

            //get the longest distance, fastest speed, today's total distance, today's total duration and today's total speed
            for (Tracker record : allRecord) {
                if (record.getDistance() > longestDistance) {
                    longestDistance = record.getDistance();
                }

                if (record.getAveragespeed() > fastestSpeed) {
                    fastestSpeed = record.getAveragespeed();
                }

                if (dateFormat.format(record.getStartdatetime()).equals(dateFormat.format(Calendar.getInstance().getTime()))) {
                    todayTotalDistance += record.getDistance();
                    todayTotalDuration += record.getDuration();
                    todayTotalSpeed += record.getAveragespeed();
                    countToday++;
                }
            }
        }

        //calculate the average speed
        float averageSpeed = todayTotalSpeed/countToday;

        //if retrieved records is null, print 'No Data'
        if(String.valueOf(allRecord) != "null") {
            showLongestDistance.setText(String.format("%.2f", longestDistance) + " km");
            showFastestSpeed.setText(String.format("%.2f", fastestSpeed) + " km/h");
            showTotalDistance.setText(String.format("%.2f", todayTotalDistance) + " km");
            showTotalTime.setText(convertToTimeString(todayTotalDuration));
            showAverageSpeed.setText(String.format("%.2f", averageSpeed) + " km/h");
        }else{
            showLongestDistance.setText("No Data");
            showFastestSpeed.setText("No Data");
            showTotalDistance.setText("No Data");
            showTotalTime.setText("No Data");
            showAverageSpeed.setText("No Data");
        }
    }

    //convert and return as HH:mm:ss form of string
    private String convertToTimeString(int count){

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
            secondInString = "0 sec";
        }

        //return string in form HH:mm:ss
        return hourInString + minuteInString + secondInString;
    }

}
