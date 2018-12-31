package com.slensky.focussis.data;

import android.util.Log;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.slensky.focussis.util.SchoolSingleton;
import com.slensky.focussis.util.TermUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by slensky on 5/12/17.
 */

public class CalendarEventDetails implements GoogleCalendarEvent {

    private static final String TAG = "CalendarEventDetails";
    private final DateTime date;
    private final String id;
    private final String school;
    private final String title;
    private final CalendarEvent.EventType type;
    private final String notes;
    private final String courseDays;
    private final String courseName;
    private final String coursePeriod;
    private final String courseSection; // alternative to period, if the section is not numbered (e.g. advisory)
    private final String courseTeacher;
    private final ScheduleCourse.Term courseTerm;

    public CalendarEventDetails(DateTime date, String id, String school, String title, CalendarEvent.EventType type, String notes, String courseDays, String courseName, String coursePeriod, String courseSection, String courseTeacher, ScheduleCourse.Term courseTerm) {
        this.date = date;
        this.id = id;
        this.school = school;
        this.title = title;
        this.type = type;
        this.notes = notes;
        this.courseDays = courseDays;
        this.courseName = courseName;
        this.coursePeriod = coursePeriod;
        this.courseSection = courseSection;
        this.courseTeacher = courseTeacher;
        this.courseTerm = courseTerm;
    }

    public DateTime getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public String getSchool() {
        return school;
    }

    public String getTitle() {
        return title;
    }

    public CalendarEvent.EventType getType() {
        return type;
    }

    public String getNotes() {
        return notes;
    }

    public boolean hasNotes() {
        return notes != null;
    }

    public String getCourseDays() {
        return courseDays;
    }

    public String getCourseName() {
        return courseName;
    }

    public boolean hasPeriod() {
        return coursePeriod != null;
    }

    public String getCoursePeriod() {
        return coursePeriod;
    }

    public String getCourseSection() {
        return courseSection;
    }

    public String getCourseTeacher() {
        return courseTeacher;
    }

    public ScheduleCourse.Term getCourseTerm() {
        return courseTerm;
    }

    @Override
    public DateTime getStart() {
        if (type == CalendarEvent.EventType.ASSIGNMENT) {
            LocalTime periodStart = SchoolSingleton.getInstance().getSchool().getStartTimeOfPeriodOnDay(coursePeriod, date);
            return date.withTime(periodStart.getHourOfDay(), periodStart.getMinuteOfHour(), periodStart.getSecondOfMinute(), periodStart.getMillisOfSecond());
        }
        else {
            LocalTime schoolStart = SchoolSingleton.getInstance().getSchool().getStartTimeOfSchooldayOnDay(date);
            return date.withTime(schoolStart.getHourOfDay(), schoolStart.getMinuteOfHour(), schoolStart.getSecondOfMinute(), schoolStart.getMillisOfSecond());
        }
    }

    @Override
    public DateTime getEnd() {
        if (type == CalendarEvent.EventType.ASSIGNMENT) {
            LocalTime periodEnd = SchoolSingleton.getInstance().getSchool().getStopTimeOfPeriodOnDay(coursePeriod, date);
            return date.withTime(periodEnd.getHourOfDay(), periodEnd.getMinuteOfHour(), periodEnd.getSecondOfMinute(), periodEnd.getMillisOfSecond());
        }
        else {
            LocalTime schoolEnd = SchoolSingleton.getInstance().getSchool().getStopTimeOfSchooldayOnDay(date);
            return date.withTime(schoolEnd.getHourOfDay(), schoolEnd.getMinuteOfHour(), schoolEnd.getSecondOfMinute(), schoolEnd.getMillisOfSecond());
        }
    }

    @Override
    public Event toGoogleCalendarEvent() {
        if (type == CalendarEvent.EventType.ASSIGNMENT) {
            Event.Creator creator = new Event.Creator()
                    .setDisplayName(courseTeacher);
            Event event = new Event()
                    .setSummary(title)
                    .setCreator(creator)
                    .setLocation(SchoolSingleton.getInstance().getSchool().getFullName());

            String description = courseName;
            if (coursePeriod != null) {
                description += " - Period " + coursePeriod;
            } else {
                description += " - " + courseSection;
            }
            if (notes != null) {
                description += "\n" + notes;
            }
            event.setDescription(description);

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
        else {
            Event event = new Event()
                    .setSummary(title)
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

}
