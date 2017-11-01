package org.kidcontact.focussis.data;

import org.joda.time.DateTime;

/**
 * Created by slensky on 5/24/17.
 */

public class AbsencePeriod {

    private final String days;
    private final DateTime lastUpdated;
    private final String lastUpdatedBy;
    private final String name;
    private final int period;
    private final Absences.Status status;
    private final String teacher;

    public AbsencePeriod(String days, DateTime lastUpdated, String lastUpdatedBy, String name, int period, Absences.Status status, String teacher) {
        this.days = days;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedBy = lastUpdatedBy;
        this.name = name;
        this.period = period;
        this.status = status;
        this.teacher = teacher;
    }

    public String getDays() {
        return days;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public String getName() {
        return name;
    }

    public int getPeriod() {
        return period;
    }

    public Absences.Status getStatus() {
        return status;
    }

    public String getTeacher() {
        return teacher;
    }
}
