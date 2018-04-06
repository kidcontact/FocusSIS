package com.slensky.focussis.data;

import android.util.Log;

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

    public Course(JSONObject courseJSON) {
        super(courseJSON);

        List<CourseAssignment> assignments;
        List<CourseCategory> categories;
        String id;
        String letterGrade;
        String name;
        int percentGrade;
        String period;
        String teacher;

        try {
            JSONArray assignmentsJSON = courseJSON.getJSONArray("assignments");
            assignments = new ArrayList<CourseAssignment>();

            for (int i = 0; i < assignmentsJSON.length(); i++) {
                JSONObject assignmentJSON = assignmentsJSON.getJSONObject(i);
                String assignmentName = assignmentJSON.getString("name");
                DateTime assigned = new DateTime(assignmentJSON.getString("assigned"));
                DateTime due = new DateTime(assignmentJSON.getString("due"));
                String category = null;
                if (assignmentJSON.has("category")) {
                    category = assignmentJSON.getString("category");
                }

                int maxGrade = -1;
                String maxGradeString = null;
                double studentGrade = -1;
                String studentGradeString = null;
                String assignmentLetterGrade = null;
                int assignmentPercentGrade = -1;
                String gradeString = null;
                CourseAssignment.Status status = null;

                if (assignmentJSON.getString("status").equals("pass")) {
                    status = CourseAssignment.Status.PASS;
                }
                else if (assignmentJSON.getString("status").equals("fail")) {
                    status = CourseAssignment.Status.FAIL;
                }
                else if (assignmentJSON.getString("status").equals("excluded")) {
                    status = CourseAssignment.Status.EXCLUDED;
                    if (assignmentJSON.has("max_grade_string")) {
                        maxGradeString = assignmentJSON.getString("max_grade_string");
                    }
                    else {
                        maxGrade = assignmentJSON.getInt("max_grade");
                    }
                }
                else if (assignmentJSON.getString("status").equals("ng")) {
                    status = CourseAssignment.Status.NOT_GRADED;
                    if (assignmentJSON.has("max_grade_string")) {
                        maxGradeString = assignmentJSON.getString("max_grade_string");
                    }
                    else {
                        maxGrade = assignmentJSON.getInt("max_grade");
                    }
                }
                else if (assignmentJSON.getString("status").equals("missing")) {
                    status = CourseAssignment.Status.MISSING;
                    if (assignmentJSON.has("max_grade_string")) {
                        maxGradeString = assignmentJSON.getString("max_grade_string");
                    }
                    else {
                        maxGrade = assignmentJSON.getInt("max_grade");
                    }
                }
                else if (assignmentJSON.getString("status").equals("extra")) {
                    status = CourseAssignment.Status.EXTRA_CREDIT;
                    if (assignmentJSON.has("max_grade_string")) {
                        maxGradeString = assignmentJSON.getString("max_grade_string");
                    }
                    else {
                        maxGrade = assignmentJSON.getInt("max_grade");
                    }
                    if (assignmentJSON.has("student_grade_string")) {
                        studentGradeString = assignmentJSON.getString("student_grade_string");
                    }
                    else {
                        studentGrade = assignmentJSON.getDouble("student_grade");
                    }
                }
                else if (assignmentJSON.getString("status").equals("graded")) {
                    status = CourseAssignment.Status.GRADED;
                    if (assignmentJSON.has("max_grade_string")) {
                        maxGradeString = assignmentJSON.getString("max_grade_string");
                    }
                    else {
                        maxGrade = assignmentJSON.getInt("max_grade");
                    }
                    if (assignmentJSON.has("student_grade_string")) {
                        studentGradeString = assignmentJSON.getString("student_grade_string");
                    }
                    else {
                        studentGrade = assignmentJSON.getDouble("student_grade");
                    }
                    if (assignmentJSON.has("overall_grade_string")) {
                        gradeString = assignmentJSON.getString("overall_grade_string");
                    }
                    else {
                        assignmentLetterGrade = assignmentJSON.getString("letter_overall_grade");
                        assignmentPercentGrade = assignmentJSON.getInt("percent_overall_grade");
                    }
                }

                String description = null;
                if (assignmentJSON.has("description")) {
                    description = assignmentJSON.getString("description");
                }

                assignments.add(new CourseAssignment(assignmentName, assigned, due, category, maxGrade, maxGradeString, studentGrade, studentGradeString, assignmentLetterGrade, assignmentPercentGrade, gradeString, description, status));

            }
        } catch (JSONException e) {
            assignments = null;
            Log.e(TAG, "Error parsing course assignment JSON");

        }

        try {
            JSONArray categoriesJSON = courseJSON.getJSONArray("categories");
            categories = new ArrayList<CourseCategory>();

            for (int i = 0; i < categoriesJSON.length(); i++) {
                JSONObject categoryJSON = categoriesJSON.getJSONObject(i);
                String categoryName = categoryJSON.getString("name");
                String categoryLetterGrade = null;
                int categoryPercentGrade = -1;
                if (categoryJSON.has("letter_grade") && categoryJSON.has("percent_grade")) {
                    categoryLetterGrade = categoryJSON.getString("letter_grade");
                    categoryPercentGrade = categoryJSON.getInt("percent_grade");
                }
                int percentWeight = categoryJSON.getInt("percent_weight");
                categories.add(new CourseCategory(categoryName, categoryLetterGrade, categoryPercentGrade, percentWeight));
            }
        } catch (JSONException e) {
            categories = null;
            Log.e(TAG, "Error parsing categories of course");
        }

        try {
            id = courseJSON.getString("id");
        } catch (JSONException e) {
            id = null;
            Log.e(TAG, "ID of course not found");
        }

        try {
            letterGrade = courseJSON.getString("letter_grade");
            percentGrade = courseJSON.getInt("percent_grade");
        } catch (JSONException e) {
            letterGrade = null;
            percentGrade = -1;
            Log.w(TAG, "No grade for course found, could be ungraded");
        }

        try {
            name = courseJSON.getString("name");
        } catch (JSONException e) {
            name = null;
            Log.e(TAG, "Course name not found in JSON");
        }

        try {
            period = courseJSON.getString("period");
        } catch (JSONException e) {
            period = "";
            Log.e(TAG, "Course period not found in JSON");
        }

        try {
            teacher = courseJSON.getString("teacher");
        } catch (JSONException e) {
            teacher = null;
            Log.e(TAG, "Course teacher not found in JSON");
        }

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
        return assignments != null;
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
