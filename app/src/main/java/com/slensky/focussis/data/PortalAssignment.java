package com.slensky.focussis.data;

import android.support.annotation.NonNull;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.slensky.focussis.util.SchoolSingleton;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * Created by slensky on 4/14/17.
 */

public class PortalAssignment implements Comparable<PortalAssignment>, GoogleCalendarEvent {

    private final String name;
    private final DateTime due;

    // used for creating google calendar event
    private final String courseName;
    private final String coursePeriod;
    private final boolean isAdvisory;
    private final String courseTeacher;
    private final String courseTeacherEmail;

    // only applies to saved custom assignments
    // this information is not normally present on assignments in the portal
    private String description;
    private boolean descriptionSet;
    private boolean customAssignment;

    public PortalAssignment(String name, DateTime due, String courseName, String coursePeriod, boolean isAdvisory, String courseTeacher, String courseTeacherEmail) {
        this.name = name;
        this.due = due;
        this.courseName = courseName;
        this.coursePeriod = coursePeriod;
        this.isAdvisory = isAdvisory;
        this.courseTeacher = courseTeacher;
        this.courseTeacherEmail = courseTeacherEmail;
    }

    public String getName() {
        return name;
    }

    public DateTime getDue() {
        return due;
    }

    @Override
    public int compareTo(@NonNull PortalAssignment assignment) {
        if (due.compareTo(assignment.due) != 0) {
            return due.compareTo(assignment.due);
        }
        return name.compareTo(assignment.name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionSet = true;
    }

    public boolean isDescriptionSet() {
        return descriptionSet;
    }

    public boolean isCustomAssignment() {
        return customAssignment;
    }

    public void setCustomAssignment(boolean customAssignment) {
        this.customAssignment = customAssignment;
    }

    @Override
    public DateTime getStart() {
        LocalTime periodStart = SchoolSingleton.getInstance().getSchool().getStartTimeOfPeriodOnDay(coursePeriod, due);
        return due.withTime(periodStart.getHourOfDay(), periodStart.getMinuteOfHour(), periodStart.getSecondOfMinute(), periodStart.getMillisOfSecond());
    }

    @Override
    public DateTime getEnd() {
        LocalTime periodEnd = SchoolSingleton.getInstance().getSchool().getStopTimeOfPeriodOnDay(coursePeriod, due);
        return due.withTime(periodEnd.getHourOfDay(), periodEnd.getMinuteOfHour(), periodEnd.getSecondOfMinute(), periodEnd.getMillisOfSecond());
    }

    @Override
    public Event toGoogleCalendarEvent() {
        Event.Creator creator = new Event.Creator()
                .setDisplayName(courseTeacher)
                .setEmail(courseTeacherEmail);
        Event event = new Event()
                .setSummary(name)
                .setCreator(creator)
                .setLocation(SchoolSingleton.getInstance().getSchool().getFullName());

        String description = courseName;
        if (coursePeriod != null) {
            description += " - Period " + coursePeriod;
        }
        if (this.description != null) {
            description += "\n" + this.description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PortalAssignment that = (PortalAssignment) o;

        if (customAssignment != that.customAssignment) return false;
        if (!name.equals(that.name)) return false;
        if (!due.equals(that.due)) return false;
        if (!courseName.equals(that.courseName)) return false;
        if (!courseTeacher.equals(that.courseTeacher)) return false;
        if (!courseTeacherEmail.equals(that.courseTeacherEmail)) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + due.hashCode();
        result = 31 * result + courseName.hashCode();
        result = 31 * result + courseTeacher.hashCode();
        result = 31 * result + courseTeacherEmail.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (customAssignment ? 1 : 0);
        return result;
    }
}
