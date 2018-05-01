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

    public CalendarEventDetails(JSONObject eventJSON) {
        DateTime date;
        String id;
        String school;
        String title;
        CalendarEvent.EventType type;
        String notes;
        String courseDays;
        String courseName;
        String coursePeriod;
        String courseSection = null;
        String courseTeacher;
        ScheduleCourse.Term courseTerm;

        try {
            date = new DateTime(eventJSON.getString("date"));
        } catch (JSONException e) {
            Log.e(TAG, "Error getting date");
            date = null;
        }

        try {
            id = eventJSON.getString("id");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting event id");
            id = null;
        }

        try {
            school = eventJSON.getString("school");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting school");
            school = null;
        }

        try {
            title = eventJSON.getString("title");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting title");
            title = null;
        }

        try {
            type = CalendarEvent.stringToEventType(eventJSON.getString("type"));
        } catch (JSONException e) {
            Log.e(TAG, "Error getting type");
            type = null;
        }

        if (type == CalendarEvent.EventType.ASSIGNMENT) {
            try {
                notes = eventJSON.getString("notes");
            } catch (JSONException e) {
                Log.w(TAG, "Error getting assignment notes, maybe it has none?");
                notes = null;
            }
            try {
                JSONObject courseJSON = eventJSON.getJSONObject("course");
                courseDays = courseJSON.getString("days");
                courseName = courseJSON.getString("name");
                if (courseJSON.has("period")) {
                    coursePeriod = courseJSON.getString("period");
                    courseSection = null;
                }
                else {
                    courseSection = courseJSON.getString("section");
                    coursePeriod = "0";
                }

                courseTeacher = courseJSON.getString("teacher");
                if (courseJSON.has("term")) {
                    courseTerm = TermUtil.stringToTerm(courseJSON.getString("term"));
                }
                else {
                    courseTerm = ScheduleCourse.Term.YEAR;
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error getting/parsing course object from assignment");
                courseDays = null;
                courseName = null;
                coursePeriod = "0";
                courseSection = null;
                courseTeacher = null;
                courseTerm = null;
            }
        }
        else {
            notes = null;
            courseDays = null;
            courseName = null;
            coursePeriod = null;
            courseTeacher = null;
            courseTerm = null;
        }

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
        return courseSection == null;
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
            if (courseSection == null) {
                description += " - Period " + coursePeriod;
            }
            else {
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
