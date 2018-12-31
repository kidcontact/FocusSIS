package com.slensky.focussis.data;

/**
 * Created by slensky on 4/28/17.
 */

public class ScheduleCourse {

    private final String days;
    private final String name;
    private final String period;
    private final String room;
    private final String teacher;
    public enum Term {
        S1,
        S2,
        Q1,
        Q2,
        Q3,
        Q4,
        YEAR
    }
    private final Term term;

    public ScheduleCourse(String days, String name, String period, String room, String teacher, Term term) {
        this.days = days;
        this.name = name;
        this.period = period;
        this.room = room;
        this.teacher = teacher;
        this.term = term;
    }

    public String getDays() {
        return days;
    }

    public String getName() {
        return name;
    }

    public String getPeriod() {
        return period;
    }

    public String getRoom() {
        return room;
    }

    public String getTeacher() {
        return teacher;
    }

    public Term getTerm() {
        return term;
    }

    public boolean isAdvisory() {
        return period != null && period.toLowerCase().equals("advisory");
    }

}
