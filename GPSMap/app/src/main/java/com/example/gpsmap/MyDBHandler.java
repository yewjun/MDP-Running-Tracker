package com.example.gpsmap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.gpsmap.provider.MyContentProvider;

import java.util.ArrayList;
import java.util.Date;

public class MyDBHandler extends SQLiteOpenHelper {

    private ContentResolver myCR;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "trackerDB.db";
    public static final String TABLE_TRACKER = "tracker";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_STARTDATETIME = "startdatetime";
    public static final String COLUMN_ENDDATETIME = "enddatetime";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_AVERAGESPEED = "averagespeed";
    public static final String COLUMN_COORDINATES = "coordinates";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);

        //get content resolver
        myCR = context.getContentResolver();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create a table in database
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                TABLE_TRACKER + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_STARTDATETIME + " DATETIME," +
                COLUMN_ENDDATETIME + " DATETIME," +
                COLUMN_DURATION + " INTEGER," +
                COLUMN_DISTANCE + " FLOAT," +
                COLUMN_AVERAGESPEED + " FLOAT," +
                COLUMN_COORDINATES + " TEXT" +
                ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKER);
        onCreate(db);
    }

    //insert record to database
    public void addRecord(Tracker tracker) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_STARTDATETIME, tracker.getStartdatetime().toString());
        values.put(COLUMN_ENDDATETIME, tracker.getEnddatetime().toString());
        values.put(COLUMN_DURATION, tracker.getDuration());
        values.put(COLUMN_DISTANCE, tracker.getDistance());
        values.put(COLUMN_AVERAGESPEED, tracker.getAveragespeed());
        values.put(COLUMN_COORDINATES, tracker.getCoordinates());
        myCR.insert(MyContentProvider.CONTENT_URI, values);
    }

    //find record in database
    public Tracker findRecord(int id) {
        String[] projection = {COLUMN_ID,
                COLUMN_STARTDATETIME, COLUMN_ENDDATETIME, COLUMN_DURATION, COLUMN_DISTANCE, COLUMN_AVERAGESPEED, COLUMN_COORDINATES };
        String selection = COLUMN_ID + " = \"" + id + "\"";
        Cursor cursor = myCR.query(MyContentProvider.CONTENT_URI, projection, selection, null, null);
        Tracker tracker = new Tracker();
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            tracker.setID(Integer.parseInt(cursor.getString(0)));
            tracker.setStartdatetime(new Date(cursor.getString(1)));
            tracker.setEnddatetime(new Date(cursor.getString(2)));
            tracker.setDuration(Integer.parseInt(cursor.getString(3)));
            tracker.setDistance(cursor.getFloat(4));
            tracker.setAveragespeed(cursor.getFloat(5));
            tracker.setCoordinates(cursor.getString(6));

            cursor.close();
        } else {
            tracker = null;
        }
        return tracker;
    }

    //delete record in database
    public boolean deleteRecord(int id) {
        boolean result = false;
        String selection = COLUMN_ID + " = \"" + id + "\"";
        int rowsDeleted = myCR.delete(MyContentProvider.CONTENT_URI,
                selection, null);
        if (rowsDeleted > 0)
            result = true;
        return result;
    }

    //find all records in database
    public ArrayList<Tracker> findAllRecords() {
        //retrieve all records that exist in the "records" table
        ArrayList<Tracker> allRecords = new ArrayList<Tracker>();

        String[] projection = {COLUMN_ID,
                COLUMN_STARTDATETIME, COLUMN_ENDDATETIME, COLUMN_DURATION, COLUMN_DISTANCE, COLUMN_AVERAGESPEED, COLUMN_COORDINATES };

        Cursor cursor = myCR.query(MyContentProvider.CONTENT_URI, projection, null, null, null);

        if(cursor.moveToFirst()) {
            cursor.moveToFirst();

            do {
                Tracker tracker = new Tracker();

                tracker.setID(Integer.parseInt(cursor.getString(0)));
                tracker.setStartdatetime(new Date(cursor.getString(1)));
                tracker.setEnddatetime(new Date(cursor.getString(2)));
                tracker.setDuration(Integer.parseInt(cursor.getString(3)));
                tracker.setDistance(cursor.getFloat(4));
                tracker.setAveragespeed(cursor.getFloat(5));
                tracker.setCoordinates(cursor.getString(6));

                allRecords.add(tracker); //add the title from each row into the arrayList
            } while (cursor.moveToNext());

            cursor.close();

        } else {
            allRecords = null;
        }

        return allRecords;
    }

}
