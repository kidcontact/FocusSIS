package com.slensky.focussis.data;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.slensky.focussis.util.SchoolSingleton;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * Created by slensky on 5/8/17.
 */

public class CalendarEvent implements GoogleCalendarEvent {

    private final DateTime date;
    private final String id;
    private final String name;

    public enum EventType {
        ASSIGNMENT,
        OCCASION
    }
    private final EventType type;

    public CalendarEvent(DateTime date, String id, String name, EventType type) {
        this.date = date;
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public DateTime getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EventType getType() {
        return type;
    }

    public static EventType stringToEventType(String eventStr) {
        if (eventStr.equals("occasion")) {
            return EventType.OCCASION;
        }
        else {
            return EventType.ASSIGNMENT;
        }
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
                .setSummary(name)
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
