package com.slensky.focussis.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 4/14/17.
 */

public class PortalCourse implements Comparable<PortalCourse> {

    private List<PortalAssignment> assignments;
    private final String id;
    private final String letterGrade;
    private final String name;
    private final int percentGrade;
    private final String period;
    private final String teacher;
    private final String teacherEmail;

    public PortalCourse(List<PortalAssignment> assignments,
                        String id,
                        String letterGrade,
                        String name,
                        int percentGrade,
                        String period,
                        String teacher,
                        String teacherEmail) {
        this.assignments = assignments;
        this.id = id;
        this.letterGrade = letterGrade;
        this.name = name;
        this.percentGrade = percentGrade;
        this.period = period;
        this.teacher = teacher;
        this.teacherEmail = teacherEmail;
    }

    public List<PortalAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<PortalAssignment> assignments) {
        this.assignments = assignments;
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

    public boolean isAdvisory() {
        return period.toLowerCase().equals("advisory") || period.equals("0");
    }

    public String getPeriod() {
        return period;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public boolean isGraded() {
        return letterGrade != null && percentGrade >= 0;
    }

    public boolean hasAssignments() {
        return assignments.size() > 0;
    }

    @Override
    public int compareTo(@NonNull PortalCourse o) {
        return this.period.compareTo(o.period);
    }
}
