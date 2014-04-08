package com.example.myapp;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MyActivity extends Activity implements View.OnClickListener {
    private void fillEventList() {
        GregorianCalendar beginWindow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        beginWindow.setTime(new Date());
        beginWindow.roll(Calendar.DAY_OF_MONTH, false);

        GregorianCalendar endWindow = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        endWindow.setTime(new Date());
        endWindow.roll(Calendar.DAY_OF_MONTH, true);

        String[] projection =
                new String[]{
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
        if (calCursor.moveToFirst()) {
            do {
                long id = calCursor.getLong(0);
                GregorianCalendar begin = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                begin.setTimeInMillis(calCursor.getLong(1));
                GregorianCalendar end = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                end.setTimeInMillis(calCursor.getLong(2));
                String title = calCursor.getString(4);
                System.out.println(id + " " + formatter.format(begin.getTime()) + " " + formatter.format(end.getTime()) + " " + title);
            } while (calCursor.moveToNext());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button button = (Button) findViewById(R.id.browse);
        button.setOnClickListener(this);
        fillEventList();
        Intent intent = getIntent();
        if (intent.getAction() == Intent.ACTION_VIEW) {
            TextView text = (TextView) findViewById(R.id.text);
            text.setText(intent.getData().toString());
        }
    }


    @Override
    public void onClick(View v) {
        //new Intent(this, MyActivity.class);
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("sip:lys01-3-didrik2@cisco.com")));
        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.python.com")));
    }
}
