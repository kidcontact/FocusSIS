package com.slensky.focussis.data.focus;

import java.util.List;

/**
 * Created by slensky on 4/28/17.
 */

public class Schedule extends MarkingPeriodPage {

    private static final String TAG = Schedule.class.getName();
    private final List<ScheduleCourse> courses;

    public Schedule(List<MarkingPeriod> markingPeriods, List<Integer> markingPeriodYears, List<ScheduleCourse> courses) {
        super(markingPeriods, markingPeriodYears);
        this.courses = courses;
    }

    public List<ScheduleCourse> getCourses() {
        return courses;
    }

}
