package com.example.gpsmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class TrackerList extends AppCompatActivity {

    private static final String TAG = "G53MDP";

    private ListView lv;
    private String selectedRecord;
    ArrayList<Tracker> allRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_list);

        //create list view
        setListViewAdapter();
    }

    //set up list view
    private void setListViewAdapter() {

        //database handler
        final MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);

        //array list to hold recipe titles
        allRecord = new ArrayList<Tracker>();

        //call find all records function to check all records in database
        allRecord = dbHandler.findAllRecords();

        lv = (ListView) findViewById(R.id.RecordListView);

        //if retrieved records is null then show 'No records found'
        if(allRecord == null) {
            ArrayList<String> noRecord = new ArrayList<String>();
            noRecord.add("No records found");

            lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, noRecord));
            lv.setOnItemClickListener(null);    //unregister the onItemClickListener
        }else{

            //reverse the entire list of records, so that the latest record is shown
            Collections.reverse(allRecord);

            //set list view
            lv.setAdapter(new ArrayAdapter<Tracker>(this, android.R.layout.simple_list_item_1, allRecord));

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {

                    //get the selected list item integer
                    Tracker selectedRecord = allRecord.get(myItemInt);

                    //pass intent to Tracker Record class
                    Intent intent = new Intent(TrackerList.this, TrackerRecord.class);

                    //pass the selected record ID to Tracker Record class
                    intent.putExtra("myRecord", selectedRecord.getID());
                    startActivity(intent);
                }
            });
        }
    }

    //Update the Tracker Records Title List when on resume
    @Override
    public void onResume() {
        super.onResume();
        setListViewAdapter();
    }
}
