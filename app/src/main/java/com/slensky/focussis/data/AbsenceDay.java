package com.slensky.focussis.data;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by slensky on 5/24/17.
 */

public class AbsenceDay {

    private final DateTime date;
    private final List<AbsencePeriod> periods;
    private final Absences.Status status;

    public AbsenceDay(DateTime date, List<AbsencePeriod> periods, Absences.Status status) {
        this.date = date;
        this.periods = periods;
        this.status = status;
    }

    public DateTime getDate() {
        return date;
    }

    public List<AbsencePeriod> getPeriods() {
        return periods;
    }

    public Absences.Status getStatus() {
        return status;
    }

}
