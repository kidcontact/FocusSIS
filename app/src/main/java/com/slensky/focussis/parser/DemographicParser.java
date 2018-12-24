package com.slensky.focussis.parser;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.NoSuchElementException;

/**
 * Created by slensky on 3/24/18.
 */

public class DemographicParser extends FocusPageParser {
    private static final String TAG = "DemographicParser";

    private JSONArray result0;
    private JSONArray result1;

    @Override
    public JSONObject parse(String jsonStr) throws JSONException {
        JSONArray json = new JSONArray(jsonStr);
        result0 = json.getJSONObject(0).getJSONArray("result");
        result1 = json.getJSONObject(1).getJSONArray("result");
        JSONObject parsed = new JSONObject();

        String first = findField("First Name", "students|first_name", "first_name").getString("value");
        Object middleObj = findField("Middle Name", "students|middle_name", "middle_name").get("value");
        String middle = JSONObject.NULL.equals(middleObj) ? null : (String) middleObj;
        String last = findField("Last Name", "students|last_name", "last_name").getString("value");
        parsed.put("name", first + " " + (middle != null ? middle + " " : "") + last);

        parsed.put("username", findField("Username", "students|username", "username").getString("value"));
        parsed.put("pass_length", findField("Password", "students|password", "password").getString("value").length());

        try {
            JSONObject levelField = findField("Level (Year)", "384", "custom_103");
            parsed.put("level", Integer.parseInt(getTextFromOptions(levelField, "1")));
        } catch (NoSuchElementException e) {
            Log.w(TAG, "Level (number of years at school) not found in demographic JSON");
        }

        try {
            JSONObject genderField = findField("Gender", "380", "custom_200000000");
            parsed.put("gender", getTextFromOptions(genderField, "Unknown"));
        } catch (NoSuchElementException e) {
            Log.w(TAG, "Gender not found in demographic JSON");
        }


        JSONObject nicknameField = findField("Nickname", "394", "custom_200000002");
        if (!JSONObject.NULL.equals(nicknameField.get("value"))) {
            parsed.put("nickname", nicknameField.getString("value"));
        }

        parsed.put("email", findField("Email", "393", "custom_200000012").getString("value"));

        JSONObject lockerField = findField("Locker #", "245", "custom_51");
        if (!JSONObject.NULL.equals(lockerField.get("value"))) {
            String locker = lockerField.getString("value");
            if (!locker.isEmpty() && !locker.equals("-")) {
                parsed.put("locker", locker);
            }
        }

        JSONObject lockerComboField = findField("Locker Combo", "247", "custom_118");
        if (!JSONObject.NULL.equals(lockerComboField.get("value"))) {
            String lockerCombo = lockerComboField.getString("value");
            if (!lockerCombo.isEmpty() && !lockerCombo.equals("-")) {
                parsed.put("locker_combo", lockerCombo);
            }
        }

        JSONObject busesField = findField("Bus", "28", "custom_50");
        if (!JSONObject.NULL.equals(busesField.get("value"))) {
            String[] buses = busesField.getString("value").split("/");
            if (!buses[0].trim().isEmpty() && !buses[0].equals("0") && !buses[0].equals("-")) {
                parsed.put("arrival_bus", buses[0]);
                if (buses.length == 1 && buses[0].split(" ").length > 1) {
                    buses = buses[0].split(" ");
                }
                parsed.put("dismissal_bus", buses.length == 1 ? buses[0] : buses[1]);
            }
        }

        try {
            JSONObject cumulativeFileField = findField("Cumulative File", "518", "custom_92");
            parsed.put("cumulative_file", getTextFromOptions(cumulativeFileField, "Unknown"));
        } catch (NoSuchElementException e) {
            Log.w(TAG, "Cumulative file not found in demographic JSON");
        }

        try {
            JSONObject medicalRecordsField = findField("Medical Records In", "297", "custom_94");
            parsed.put("medical_record_status", getTextFromOptions(medicalRecordsField, "Unknown"));
        } catch (NoSuchElementException e) {
            Log.w(TAG, "Medical records in not found in demographic JSON");
        }

        JSONObject photoAuthField = findField("Photo/Publicity Authorized", "232", "custom_317");
        parsed.put("photo_auth", photoAuthField.getString("value").equals("1"));

        // TODO: new fields, permission to record and off campus lunch?

        JSONObject studentMobileField = findField("Student Mobile", "392", "custom_64");
        String studentMobile = sanitizePhoneNumber(studentMobileField.getString("value"));
        if (!studentMobile.isEmpty() && !studentMobile.equals("000")) {
            parsed.put("student_mobile", studentMobile);
        }

        return parsed;
    }

    // searches through both result0 and result1 to find the field that has either the title, id, or column_name given
    // prefers to find a matching title, then matching id, then matching columnName
    private JSONObject findField(@NonNull String title, @NonNull String id, @NonNull String columnName) throws JSONException {
        // result0 title
        for (int i = 0; i < result0.length(); i++) {
            if (!(result0.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result0.getJSONObject(i);
            if (field.getString("title").equals(title)) {
                Log.v(TAG, String.format("\"%s\" retrieved via title", title));
                return field;
            }
        }

        // result1 title
        for (int i = 0; i < result1.length(); i++) {
            if (!(result1.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result1.getJSONObject(i);
            if (field.getString("title").equals(title)) {
                Log.v(TAG, String.format("\"%s\" retrieved via title", title));
                return field;
            }
        }

        // result0 id
        for (int i = 0; i < result0.length(); i++) {
            if (!(result0.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result0.getJSONObject(i);
            if (field.getString("id").equals(id)) {
                Log.v(TAG, String.format("\"%s\" retrieved via id", title));
                return field;
            }
        }

        // result1 id
        for (int i = 0; i < result1.length(); i++) {
            if (!(result1.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result1.getJSONObject(i);
            if (field.getString("id").equals(id)) {
                Log.v(TAG, String.format("\"%s\" retrieved via id", title));
                return field;
            }
        }

        // result0 columnName
        for (int i = 0; i < result0.length(); i++) {
            if (!(result0.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result0.getJSONObject(i);
            if (field.getString("column_name").equals(columnName)) {
                Log.v(TAG, String.format("\"%s\" retrieved via column_name", title));
                return field;
            }
        }

        // result1 columnName
        for (int i = 0; i < result1.length(); i++) {
            if (!(result1.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result1.getJSONObject(i);
            if (field.getString("column_name").equals(columnName)) {
                Log.v(TAG, String.format("\"%s\" retrieved via column_name", title));
                return field;
            }
        }

        throw new NoSuchElementException(String.format("Field title %s, id %s, column_name %s could not be found", title, id, columnName));
    }

    private String getTextFromOptions(JSONObject field, String defaultText) throws JSONException {
        if (!field.has("options")) {
            throw new NoSuchElementException("Requested field does not have options");
        }

        String value = field.getString("value");
        JSONArray options = field.getJSONArray("options");
        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.getJSONObject(i);
            if (option.getString("value").equals(value)) {
                return option.getString("text");
            }
        }

        // get title for warning log string
        String title = "[title not in json]";
        if (field.has("title")) {
            title = field.getString("title");
        }
        Log.w(TAG, String.format("Using defaultText for field title %s", title));
        return defaultText;
    }

}
