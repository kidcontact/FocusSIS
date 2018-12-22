package com.slensky.focussis.school;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.json.JSONObject;

/**
 * Created by slensky on 4/25/18.
 * Contains school specific information such as the name of the school, URLs for Focus, and schedule
 */

public abstract class School {
    protected static final String TAG = "School";

    public abstract String getFocusTld();
    public abstract String getShortName();
    public abstract String getFullName();
    public abstract String getDomainName();
    public abstract LocalTime getStartTimeOfSchooldayOnDay(DateTime day);
    public abstract LocalTime getStopTimeOfSchooldayOnDay(DateTime day);
    public abstract LocalTime getStartTimeOfPeriodOnDay(String period, DateTime day);
    public abstract LocalTime getStopTimeOfPeriodOnDay(String period, DateTime day);

    // not guaranteed to produce accurate results, but is used when districts follow simple formats
    // (e.g. firstname.lastname@domain.edu) and the teacher's email cannot be found through other means
    public abstract String getTeacherEmailFromName(String teacherName);

    public abstract int getBellScheduleTypesId();
    public abstract int[] getBellScheduleLayouts();
    public abstract int getMapDrawableId();
    public abstract int getMapRoomNumbersId();
    public abstract int getMapKeywordsId();
}
