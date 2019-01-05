package com.slensky.focussis.data;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 5/24/17.
 */

public class Absences extends MarkingPeriodPage {

    private static final String TAG = "Absences";
    private final List<AbsenceDay> days;
    private final double daysPossible;
    private final double daysAbsent;
    private final double daysAbsentPercent;
    private final double daysAttended;
    private final double daysAttendedPercent;
    private final int periodsAbsent;
    private final int periodsAbsentUnexcused;
    private final int periodsAbsentExcused;
    private final int periodsDismissed; // NOT ALWAYS PRESENT
    private final int periodsOtherMarks;
    private final int periodsLate;
    private final int periodsTardy;
    private final int periodsMisc;
    private final int periodsOffsite;
    private final int daysPartiallyAbsent;
    private final int daysAbsentExcused;
    private final int daysOtherMarks;

    public enum Status {
        UNSET,
        PRESENT,
        HALF_DAY,
        ABSENT,
        DISMISSED,
        EXCUSED,
        LATE,
        TARDY,
        MISC,
        OFFSITE
    }

    public Absences(List<MarkingPeriod> markingPeriods, List<Integer> markingPeriodYears, List<AbsenceDay> days, double daysPossible, double daysAbsent, double daysAbsentPercent, double daysAttended, double daysAttendedPercent, int periodsAbsent, int periodsAbsentUnexcused, int periodsAbsentExcused, int periodsDismissed, int periodsOtherMarks, int periodsLate, int periodsTardy, int periodsMisc, int periodsOffsite, int daysPartiallyAbsent, int daysAbsentExcused, int daysOtherMarks) {
        super(markingPeriods, markingPeriodYears);
        this.days = days;
        this.daysPossible = daysPossible;
        this.daysAbsent = daysAbsent;
        this.daysAbsentPercent = daysAbsentPercent;
        this.daysAttended = daysAttended;
        this.daysAttendedPercent = daysAttendedPercent;
        this.periodsAbsent = periodsAbsent;
        this.periodsAbsentUnexcused = periodsAbsentUnexcused;
        this.periodsAbsentExcused = periodsAbsentExcused;
        this.periodsDismissed = periodsDismissed;
        this.periodsOtherMarks = periodsOtherMarks;
        this.periodsLate = periodsLate;
        this.periodsTardy = periodsTardy;
        this.periodsMisc = periodsMisc;
        this.periodsOffsite = periodsOffsite;
        this.daysPartiallyAbsent = daysPartiallyAbsent;
        this.daysAbsentExcused = daysAbsentExcused;
        this.daysOtherMarks = daysOtherMarks;
    }

    public static Status stringtoStatus(String status) {
        switch (status) {
            case "absent":
                return Status.ABSENT;
            case "present":
                return Status.PRESENT;
            case "half_day":
                return Status.HALF_DAY;
            case "excused":
                return Status.EXCUSED;
            case "late":
                return Status.LATE;
            case "tardy":
                return Status.TARDY;
            case "offsite":
                return Status.OFFSITE;
            default:
                return Status.MISC;
        }
    }

    public List<AbsenceDay> getDays() {
        return days;
    }

    public double getDaysPossible() {
        return daysPossible;
    }

    public double getDaysAbsent() {
        return daysAbsent;
    }

    public double getDaysAbsentPercent() {
        return daysAbsentPercent;
    }

    public double getDaysAttended() {
        return daysAttended;
    }

    public double getDaysAttendedPercent() {
        return daysAttendedPercent;
    }

    public int getPeriodsAbsent() {
        return periodsAbsent;
    }

    public int getPeriodsOtherMarks() {
        return periodsOtherMarks;
    }

    public int getPeriodsLate() {
        return periodsLate;
    }

    public int getPeriodsTardy() {
        return periodsTardy;
    }

    public int getPeriodsMisc() {
        return periodsMisc;
    }

    public int getPeriodsOffsite() {
        return periodsOffsite;
    }

    public int getDaysPartiallyAbsent() {
        return daysPartiallyAbsent;
    }

    public int getPeriodsAbsentUnexcused() {
        return periodsAbsentUnexcused;
    }

    public int getPeriodsAbsentExcused() {
        return periodsAbsentExcused;
    }

    public int getDaysAbsentExcused() {
        return daysAbsentExcused;
    }

    public int getDaysOtherMarks() {
        return daysOtherMarks;
    }

}
