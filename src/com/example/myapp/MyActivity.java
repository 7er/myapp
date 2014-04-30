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
    private void debugCalendars() {
        Cursor calCursor = getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,   // The content URI of the words table
                new String[] {
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND,
                        CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                        CalendarContract.Events.DESCRIPTION,
                        CalendarContract.Events.ORGANIZER,
                        CalendarContract.Events.TITLE
                },
                null,
                null,
                null);

        if (calCursor.moveToFirst()) {
            do {
                System.out.println(calCursor.getString(0) + " " + calCursor.getString(1) + " " + calCursor.getString(2)
                        + "  " + calCursor.getString(3) + " " + calCursor.getString(4) + " " + calCursor.getString(5));
            } while (calCursor.moveToNext());
        }


    }
    private List<Meeting> getEventList() {
        GregorianCalendar beginWindow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        beginWindow.setTime(new Date());
        beginWindow.roll(Calendar.DAY_OF_YEAR, false);

        GregorianCalendar endWindow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        endWindow.setTime(new Date());
        endWindow.roll(Calendar.DAY_OF_YEAR, true);

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

//        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
//        ContentUris.appendId(builder, beginWindow.getTimeInMillis());
//        ContentUris.appendId(builder, endWindow.getTimeInMillis());
//        Cursor calCursor = getContentResolver().query(
//                builder.build(),
//                projection,
//                null, null,
//                "begin ASC");



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
                Cursor attendeeCursor = CalendarContract.Attendees.query(getContentResolver(), eventId, new String[] {CalendarContract.Attendees.ATTENDEE_EMAIL, CalendarContract.Attendees.ATTENDEE_RELATIONSHIP, CalendarContract.Attendees.ORGANIZER});
                String organizerEmail = "";
                String sipAddress = "";
                if (attendeeCursor.moveToFirst()) {
                    System.out.println("Attendee - EMAIL: " + attendeeCursor.getString(0) + " RELATIONSHIP: " + attendeeCursor.getString(1) + " ORGANIZER" + attendeeCursor.getString(2));
                    organizerEmail = attendeeCursor.getString(2);
                    // split @ prefix + ".meet" + "@" + suffix
                    String[] prefixAndSuffix = organizerEmail.split("@");
                    sipAddress = "sip:" + prefixAndSuffix[0] + ".meet@" + prefixAndSuffix[1];
                }
                Meeting meeting = new Meeting();
                meeting.displayName = title + " " + formatter.format(begin.getTime()) + " " + formatter.format(end.getTime()) + " " + "organizer mail: " + organizerEmail;
                meeting.sipAddress = sipAddress;
                System.out.println("added " + sipAddress + " description: " + meeting);
                result.add(meeting);
            } while (calCursor.moveToNext());
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //debugCalendars();
        System.out.println("before event list");
        List<Meeting> events = getEventList();

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
        Meeting meeting = (Meeting)list.getAdapter().getItem(position);
        System.out.println("About to call: " + meeting.sipAddress);
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(meeting.sipAddress)));
    }
}
