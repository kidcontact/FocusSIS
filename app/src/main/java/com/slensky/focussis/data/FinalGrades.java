package com.slensky.focussis.data;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 4/3/18.
 */

public class FinalGrades extends MarkingPeriodPage {
    private static final String TAG = "FinalGrades";

    private final String gpa;
    private final String weightedGpa;
    private final String totalCreditsEarned;
    private final String currentSemesterName;
    private final String currentSemesterExamsName;
    private final String commentCodes;
    private final List<FinalGrade> finalGrades;

    public FinalGrades(JSONObject page) {
        super(page);

        String gpa = null;
        String weightedGpa = null;
        String totalCreditsEarned = null;
        String currentSemesterName = null;
        String currentSemesterExamsName = null;
        String commentCodes = null;

        try {
            gpa = page.getString("gpa");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting gpa");
            e.printStackTrace();
        }
        try {
            weightedGpa = page.getString("weighted_gpa");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting weighted gpa");
            e.printStackTrace();
        }
        try {
            totalCreditsEarned = page.getString("credits_earned");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting credits earned");
            e.printStackTrace();
        }

        try {
            currentSemesterName = page.getString("current_sem_name");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting current semester name");
            e.printStackTrace();
        }
        try {
            currentSemesterExamsName = page.getString("current_sem_exams_name");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting current semester exams name");
            e.printStackTrace();
        }

        try {
            commentCodes = page.getString("comment_codes");
        } catch (JSONException e) {
            Log.e(TAG, "Error getting comment codes");
            e.printStackTrace();
        }

        this.gpa = gpa;
        this.weightedGpa = weightedGpa;
        this.totalCreditsEarned = totalCreditsEarned;
        this.currentSemesterName = currentSemesterName;
        this.currentSemesterExamsName = currentSemesterExamsName;
        this.commentCodes = commentCodes;

        List<FinalGrade> finalGrades = new ArrayList<>();
        try {
            Iterator<?> keys = page.getJSONObject("grades").keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (page.getJSONObject("grades").getJSONObject(key) instanceof JSONObject) {
                    JSONObject grade = page.getJSONObject("grades").getJSONObject(key);
                    try {
                        String id = grade.getString("id");
                        String syear = grade.getString("syear");
                        String name = grade.getString("name");
                        boolean affectsGpa = grade.getBoolean("affects_gpa");
                        double gpaPoints = -1;
                        double weightedGpaPoints = -1;
                        if (affectsGpa) {
                            gpaPoints = grade.getDouble("gpa_points");
                            weightedGpaPoints = grade.getDouble("weighted_gpa_points");
                        }
                        String teacher = grade.getString("teacher");
                        String courseId = grade.getString("course_id");
                        String courseNum = grade.getString("course_num");
                        String percentGrade = null;
                        if (grade.has("percent_grade")) {
                            percentGrade = grade.getString("percent_grade");
                        }
                        String letterGrade = null;
                        if (grade.has("letter_grade")) {
                            letterGrade = grade.getString("letter_grade");
                        }
                        double credits = -1;
                        double creditsEarned = -1;
                        if (grade.has("credits")) {
                            credits = grade.getDouble("credits");
                            creditsEarned = grade.getDouble("credits_earned");
                        }
                        int gradeLevel = -1;
                        if (grade.has("grade_level")) {
                            gradeLevel = grade.getInt("grade_level");
                        }
                        DateTime lastUpdated = null;
                        if (grade.has("last_updated")) {
                            lastUpdated = new DateTime(grade.getString("last_updated"));
                        }
                        String location = grade.getString("location");
                        String mpId = grade.getString("mp_id");
                        String mpTitle = grade.getString("mp_title");
                        String yearTitle = grade.getString("year_title");
                        String gradeScaleTitle = grade.getString("grade_scale_title");
                        String gradSubject = null;
                        if (grade.has("grad_subject")) {
                            gradSubject = grade.getString("grad_subject");
                        }
                        String comment = null;
                        if (grade.has("comment")) {
                            comment = grade.getString("comment");
                        }

                        FinalGrade finalGrade = new FinalGrade(id, syear, name, affectsGpa, gpaPoints, weightedGpaPoints, teacher, courseId, courseNum, percentGrade, letterGrade, credits, creditsEarned, gradeLevel, lastUpdated, location, mpId, mpTitle, yearTitle, gradeScaleTitle, gradSubject, comment);
                        finalGrades.add(finalGrade);

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing grade " + grade.toString());
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Could not find grades in JSON");
            e.printStackTrace();
        }

        Collections.sort(finalGrades, Collections.<FinalGrade>reverseOrder());
        this.finalGrades = finalGrades;

    }

    public String getGpa() {
        return gpa;
    }

    public String getWeightedGpa() {
        return weightedGpa;
    }

    public String getTotalCreditsEarned() {
        return totalCreditsEarned;
    }

    public String getCurrentSemesterName() {
        return currentSemesterName;
    }

    public String getCurrentSemesterExamsName() {
        return currentSemesterExamsName;
    }

    public String getCommentCodes() {
        return commentCodes;
    }

    public List<FinalGrade> getFinalGrades() {
        return finalGrades;
    }

}
