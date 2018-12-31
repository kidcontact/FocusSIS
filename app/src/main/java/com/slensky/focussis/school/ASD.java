package com.slensky.focussis.school;

import android.util.Log;
import android.util.SparseArray;

import com.slensky.focussis.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by slensky on 4/25/18.
 */

public class ASD extends School {
    private static final String FOCUS_TLD = "https://focus.asdnh.org";
    private static final String NAME_SHORT = "ASDNH";
    private static final String NAME_FULL = "Academy for Science and Design";
    private static final String DOMAIN_NAME = "asdnh.org";

    private static final LocalTime schooldayStart = new LocalTime(8, 15);
    private static final LocalTime schooldayStop = new LocalTime(15, 25);

    private static final SparseArray<Map<String, LocalTime>> scheduleStartTimes;
    private static final SparseArray<Map<String, LocalTime>> scheduleStopTimes;
    private static final Map<String, LocalTime> sixthGradeScheduleStartTimes;
    private static final Map<String, LocalTime> sixthGradeScheduleStopTimes;
    private static final Map<String, LocalTime> oddDayScheduleStartTimes;
    private static final Map<String, LocalTime> evenDayScheduleStartTimes;
    private static final Map<String, LocalTime> wednesdayScheduleStartTimes;
    private static final Map<String, LocalTime> oddDayScheduleStopTimes;
    private static final Map<String, LocalTime> evenDayScheduleStopTimes;
    private static final Map<String, LocalTime> wednesdayScheduleStopTimes;

    // last updated for ASD during the 2017-2018 school year
    static {

        scheduleStartTimes = new SparseArray<>();
        scheduleStopTimes = new SparseArray<>();
        sixthGradeScheduleStartTimes = new HashMap<>();
        sixthGradeScheduleStopTimes = new HashMap<>();
        oddDayScheduleStartTimes = new HashMap<>();
        evenDayScheduleStartTimes = new HashMap<>();
        wednesdayScheduleStartTimes = new HashMap<>();
        scheduleStartTimes.put(DateTimeConstants.MONDAY, oddDayScheduleStartTimes);
        scheduleStartTimes.put(DateTimeConstants.TUESDAY, evenDayScheduleStartTimes);
        scheduleStartTimes.put(DateTimeConstants.WEDNESDAY, wednesdayScheduleStartTimes);
        scheduleStartTimes.put(DateTimeConstants.THURSDAY, oddDayScheduleStartTimes);
        scheduleStartTimes.put(DateTimeConstants.FRIDAY, evenDayScheduleStartTimes);
        oddDayScheduleStopTimes = new HashMap<>();
        evenDayScheduleStopTimes = new HashMap<>();
        wednesdayScheduleStopTimes = new HashMap<>();
        scheduleStopTimes.put(DateTimeConstants.MONDAY, oddDayScheduleStopTimes);
        scheduleStopTimes.put(DateTimeConstants.TUESDAY, evenDayScheduleStopTimes);
        scheduleStopTimes.put(DateTimeConstants.WEDNESDAY, wednesdayScheduleStopTimes);
        scheduleStopTimes.put(DateTimeConstants.THURSDAY, oddDayScheduleStopTimes);
        scheduleStopTimes.put(DateTimeConstants.FRIDAY, evenDayScheduleStopTimes);

        // period 0 is advisory

        // odd day start times
        oddDayScheduleStartTimes.put("1", new LocalTime(8, 15));
        oddDayScheduleStartTimes.put("0", new LocalTime(9, 50));
        oddDayScheduleStartTimes.put("3", new LocalTime(10, 15));
        oddDayScheduleStartTimes.put("5", new LocalTime(11, 50));
        oddDayScheduleStartTimes.put("7", new LocalTime(13, 55));

        // even day start times
        evenDayScheduleStartTimes.put("2", new LocalTime(8, 15));
        evenDayScheduleStartTimes.put("0", new LocalTime(9, 50));
        evenDayScheduleStartTimes.put("4", new LocalTime(10, 15));
        evenDayScheduleStartTimes.put("6", new LocalTime(11, 50));
        evenDayScheduleStartTimes.put("8", new LocalTime(13, 55));

        // odd day end times
        oddDayScheduleStopTimes.put("1", new LocalTime(9, 45));
        oddDayScheduleStopTimes.put("0", new LocalTime(10, 10));
        oddDayScheduleStopTimes.put("3", new LocalTime(11, 45));
        oddDayScheduleStopTimes.put("5", new LocalTime(13, 50));
        oddDayScheduleStopTimes.put("7", new LocalTime(15, 25));

        // even day end times
        evenDayScheduleStopTimes.put("2", new LocalTime(9, 45));
        evenDayScheduleStopTimes.put("0", new LocalTime(10, 10));
        evenDayScheduleStopTimes.put("4", new LocalTime(11, 45));
        evenDayScheduleStopTimes.put("6", new LocalTime(13, 50));
        evenDayScheduleStopTimes.put("8", new LocalTime(15, 25));

        // wednesday start times
        wednesdayScheduleStartTimes.put("1", new LocalTime(8, 15));
        wednesdayScheduleStartTimes.put("2", new LocalTime(9, 0));
        wednesdayScheduleStartTimes.put("3", new LocalTime(9, 45));
        wednesdayScheduleStartTimes.put("4", new LocalTime(10, 30));
        wednesdayScheduleStartTimes.put("0", new LocalTime(11, 10));
        wednesdayScheduleStartTimes.put("5", new LocalTime(12, 30));
        wednesdayScheduleStartTimes.put("6", new LocalTime(13, 15));
        wednesdayScheduleStartTimes.put("7", new LocalTime(14, 0));
        wednesdayScheduleStartTimes.put("8", new LocalTime(14, 45));

        // wednesday stop times
        wednesdayScheduleStopTimes.put("1", new LocalTime(8, 55));
        wednesdayScheduleStopTimes.put("2", new LocalTime(9, 40));
        wednesdayScheduleStopTimes.put("3", new LocalTime(10, 25));
        wednesdayScheduleStopTimes.put("4", new LocalTime(11, 10));
        wednesdayScheduleStopTimes.put("0", new LocalTime(12, 25));
        wednesdayScheduleStopTimes.put("5", new LocalTime(13, 10));
        wednesdayScheduleStopTimes.put("6", new LocalTime(13, 55));
        wednesdayScheduleStopTimes.put("7", new LocalTime(14, 40));
        wednesdayScheduleStopTimes.put("8", new LocalTime(15, 25));

        // sixth grade start times
        sixthGradeScheduleStartTimes.put("0", new LocalTime(8, 15));
        sixthGradeScheduleStartTimes.put("1", new LocalTime(8, 25));
        sixthGradeScheduleStartTimes.put("2", new LocalTime(9, 13));
        sixthGradeScheduleStartTimes.put("3", new LocalTime(10, 20));
        sixthGradeScheduleStartTimes.put("4", new LocalTime(11, 10));
        sixthGradeScheduleStartTimes.put("5", new LocalTime(12, 55));
        sixthGradeScheduleStartTimes.put("6", new LocalTime(13, 45));

        // sixth grade stop times
        sixthGradeScheduleStopTimes.put("0", new LocalTime(8, 25));
        sixthGradeScheduleStopTimes.put("1", new LocalTime(9, 13));
        sixthGradeScheduleStopTimes.put("2", new LocalTime(10, 3));
        sixthGradeScheduleStopTimes.put("3", new LocalTime(11, 8));
        sixthGradeScheduleStopTimes.put("4", new LocalTime(11, 58));
        sixthGradeScheduleStopTimes.put("5", new LocalTime(13, 43));
        sixthGradeScheduleStopTimes.put("6", new LocalTime(14, 32));

    }

