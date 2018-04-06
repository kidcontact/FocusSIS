package com.slensky.focussis.data;

import org.joda.time.DateTime;

/**
 * Created by slensky on 4/16/17.
 */

public class CourseAssignment {

    private final String name;
    private final DateTime assigned;
    private final DateTime due;
    private final String category;
    private final int maxGrade;
    private final String maxGradeString;
    private final double studentGrade;
    private final String studentGradeString;
    private final String letterGrade;
    private final int percentGrade;
    private final String overallGradeString;
    private final String description;

    public enum Status {
        PASS,
        FAIL,
        EXCLUDED,
        NOT_GRADED,
        MISSING,
        EXTRA_CREDIT,
        GRADED
    }
    private final Status status;


    public CourseAssignment(String name, DateTime assigned, DateTime due, String category, int maxGrade, String maxGradeString, double studentGrade, String studentGradeString, String letterGrade, int percentGrade, String overallGradeString, String description, Status status) {
        this.name = name;
        this.assigned = assigned;
        this.due = due;
        this.category = category;
        this.maxGrade = maxGrade;
        this.maxGradeString = maxGradeString;
        this.studentGrade = studentGrade;
        this.studentGradeString = studentGradeString;
        this.letterGrade = letterGrade;
        this.percentGrade = percentGrade;
        this.status = status;
        this.overallGradeString = overallGradeString;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getShortName(int length) {
        if (name.length() > length - 3) {
            if (name.charAt(length - 4) == ' ') {
                return name.substring(0, length - 4) + "...";
            }
            else {
                return name.substring(0, length - 3) + "...";
            }
        }
        else {
            return name;
        }
    }

    public DateTime getAssigned() {
        return assigned;
    }

    public DateTime getDue() {
        return due;
    }

    public String getCategory() {
        return category;
    }

    public int getMaxGrade() {
        return maxGrade;
    }

    public boolean hasMaxGradeString() {
        return maxGradeString != null;
    }

    public String getMaxGradeString() {
        return maxGradeString;
    }

    public double getStudentGrade() {
        return studentGrade;
    }

    public boolean hasStudentGradeString() {
        return studentGradeString != null;
    }

    public String getStudentGradeString() {
        return studentGradeString;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public int getPercentGrade() {
        return percentGrade;
    }

    public boolean hasOverallGradeString() {
        return overallGradeString != null;
    }

    public String getOverallGradeString() {
        return overallGradeString;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

}
