package com.slensky.focussis.data;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by slensky on 3/26/18.
 * (represents the Student page in Focus, not the actual student)
 */

public class Student extends MarkingPeriodPage {

    private final String id;
    private final int grade;
    @Nullable private final DateTime birthdate;
    private final String pictureUrl;
    @Nullable private Bitmap picture;

    private final String apiUrl;
    private final Map<String, Map<String, String>> methods;

    public Student(List<MarkingPeriod> markingPeriods, List<Integer> markingPeriodYears, String id, int grade, DateTime birthdate, String pictureUrl, String apiUrl, Map<String, Map<String, String>> methods) {
        super(markingPeriods, markingPeriodYears);
        this.id = id;
        this.grade = grade;
        this.birthdate = birthdate;
        this.pictureUrl = pictureUrl;
        this.apiUrl = apiUrl;
        this.methods = methods;
    }

    public String getId() {
        return id;
    }

    public int getGrade() {
        return grade;
    }

    public DateTime getBirthdate() {
        return birthdate;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public Map<String, Map<String, String>> getMethods() {
        return methods;
    }

}
