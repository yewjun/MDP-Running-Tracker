package com.example.gpsmap.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.example.gpsmap.MyDBHandler;

public class MyContentProvider extends ContentProvider {

    private MyDBHandler myDB;

    private static final String AUTHORITY = "com.example.gpsmap.provider.MyContentProvider";
    private static final String TABLE_TRACKER = "tracker";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_TRACKER);

    public static final int RECORDS = 1;
    public static final int RECORDS_ID = 2;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //URI matcher
    static {
        sURIMatcher.addURI(AUTHORITY, TABLE_TRACKER, RECORDS);
        sURIMatcher.addURI(AUTHORITY, TABLE_TRACKER + "/#", RECORDS_ID);
    }

    public MyContentProvider() {
    }

    //delete operation
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);   //use the sUriMatcher to identify the URI type
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();  //obtain a reference to a writable instance of the underlying SQLite database
        int rowsDeleted = 0;
        switch (uriType) {
            case RECORDS:
                rowsDeleted = sqlDB.delete(MyDBHandler.TABLE_TRACKER,
                        selection,
                        selectionArgs);     //perform delete operation
                break;
            case RECORDS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(MyDBHandler.TABLE_TRACKER,
                            MyDBHandler.COLUMN_ID + "=" + id,
                            null);      //perform delete operation
                } else {
                    rowsDeleted = sqlDB.delete(MyDBHandler.TABLE_TRACKER,
                            MyDBHandler.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);         //perform delete operation
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " +
                        uri);   //throw an exception if the URI is not valid
        }
        getContext().getContentResolver().notifyChange(uri, null);      //notify the content resolver of the database change
        return rowsDeleted;     //return the number of rows deleted as a result of the operation
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //insert operation
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);       //use the sUriMatcher object to identify the URI type
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();      //obtain a reference to a writable instance of the underlying SQLite database
        long id = 0;
        switch (uriType) {
            case RECORDS:
                id = sqlDB.insert(MyDBHandler.TABLE_TRACKER, null, values);      //insert the data into the database table
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);     //throw an exception if the URI is not valid
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(TABLE_TRACKER + "/" + id);     //return the URI of the newly added table row
    }

    //set the database handler
    @Override
    public boolean onCreate() {
        myDB = new MyDBHandler(getContext(), null, null, 1);    //create an instance of the MyDBHandler class
        return false;
    }

    //query operation
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MyDBHandler.TABLE_TRACKER);
        int uriType = sURIMatcher.match(uri);   //use the sUriMatcher to identify the Uri type

        switch (uriType) {
            case RECORDS_ID:
                queryBuilder.appendWhere(MyDBHandler.COLUMN_ID + "=" + uri.getLastPathSegment());        //construct SQL query based on the criteria passed to the method
                break;
            case RECORDS:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");  //throw an exception if the URI is not valid
        }

        Cursor cursor = queryBuilder.query(myDB.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);     //execute the query operation on the database

        cursor.setNotificationUri(getContext().getContentResolver(), uri);       //notify the content resolver of the operation
        return cursor;      //return a Cursor object containing the results of the query
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
