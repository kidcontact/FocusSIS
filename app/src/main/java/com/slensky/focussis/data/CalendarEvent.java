package com.slensky.focussis.data;

import org.joda.time.DateTime;

/**
 * Created by slensky on 5/8/17.
 */

public class CalendarEvent {

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

}