    @Override
    public String getFocusTld() {
        return FOCUS_TLD;
    }

    @Override
    public String getShortName() {
        return NAME_SHORT;
    }

    @Override
    public String getFullName() {
        return NAME_FULL;
    }

    @Override
    public String getDomainName() {
        return DOMAIN_NAME;
    }

    @Override
    public LocalTime getStartTimeOfSchooldayOnDay(DateTime day) {
        return schooldayStart;
    }

    @Override
    public LocalTime getStopTimeOfSchooldayOnDay(DateTime day) {
        return schooldayStop;
    }

    @Override
    public LocalTime getStartTimeOfPeriodOnDay(String period, DateTime day) {
        Map<String, LocalTime> daySchedule = scheduleStartTimes.get(day.getDayOfWeek());
        if (daySchedule != null && daySchedule.containsKey(period)) {
            return daySchedule.get(period);
        }
        Log.e(TAG, "Period " + period + " does not occur on the given day, using start time of schoolday instead");
        return schooldayStart;
    }

    @Override
    public LocalTime getStopTimeOfPeriodOnDay(String period, DateTime day) {
        Map<String, LocalTime> daySchedule = scheduleStopTimes.get(day.getDayOfWeek());
        if (daySchedule != null && daySchedule.containsKey(period)) {
            return daySchedule.get(period);
        }
        Log.e(TAG, "Period " + period + " does not occur on the given day, using stop time of schoolday instead");
        return schooldayStop;
    }

    public LocalTime getStartTimeOfPeriodSixthGrade(String period) {
        if (sixthGradeScheduleStartTimes.containsKey(period)) {
            return sixthGradeScheduleStartTimes.get(period);
        }
        return schooldayStart;
    }

    public LocalTime getStopTimeOfPeriodSixthGrade(String period) {
        if (sixthGradeScheduleStopTimes.containsKey(period)) {
            return sixthGradeScheduleStopTimes.get(period);
        }
        return schooldayStop;
    }

    @Override
    public String getTeacherEmailFromName(String teacherName) {
        String[] names = teacherName.toLowerCase().split(" ");
        if (names.length > 1) {
            return names[0] + "." + names[names.length - 1] + "@" + DOMAIN_NAME;
        }
        return names[0] + "@" + DOMAIN_NAME;
    }

    @Override
    public int getBellScheduleTypesId() {
        return R.array.asd_bell_schedule_type;
    }

    @Override
    public int[] getBellScheduleLayouts() {
        return new int[]{
                R.layout.view_school_schedule_asd_7_12,
                R.layout.view_school_schedule_asd_6,
                R.layout.view_school_schedule_asd_7_12_delay,
                R.layout.view_school_schedule_asd_6_delay,
                R.layout.view_school_schedule_asd_spark
        };
    }

    @Override
    public int getMapDrawableId() {
        return R.drawable.school_asd_school_map;
    }

    @Override
    public int getMapRoomNumbersId() {
        return R.raw.map_asd_room_numbers;
    }

    @Override
    public int getMapKeywordsId() {
        return R.raw.map_asd_room_keywords;
    }

}
