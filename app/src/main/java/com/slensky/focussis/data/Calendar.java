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

    public Calendar(List<MarkingPeriod> markingPeriods, List<Integer> markingPeriodYears, int year, int month, List<CalendarEvent> events) {
        super(markingPeriods, markingPeriodYears);
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
