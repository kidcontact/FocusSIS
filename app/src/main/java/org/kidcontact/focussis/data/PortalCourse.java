package org.kidcontact.focussis.data;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by slensky on 4/14/17.
 */

public class PortalCourse implements Comparable<PortalCourse> {

    private final List<PortalAssignment> assignments;
    private final String days;
    private final String id;
    private final String letterGrade;
    private final String name;
    private final int percentGrade;
    private final String period;
    private final String teacher;

    public PortalCourse(List<PortalAssignment> assignments,
                        String days,
                        String id,
                        String letterGrade,
                        String name,
                        int percentGrade,
                        String period,
                        String teacher) {
        this.assignments = assignments;
        this.days = days;
        this.id = id;
        this.letterGrade = letterGrade;
        this.name = name;
        this.percentGrade = percentGrade;
        this.period = period;
        this.teacher = teacher;
    }

    public List<PortalAssignment> getAssignments() {
        return assignments;
    }

    public String getDays() {
        return days;
    }

    public String getId() {
        return id;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public String getName() {
        return name;
    }

    public int getPercentGrade() {
        return percentGrade;
    }

    public String getPeriod() {
        return period;
    }

    public String getTeacher() {
        return teacher;
    }

    public boolean isGraded() {
        return letterGrade != null && percentGrade >= 0;
    }

    public boolean hasAssignments() {
        return assignments != null;
    }

    @Override
    public int compareTo(@NonNull PortalCourse o) {
        return this.period.compareTo(o.period);
    }
}
