package com.slensky.focussis.data;

import org.joda.time.DateTime;

/**
 * Created by slensky on 5/23/17.
 */

public class Referral {

    private final DateTime creationDate;
    private final DateTime entryDate;
    private final DateTime lastUpdated;
    private final boolean display;
    private final int grade;
    private final String id;
    private final String name;
    private final boolean notificationSent;
    private final boolean processed;
    private final String school;
    private final int schoolYear;
    private final String teacher;
    private final String violation;
    private final String otherViolation;

    public Referral(DateTime creationDate, DateTime entryDate, DateTime lastUpdated, boolean display, int grade, String id, String name, boolean notificationSent, boolean processed, String school, int schoolYear, String teacher, String violation, String otherViolation) {
        this.creationDate = creationDate;
        this.entryDate = entryDate;
        this.lastUpdated = lastUpdated;
        this.display = display;
        this.grade = grade;
        this.id = id;
        this.name = name;
        this.notificationSent = notificationSent;
        this.processed = processed;
        this.school = school;
        this.schoolYear = schoolYear;
        this.teacher = teacher;
        this.violation = violation;
        this.otherViolation = otherViolation;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public DateTime getEntryDate() {
        return entryDate;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public boolean isDisplay() {
        return display;
    }

    public int getGrade() {
        return grade;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean getNotificationSent() {
        return notificationSent;
    }

    public boolean isProcessed() {
        return processed;
    }

    public String getSchool() {
        return school;
    }

    public int getSchoolYear() {
        return schoolYear;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getViolation() {
        return violation;
    }

    public String getOtherViolation() {
        return otherViolation;
    }

}
