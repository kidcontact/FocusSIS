package com.slensky.focussis.data;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 5/8/17.
 * Assumes a year+month calendar JSON
 */

public class Calendar extends MarkingPeriodPage {

    private final static String TAG = Calendar.class.getName();
    private final int year;
    private final int month;
    private final List<CalendarEvent> events;

    public Calendar(JSONObject calendar) {
        super(calendar);

        int year;
        try {
            year = calendar.getInt("year");
        } catch (JSONException e) {
            year = -1;
            Log.e(TAG, "Year not found in calendar JSON");
        }

        int month;
        try {
            month = calendar.getInt("month");
        } catch (JSONException e) {
            month = -1;
            Log.e(TAG, "Month not found in calendar JSON");
        }

        List<CalendarEvent> events = new ArrayList<>();
        try {
            if (calendar.has("events")) {
                JSONObject eventsJSON = calendar.getJSONObject("events");
                Iterator<?> keys = eventsJSON.keys();

                while(keys.hasNext()) {
                    String key = (String) keys.next();
                    JSONObject eventJSON = eventsJSON.getJSONObject(key);

                    DateTime date = new DateTime(eventJSON.getString("date"));
                    String id = eventJSON.getString("id");
                    String name = eventJSON.getString("name");
                    String stype = eventJSON.getString("type");
                    CalendarEvent.EventType type = CalendarEvent.stringToEventType(stype);
                    events.add(new CalendarEvent(date, id, name, type));
                }
            }
            else {
                Log.w(TAG, "Calendar for year " + year + " and month " + month + " appears to have no events");
            }
        } catch (JSONException e) {
            events = null;
            Log.e(TAG, "Error parsing calendar events");
        }

        this.year = year;
        this.month = month;
        this.events = events;

    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public List<CalendarEvent> getEvents() {
        return events;
    }

}
