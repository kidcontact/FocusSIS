package com.slensky.focussis.data;

import com.google.api.services.calendar.model.Event;

import org.joda.time.DateTime;

/**
 * Created by slensky on 4/25/18.
 */

public interface GoogleCalendarEvent {
    public DateTime getStart();
    public DateTime getEnd();
    public Event toGoogleCalendarEvent();
}
