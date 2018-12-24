package com.slensky.focussis.data;

import org.joda.time.DateTime;

/**
 * Created by slensky on 5/24/17.
 */

public class AbsencePeriod {

    private final DateTime lastUpdated;
    private final String lastUpdatedBy;
    private final String name;
    private final String period;
    private final Absences.Status status;
    private final String teacher;

    public AbsencePeriod(DateTime lastUpdated, String lastUpdatedBy, String name, String period, Absences.Status status, String teacher) {
        this.lastUpdated = lastUpdated;
        this.lastUpdatedBy = lastUpdatedBy;
        this.name = name;
        this.period = period;
        this.status = status;
        this.teacher = teacher;
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

    public String getPeriod() {
        return period;
    }

    public Absences.Status getStatus() {
        return status;
    }

    public String getTeacher() {
        return teacher;
    }
}
