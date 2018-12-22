package com.slensky.focussis.data;

import android.graphics.Bitmap;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by slensky on 3/26/18.
 */

public class Student {

    private final JSONObject json;
    private final String id;
    private final int grade;
    private DateTime birthdate;
    private Bitmap picture;

    private final String apiUrl;
    private final Map<String, Map<String, String>> methods;

    public Student(JSONObject student) throws JSONException {
        this.json = student;
        this.id = student.getString("id");
        this.grade = student.getInt("grade");
        if (student.has("birthdate")) {
            this.birthdate = new DateTime(student.getString("birthdate"));
        }

        this.apiUrl = student.getString("api_url");

        JSONObject controllersJson = student.getJSONObject("methods");
        methods = new HashMap<>();
        Iterator<?> keys = controllersJson.keys();
        while(keys.hasNext()) {
            String key = (String) keys.next();
            if (controllersJson.get(key) instanceof JSONObject) {
                Map<String, String> methodsMap = new HashMap<>();
                JSONObject methodsJson = controllersJson.getJSONObject(key);
                Iterator<?> keys2 = methodsJson.keys();
                while(keys2.hasNext()) {
                    String key2 = (String) keys2.next();
                    if (methodsJson.get(key2) instanceof String) {
                        methodsMap.put(key2, methodsJson.getString(key2));
                    }
                }
                methods.put(key, methodsMap);
            }
        }
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

    public String getApiUrl() {
        return apiUrl;
    }

    public Map<String, Map<String, String>> getMethods() {
        return methods;
    }

}
