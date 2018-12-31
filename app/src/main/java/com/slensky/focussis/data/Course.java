package com.slensky.focussis.data;

import android.util.Log;

import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by slensky on 4/16/17.
 */

public class Course extends MarkingPeriodPage {

    private static final String TAG = Course.class.getName();
    private final List<CourseAssignment> assignments;
    private final List<CourseCategory> categories;
    private final String id;
    private final String letterGrade;
    private final String name;
    private final int percentGrade;
    private final String period;
    private final String teacher;

    public Course(List<MarkingPeriod> markingPeriods, List<Integer> markingPeriodYears, List<CourseAssignment> assignments, List<CourseCategory> categories, String id, String letterGrade, String name, int percentGrade, String period, String teacher) {
        super(markingPeriods, markingPeriodYears);
        this.assignments = assignments;
        this.categories = categories;
        this.id = id;
        this.letterGrade = letterGrade;
        this.name = name;
        this.percentGrade = percentGrade;
        this.period = period;
        this.teacher = teacher;
    }

    public List<CourseAssignment> getAssignments() {
        return assignments;
    }

    public List<CourseCategory> getCategories() {
        return categories;
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
        return assignments.size() > 0;
    }

    public boolean hasCategories() {
        return categories != null;
    }

    public int getAppropriateAssignmentNameLength() {
        List<CourseAssignment> sortedAssignments = new ArrayList<>(assignments);

        Collections.sort(sortedAssignments, new Comparator<CourseAssignment>() {
            @Override
            public int compare(CourseAssignment o1, CourseAssignment o2) {
                return o1.getName().length() - o2.getName().length();
            }
        });
        int numToRemove = (int) (sortedAssignments.size() * 0.1);
        for (int i = 0; i < numToRemove; i++) {
            sortedAssignments.remove(0);
            sortedAssignments.remove(sortedAssignments.size() - 1);
        }

        int average = 0;
        for (CourseAssignment a : sortedAssignments) {
            average += a.getName().length();
        }
        average = average / sortedAssignments.size();

        if (average * 1.5 < 40) {
            return (int) (average * 1.5);
        }
        else {
            return 40;
        }

    }

}
