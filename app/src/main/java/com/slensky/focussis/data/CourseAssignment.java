package com.slensky.focussis.data;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

/**
 * Created by slensky on 4/16/17.
 */

public class CourseAssignment implements Comparable<CourseAssignment> {

    private final String name;
    private final DateTime assigned;
    private final DateTime due;
    private final DateTime lastModified;
    private final String category;
    private final int maxGrade;
    private final String maxGradeString;
    private final double studentGrade;
    private final String studentGradeString;
    private final String fullGradeRatioString;
    private final String letterGrade;
    private final int percentGrade;
    private final String overallGradeString;
    private final String description;
    private final String markingPeriodId;
    private boolean isCustomAssignment;
    private boolean isEditedAssignment;

    public enum Status {
        PASS,
        FAIL,
        EXCLUDED,
        NOT_GRADED,
        MISSING,
        EXTRA_CREDIT,
        GRADED,
        OTHER
    }
    private final Status status;

    public CourseAssignment(String name, DateTime assigned, DateTime due, DateTime lastModified, String category, int maxGrade, String maxGradeString, double studentGrade, String studentGradeString, String fullGradeRatioString, String letterGrade, int percentGrade, String overallGradeString, String description, Status status, String markingPeriodId) {
        this.name = name;
        this.assigned = assigned;
        this.due = due;
        this.lastModified = lastModified;
        this.category = category;
        this.maxGrade = maxGrade;
        this.maxGradeString = maxGradeString;
        this.studentGrade = studentGrade;
        this.studentGradeString = studentGradeString;
        this.fullGradeRatioString = fullGradeRatioString;
        this.letterGrade = letterGrade;
        this.percentGrade = percentGrade;
        this.status = status;
        this.overallGradeString = overallGradeString;
        this.description = description;
        this.markingPeriodId = markingPeriodId;
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

    public DateTime getLastModified() {
        return lastModified;
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

    public boolean hasFullGradeRatioString() {
        return fullGradeRatioString != null;
    }

    public String getFullGradeRatioString() {
        return fullGradeRatioString;
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

    public boolean isCustomAssignment() {
        return isCustomAssignment;
    }

    public void setCustomAssignment(boolean customAssignment) {
        isCustomAssignment = customAssignment;
    }

    public boolean isEditedAssignment() {
        return isEditedAssignment;
    }

    public void setEditedAssignment(boolean editedAssignment) {
        isEditedAssignment = editedAssignment;
    }

    public String getMarkingPeriodId() {
        return markingPeriodId;
    }

    public boolean overrides(CourseAssignment that) {
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (assigned != null ? !assigned.equals(that.assigned) : that.assigned != null)
            return false;
        if (markingPeriodId != null ? !markingPeriodId.equals(that.markingPeriodId) : that.markingPeriodId != null)
            return false;
        if (!isEditedAssignment() || that.isCustomAssignment() || that.isEditedAssignment())
            return false;
        return !(that.lastModified.isAfter(lastModified) && that.hasDescription());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseAssignment that = (CourseAssignment) o;

        if (maxGrade != that.maxGrade) return false;
        if (Double.compare(that.studentGrade, studentGrade) != 0) return false;
        if (percentGrade != that.percentGrade) return false;
        if (isCustomAssignment != that.isCustomAssignment) return false;
        if (isEditedAssignment != that.isEditedAssignment) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (assigned != null ? !assigned.equals(that.assigned) : that.assigned != null)
            return false;
        if (due != null ? !due.equals(that.due) : that.due != null) return false;
        if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null)
            return false;
        if (category != null ? !category.equals(that.category) : that.category != null)
            return false;
        if (maxGradeString != null ? !maxGradeString.equals(that.maxGradeString) : that.maxGradeString != null)
            return false;
        if (studentGradeString != null ? !studentGradeString.equals(that.studentGradeString) : that.studentGradeString != null)
            return false;
        if (fullGradeRatioString != null ? !fullGradeRatioString.equals(that.fullGradeRatioString) : that.fullGradeRatioString != null)
            return false;
        if (letterGrade != null ? !letterGrade.equals(that.letterGrade) : that.letterGrade != null)
            return false;
        if (overallGradeString != null ? !overallGradeString.equals(that.overallGradeString) : that.overallGradeString != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (markingPeriodId != null ? !markingPeriodId.equals(that.markingPeriodId) : that.markingPeriodId != null)
            return false;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (assigned != null ? assigned.hashCode() : 0);
        result = 31 * result + (due != null ? due.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + maxGrade;
        result = 31 * result + (maxGradeString != null ? maxGradeString.hashCode() : 0);
        temp = Double.doubleToLongBits(studentGrade);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (studentGradeString != null ? studentGradeString.hashCode() : 0);
        result = 31 * result + (fullGradeRatioString != null ? fullGradeRatioString.hashCode() : 0);
        result = 31 * result + (letterGrade != null ? letterGrade.hashCode() : 0);
        result = 31 * result + percentGrade;
        result = 31 * result + (overallGradeString != null ? overallGradeString.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (isCustomAssignment ? 1 : 0);
        result = 31 * result + (isEditedAssignment ? 1 : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull CourseAssignment assignment) {
        if (due.compareTo(assignment.due) != 0) {
            return due.compareTo(assignment.due);
        }
        if (assigned.compareTo(assignment.assigned) != 0) {
            return assigned.compareTo(assignment.assigned);
        }
        return assignment.name.compareTo(name);
    }

}
