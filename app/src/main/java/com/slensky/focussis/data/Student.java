package com.slensky.focussis.data;

import android.graphics.Bitmap;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by slensky on 3/26/18.
 */

public class Student {

    private final JSONObject json;
    private final String id;
    private final int grade;
    private final DateTime birthdate;
    private Bitmap picture;

    public Student(JSONObject student) throws JSONException {
        this.json = student;
        this.id = student.getString("id");
        this.grade = student.getInt("grade");
        this.birthdate = new DateTime(student.getString("birthdate"));
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

    public JSONObject getJson() {
        return json;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

}
