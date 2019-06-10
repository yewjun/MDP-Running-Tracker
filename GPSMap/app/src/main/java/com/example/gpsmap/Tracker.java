package com.example.gpsmap;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tracker {

    private int _id;
    private Date _startdatetime;
    private Date _enddatetime;
    private int _duration;
    private float _distance;
    private float _averagespeed;
    private String _coordinates;

    public Tracker(){

    }

    public Tracker(int id, Date startdatetime, Date enddatetime, int duration, float distance, float averagespeed, String coordinates){
        this._id = id;
        this._startdatetime = startdatetime;
        this._enddatetime = enddatetime;
        this._duration = duration;
        this._distance = distance;
        this._averagespeed = averagespeed;
        this._coordinates = coordinates;
    }

    public Tracker(Date startdatetime, Date enddatetime, int duration, float distance, float averagespeed, String coordinates){
        this._startdatetime = startdatetime;
        this._enddatetime = enddatetime;
        this._duration = duration;
        this._distance = distance;
        this._averagespeed = averagespeed;
        this._coordinates = coordinates;
    }

    //get & set ID
    public void setID(int id){
        this._id = id;
    }

    //get & set ID
    public int getID(){
        return this._id;
    }

    public void setStartdatetime(Date startdatetime){
        this._startdatetime = startdatetime;
    }

    public Date getStartdatetime(){
        return this._startdatetime;
    }

    public void setEnddatetime(Date enddatetime){
        this._enddatetime = enddatetime;
    }

    public Date getEnddatetime(){
        return this._enddatetime;
    }

    public void setDuration(int duration){
        this._duration = duration;
    }

    public int getDuration(){
        return this._duration;
    }

    public void setDistance(float distance){
        this._distance = distance;
    }

    public float getDistance(){
        return this._distance;
    }

    public void setAveragespeed(float averagespeed) { this._averagespeed = averagespeed; }

    public float getAveragespeed() { return this._averagespeed; }

    public void setCoordinates(String coordinates){
        this._coordinates = coordinates;
    }

    public String getCoordinates(){
        return this._coordinates;
    }

    //change to date format & return as string
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy h:mm a");

        String dateString = dateFormat.format(this._startdatetime);
        return dateString;
    }
}
