package com.slensky.focussis.data;

import android.support.annotation.NonNull;

import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

/**
 * Created by slensky on 4/3/18.
 */

public class FinalGrade implements Comparable<FinalGrade> {

    private final String id;
    private final String syear;
    private final String name;
    private final boolean affectsGpa;
    private final double gpaPoints;
    private final double weightedGpaPoints;
    private final String teacher;
    private final String courseId;
    private final String courseNum;
    private final String percentGrade;
    private final String letterGrade;
    private final double credits;
    private final double creditsEarned;
    private final int gradeLevel;
    private final DateTime lastUpdated;
    private final String location;
    private final String mpId;
    private final String mpTitle;
    private final String gradeScaleTitle;
    private final String gradSubject;
    private final String comment;

    public FinalGrade(String id, String syear, String name, boolean affectsGpa, double gpaPoints, double weightedGpaPoints, String teacher, String courseId, String courseNum, String percentGrade, String letterGrade, double credits, double creditsEarned, int gradeLevel, DateTime lastUpdated, String location, String mpId, String mpTitle, String gradeScaleTitle, String gradSubject, String comment) {
        this.id = id;
        this.syear = syear;
        this.name = name;
        this.affectsGpa = affectsGpa;
        this.gpaPoints = gpaPoints;
        this.weightedGpaPoints = weightedGpaPoints;
        this.teacher = teacher;
        this.courseId = courseId;
        this.courseNum = courseNum;
        this.percentGrade = percentGrade;
        this.letterGrade = letterGrade;
        this.credits = credits;
        this.creditsEarned = creditsEarned;
        this.gradeLevel = gradeLevel;
        this.lastUpdated = lastUpdated;
        this.location = location;
        this.mpId = mpId;
        this.mpTitle = mpTitle;
        this.gradeScaleTitle = gradeScaleTitle;
        this.gradSubject = gradSubject;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public String getSyear() {
        return syear;
    }

    public String getName() {
        return name;
    }

    public boolean affectsGpa() {
        return affectsGpa;
    }

    public double getGpaPoints() {
        return gpaPoints;
    }

    public double getWeightedGpaPoints() {
        return weightedGpaPoints;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseNum() {
        return courseNum;
    }

    public boolean hasPercentGrade() {
        return percentGrade != null;
    }

    public String getPercentGrade() {
        return percentGrade;
    }

    public boolean hasLetterGrade() {
        return letterGrade != null;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public boolean hasCredits() {
        return credits != -1;
    }

    public double getCredits() {
        return credits;
    }

    public double getCreditsEarned() {
        return creditsEarned;
    }

    public boolean hasGradeLevel() {
        return gradeLevel != -1;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public boolean hasLastUpdated() {
        return lastUpdated != null;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getLocation() {
        return location;
    }

    public String getMpId() {
        return mpId;
    }

    public String getMpTitle() {
        return mpTitle;
    }

    public String getGradeScaleTitle() {
        return gradeScaleTitle;
    }

    public boolean hasGradSubject() {
        return gradSubject != null;
    }

    public String getGradSubject() {
        return gradSubject;
    }

    public boolean hasComment() {
        return comment != null;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public int compareTo(@NonNull FinalGrade o) {
        if (!syear.equals(o.syear)) {
            if (NumberUtils.isDigits(syear) && NumberUtils.isDigits(o.syear)) {
                return Integer.compare(Integer.parseInt(syear), Integer.parseInt(o.syear));
            }
            return syear.compareTo(o.syear);
        }
        else if (!mpId.equals(o.mpId)) {
            if (NumberUtils.isDigits(mpId) && NumberUtils.isDigits(o.mpId)) {
                return Integer.compare(Integer.parseInt(o.mpId), Integer.parseInt(mpId));
            }
            return o.mpId.compareTo(mpId);
        }
        return name.compareTo(o.name) * -1;
    }
}
