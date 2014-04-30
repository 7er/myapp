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

class Meeting {
    public String displayName;
    public String sipAddress;
    public String toString() {
        return displayName;
    }
}

public class MyActivity extends ListActivity {
    private List<Meeting> getEventList() {
        GregorianCalendar beginWindow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        beginWindow.setTime(new Date(Integer.MIN_VALUE));
        beginWindow.roll(Calendar.DAY_OF_MONTH, false);

        GregorianCalendar endWindow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        endWindow.setTime(new Date(Integer.MAX_VALUE));
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
        ArrayList<Meeting> result = new ArrayList<Meeting>();
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
                String sipAddress = "";
                if (attendeeCursor.moveToFirst()) {
                    do {
                        if (attendeeCursor.getInt(1) == CalendarContract.Attendees.RELATIONSHIP_ORGANIZER) {
                            email = attendeeCursor.getString(0);
                            // split @ prefix + ".meet" + "@" + suffix
                            String[] prefixAndSuffix = email.split("@");
                            sipAddress = prefixAndSuffix[0] + ".meet@" + prefixAndSuffix[1];
                        }
                    } while (attendeeCursor.moveToNext());
                }
                Meeting meeting = new Meeting();
                meeting.displayName = title + " " + formatter.format(begin.getTime()) + " " + formatter.format(end.getTime()) + " " + "organizer mail: " + email;
                meeting.sipAddress = sipAddress;
                System.out.println("added " + sipAddress);
                result.add(meeting);
            } while (calCursor.moveToNext());
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Meeting> events = getEventList();
        System.out.println("got here");
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
