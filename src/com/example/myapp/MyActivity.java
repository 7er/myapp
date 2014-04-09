package com.example.myapp;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MyActivity extends ListActivity {
    private List<String> getEventList() {
        GregorianCalendar beginWindow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        beginWindow.setTime(new Date());
        beginWindow.roll(Calendar.DAY_OF_MONTH, false);

        GregorianCalendar endWindow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        endWindow.setTime(new Date());
        endWindow.roll(Calendar.DAY_OF_MONTH, true);

        String[] projection =
                new String[] {
                        CalendarContract.Instances._ID,
                        CalendarContract.Instances.BEGIN,
                        CalendarContract.Instances.END,
                        CalendarContract.Instances.EVENT_ID,
                        CalendarContract.Instances.TITLE
                };
        Cursor calCursor = CalendarContract.Instances.query(
                getContentResolver(),
                projection,
                beginWindow.getTimeInMillis(),
                endWindow.getTimeInMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm");
        ArrayList<String> result = new ArrayList<String>();
        if (calCursor.moveToFirst()) {
            do {
                long id = calCursor.getLong(0);
                GregorianCalendar begin = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                begin.setTimeInMillis(calCursor.getLong(1));
                GregorianCalendar end = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                end.setTimeInMillis(calCursor.getLong(2));
                String title = calCursor.getString(4);
                long eventId = calCursor.getLong(3);
                Cursor attendeeCursor = CalendarContract.Attendees.query(getContentResolver(), eventId, new String[] {CalendarContract.Attendees.ATTENDEE_EMAIL, CalendarContract.Attendees.ATTENDEE_RELATIONSHIP});
                String email = "";
                if (attendeeCursor.moveToFirst()) {
                    do {
                        if (attendeeCursor.getInt(1) == CalendarContract.Attendees.RELATIONSHIP_ORGANIZER) {
                            email = attendeeCursor.getString(0);
                        }
                    } while (attendeeCursor.moveToNext());
                }
                result.add(title + " " + formatter.format(begin.getTime()) + " " + formatter.format(end.getTime()) + " " + "organizer mail: " + email);
            } while (calCursor.moveToNext());
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> events = getEventList();
        events.add("Ragnvald");
        setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, events));
//        Intent intent = getIntent();
//        if (intent.getAction() == Intent.ACTION_VIEW) {
//            TextView text = (TextView) findViewById(R.id.text);
//            text.setText(intent.getData().toString());
//        }
    }


    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        super.onListItemClick(list, v, position, id);
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("sip:bnordlun.meet@cisco.com")));
    }
}
