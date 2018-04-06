package com.slensky.focussis.parser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by slensky on 3/24/18.
 */

public class DemographicParser extends PageParser {
    private static final String TAG = "DemographicParser";

    @Override
    public JSONObject parse(String jsonStr) throws JSONException {
        JSONArray json = new JSONArray(jsonStr);
        JSONArray result0 = json.getJSONObject(0).getJSONArray("result");
        JSONArray result1 = json.getJSONObject(1).getJSONArray("result");
        JSONObject parsed = new JSONObject();

        String first = result0.getJSONObject(2).getString("value");
        Object middleObj = result0.getJSONObject(3).get("value");
        String middle = JSONObject.NULL.equals(middleObj) ? null : (String) middleObj;
        String last = result0.getJSONObject(4).getString("value");
        parsed.put("name", first + " " + (middle != null ? middle + " " : "") + last);

        parsed.put("username", result0.getJSONObject(6).getString("value"));
        parsed.put("pass_length", result0.getJSONObject(7).getString("value").length());

        String levelId = result1.getJSONObject(10).getString("value");
        int level = 0;
        for (int i = 0; i < result1.getJSONObject(10).getJSONArray("options").length(); i++) {
            JSONObject option = result1.getJSONObject(10).getJSONArray("options").getJSONObject(i);
            if (option.getString("value").equals(levelId)) {
                level = Integer.parseInt(option.getString("text"));
                break;
            }
        }
        parsed.put("level", level);

        String genderId = result0.getJSONObject(1).getString("value");
        String gender = "Unknown";
        for (int i = 0; i < result0.getJSONObject(1).getJSONArray("options").length(); i++) {
            JSONObject option = result0.getJSONObject(1).getJSONArray("options").getJSONObject(i);
            if (option.getString("value").equals(genderId)) {
                gender = option.getString("text");
                break;
            }
        }
        if (gender.equals("Unknown")) Log.w(TAG, "Gender option not found for ID " + genderId);
        parsed.put("gender", gender);

        if (!JSONObject.NULL.equals(result1.getJSONObject(11).get("value"))) {
            parsed.put("nickname", result1.getJSONObject(11).getString("value"));
        }

        parsed.put("email", result1.getJSONObject(0).getString("value"));
        if (!JSONObject.NULL.equals(result1.getJSONObject(1).get("value"))) {
            String locker = result1.getJSONObject(1).getString("value");
            if (!locker.isEmpty() && !locker.equals("-")) {
                parsed.put("locker", locker);
            }
        }
        if (!JSONObject.NULL.equals(result1.getJSONObject(2).get("value"))) {
            String lockerCombo = result1.getJSONObject(2).getString("value");
            if (!lockerCombo.isEmpty() && !lockerCombo.equals("-")) {
                parsed.put("locker_combo", lockerCombo);
            }
        }

        String[] buses = result1.getJSONObject(3).getString("value").split("/");
        if (!buses[0].isEmpty() && !buses[0].equals("0") && !buses[0].equals("-")) {
            parsed.put("arrival_bus", buses[0]);
            if (buses.length == 1 && buses[0].split(" ").length > 1) {
                buses = buses[0].split(" ");
            }
            parsed.put("dismissal_bus", buses.length == 1 ? buses[0] : buses[1]);
        }

        String cumulativeFileId = result1.getJSONObject(4).getString("value");
        String cumulativeFile = "Unknown";
        for (int i = 0; i < result1.getJSONObject(4).getJSONArray("options").length(); i++) {
            JSONObject option = result1.getJSONObject(4).getJSONArray("options").getJSONObject(i);
            if (option.getString("value").equals(cumulativeFileId)) {
                cumulativeFile = option.getString("text");
                break;
            }
        }
        if (cumulativeFile.equals("Unknown")) Log.w(TAG, "Cumulative file option not found for ID " + cumulativeFileId);
        parsed.put("cumulative_file", cumulativeFile);

        String medicalRecordsId = result1.getJSONObject(5).getString("value");
        String medicalRecords = "Unknown";
        for (int i = 0; i < result1.getJSONObject(5).getJSONArray("options").length(); i++) {
            JSONObject option = result1.getJSONObject(5).getJSONArray("options").getJSONObject(i);
            if (option.getString("value").equals(medicalRecordsId)) {
                medicalRecords = option.getString("text");
                break;
            }
        }
        if (medicalRecords.equals("Unknown")) Log.w(TAG, "Medical records option not found for ID " + medicalRecords);
        parsed.put("medical_record_status", medicalRecords);

        parsed.put("photo_auth", result1.getJSONObject(6).getInt("value") == 1);
        String studentMobile = sanitizePhoneNumber(result1.getJSONObject(7).getString("value"));
        if (!studentMobile.isEmpty() && !studentMobile.equals("000")) {
            parsed.put("student_mobile", studentMobile);
        }

        return parsed;
    }

}
