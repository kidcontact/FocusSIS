package com.slensky.focussis.data;

import android.graphics.Bitmap;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by slensky on 5/15/17.
 */

public class Demographic {

    private static final String TAG = "Demographic";

    private Student student;
    private final String name;
    private final int passwordLength;
    private final Map<String, String> customFields;
    private final Map<String, Boolean> parentalForms;
    private final Map<String, Boolean> parentalPermissions;

    public Demographic(String name, int passwordLength, Map<String, String> customFields, Map<String, Boolean> parentalForms, Map<String, Boolean> parentalPermissions) {
        this.name = name;
        this.passwordLength = passwordLength;
        this.customFields = customFields;
        this.parentalForms = parentalForms;
        this.parentalPermissions = parentalPermissions;
    }

    public Student getStudent() {
        return student;
    }

    public String getName() {
        return name;
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }

    public Map<String, Boolean> getParentalForms() {
        return parentalForms;
    }

    public Map<String, Boolean> getParentalPermissions() {
        return parentalPermissions;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

}
