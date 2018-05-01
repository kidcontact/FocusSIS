package com.slensky.focussis.data;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.slensky.focussis.util.SchoolSingleton;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * Created by slensky on 4/14/17.
 */

public class PortalEvent implements GoogleCalendarEvent {

    private final String description;
    private final DateTime date;

    public PortalEvent(String description, DateTime date) {
        this.description = description;
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public DateTime getDate() {
        return date;
    }

    @Override
    public DateTime getStart() {
        LocalTime schoolStart = SchoolSingleton.getInstance().getSchool().getStartTimeOfSchooldayOnDay(date);
        return date.withTime(schoolStart.getHourOfDay(), schoolStart.getMinuteOfHour(), schoolStart.getSecondOfMinute(), schoolStart.getMillisOfSecond());
    }

    @Override
    public DateTime getEnd() {
        LocalTime schoolEnd = SchoolSingleton.getInstance().getSchool().getStopTimeOfSchooldayOnDay(date);
        return date.withTime(schoolEnd.getHourOfDay(), schoolEnd.getMinuteOfHour(), schoolEnd.getSecondOfMinute(), schoolEnd.getMillisOfSecond());
    }

    @Override
    public Event toGoogleCalendarEvent() {
        Event event = new Event()
                .setSummary(description)
                .setLocation(SchoolSingleton.getInstance().getSchool().getFullName());

        DateTime startDateTime = getStart();
        DateTime endDateTime = getEnd();

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startDateTime.toDate()));
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(endDateTime.toDate()));
        event.setEnd(end);

        return event;
    }
}
