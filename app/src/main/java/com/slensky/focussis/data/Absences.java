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
        EXCUSED,
        LATE,
        TARDY,
        MISC,
        OFFSITE
    }


    public Absences(JSONObject absencesJSON) {
        super(absencesJSON);
        double daysPossible = 0;
        double daysAbsent = 0;
        double daysAbsentPercent = 0;
        double daysAttended = 0;
        double daysAttendedPercent = 0;
        int periodsAbsent = 0;
        int periodsAbsentUnexcused = 0;
        int periodsAbsentExcused = 0;
        int periodsOtherMarks = 0;
        int periodsLate = 0;
        int periodsTardy = 0;
        int periodsMisc = 0;
        int periodsOffsite = 0;
        int daysPartiallyAbsent = 0;
        int daysAbsentExcused = 0;
        int daysOtherMarks = 0;
        days = new ArrayList<>();

        try {
            JSONObject daysJSON = absencesJSON.getJSONObject("absences");
            Iterator<?> keys = daysJSON.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject dayJSON = daysJSON.getJSONObject(key);

                try {
                    DateTime date = new DateTime(dayJSON.getString("date"));
                    Status status = Status.ABSENT;
                    if (dayJSON.getString("status").equals("present")) {
                        status = Status.PRESENT;
                    }
                    List<AbsencePeriod> periods = new ArrayList<>();
                    JSONObject periodsJSON = dayJSON.getJSONObject("periods");
                    Iterator<?> periodKeys = periodsJSON.keys();
                    while (periodKeys.hasNext()) {
                        String periodKey = (String) periodKeys.next();
                        JSONObject periodJSON = periodsJSON.getJSONObject(periodKey);

                        String days = null;
                        if (periodJSON.has("days")) {
                            days = periodJSON.getString("days");
                        }
                        DateTime lastUpdated = null;
                        if (periodJSON.has("last_updated")) {
                            lastUpdated = new DateTime(periodJSON.getString("last_updated"));
                        }
                        String lastUpdatedBy = null;
                        if (periodJSON.has("last_updated_by")) {
                            lastUpdatedBy = periodJSON.getString("last_updated_by");
                        }
                        String name = null;
                        if (periodJSON.has("name")) {
                            name = periodJSON.getString("name");
                        }
                        int period = 0;
                        try {
                            period = periodJSON.getInt("period");
                        } catch (JSONException e) {
                            // period isn't an int, which means it's an advisory period, which is 0
                        }
                        String teacher = null;
                        if (periodJSON.has("teacher")) {
                            teacher = periodJSON.getString("teacher");
                        }

                        Status periodStatus = Status.UNSET;
                        if (periodJSON.has("status")) {
                            periodStatus = stringtoStatus(periodJSON.getString("status"));
                        }

                        periods.add(new AbsencePeriod(days, lastUpdated, lastUpdatedBy, name, period, periodStatus, teacher));

                    }

                    days.add(new AbsenceDay(date, periods, status));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "error parsing day in absences");
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "absences not found in JSON");
        }

        try {
            daysPossible = absencesJSON.getDouble("days_possible");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "days_possible not found in JSON");
        }

        try {
            daysAbsent = absencesJSON.getDouble("days_absent");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "days_absent not found in JSON");
        }

        try {
            daysAbsentPercent = absencesJSON.getDouble("days_absent_percent");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "days_absent_percent not found in JSON");
        }

        try {
            daysAttended = absencesJSON.getDouble("days_attended");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "days_attended not found in JSON");
        }

        try {
            daysAttendedPercent = absencesJSON.getDouble("days_attended_percent");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "days_attended_percent not found in JSON");
        }

        try {
            periodsAbsent = absencesJSON.getInt("periods_absent");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "periods_absent not found in JSON");
        }

        try {
            periodsAbsentUnexcused = absencesJSON.getInt("periods_absent_unexcused");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "periods_absent_unexcused not found in JSON");
        }

        try {
            periodsAbsentExcused = absencesJSON.getInt("periods_absent_excused");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "periods_absent_excused not found in JSON");
        }

        try {
            periodsOtherMarks = absencesJSON.getInt("periods_other_marks");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "periods_other_marks not found in JSON");
        }

        try {
            periodsLate = absencesJSON.getInt("periods_late");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "periods_late not found in JSON");
        }

        try {
            periodsTardy = absencesJSON.getInt("periods_tardy");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "periods_tardy not found in JSON");
        }

        try {
            periodsMisc = absencesJSON.getInt("periods_misc");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "periods_misc not found in JSON");
        }

        try {
            periodsOffsite = absencesJSON.getInt("periods_offsite");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "periods_offsite not found in JSON");
        }

        try {
            daysPartiallyAbsent = absencesJSON.getInt("days_partially_absent");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "days_partially_absent not found in JSON");
        }

        try {
            daysAbsentExcused = absencesJSON.getInt("days_absent_excused");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "days_absent_excused not found in JSON");
        }

        try {
            daysOtherMarks = absencesJSON.getInt("days_other_marks");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "days_other_marks not found in JSON");
        }

        this.daysPossible = daysPossible;
        this.daysAbsent = daysAbsent;
        this.daysAbsentPercent = daysAbsentPercent;
        this.daysAttended = daysAttended;
        this.daysAttendedPercent = daysAttendedPercent;
        this.periodsAbsent = periodsAbsent;
        this.periodsAbsentUnexcused = periodsAbsentUnexcused;
        this.periodsAbsentExcused = periodsAbsentExcused;
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
