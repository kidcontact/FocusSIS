package com.slensky.focussis.parser;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.slensky.focussis.data.Demographic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by slensky on 3/24/18.
 */

public class DemographicParser extends FocusPageParser {
    private static final String TAG = "DemographicParser";

    private JSONArray result0; // contains basic information like first/last name
    private JSONArray result1; // custom demographic fields, e.g. nickname, locker #, bus #
    private JSONArray result2; // parental permissions

    @Override
    public Demographic parse(String jsonStr) throws JSONException {
        JSONArray json = new JSONArray(jsonStr);
        result0 = json.getJSONObject(0).getJSONArray("result");
        result1 = json.getJSONObject(1).getJSONArray("result");
        result2 = json.getJSONObject(2).getJSONArray("result");

        // Get basic user info which should always be present (result0)
        String first = findField(result0, "First Name", "students|first_name", "first_name").getString("value");
        Object middleObj = findField(result0, "Middle Name", "students|middle_name", "middle_name").get("value");
        String middle = JSONObject.NULL.equals(middleObj) ? null : (String) middleObj;
        String last = findField(result0, "Last Name", "students|last_name", "last_name").getString("value");
        String name = first + " " + (middle != null ? middle + " " : "") + last;

        int passLength = findField(result0, "Password", "students|password", "password").getString("value").length();

        // Collect all custom field titles and values (result1)
        Map<String, String> customFields = new LinkedHashMap<>();
        for (int i = 0; i < result1.length(); i++) {
            if (!(result1.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result1.getJSONObject(i);
            if (field.has("title") && field.has("value") && !JSONObject.NULL.equals(field.get("value"))) {
                String value = field.getString("value");
                try {
                    if (field.has("options") && field.get("options") instanceof JSONArray) {
                        JSONArray options = field.getJSONArray("options");
                        for (int j = 0; j < options.length(); j++) {
                            if (options.get(j) instanceof JSONObject && options.getJSONObject(j).getString("value").equals(value)) {
                                value = options.getJSONObject(j).getString("text");
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "JSONException while attempting to parseRequirements options for custom field " + field.getString("title"));
                    Log.w(TAG, field.toString(2));
                    e.printStackTrace();
                }

                customFields.put(field.getString("title").trim(), value.trim());
            }
        }

        // Collect all parental document/permission information (result2)

        // sort the table by the sort_order element so that data fields will be after their headers
        SortedSet<JSONObject> formsPermissionsBySortOrder = new TreeSet<>(new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    return o1.getInt("sort_order") - o2.getInt("sort_order");
                } catch (JSONException e) {
                    return 0; // should never happen, we ensure objects have this property before inserting them
                }
            }
        });
        Map<String, Boolean> parentalForms = new LinkedHashMap<>();
        Map<String, Boolean> parentalPermissions = new LinkedHashMap<>();
        for (int i = 0; i < result2.length(); i++) {
            JSONObject o = result2.getJSONObject(i);
            if (o.has("title") && o.has("value") && o.has("type") && o.has("sort_order")) {
                formsPermissionsBySortOrder.add(o);
            }
        }

        boolean isFormsSection = false;
        boolean isPermissionsSection = false;
        for (JSONObject o : formsPermissionsBySortOrder) {
            if (o.getString("type").equals("holder")) { // "holder" type seems to be a header
                if (o.getString("title").toLowerCase().contains("documents")) {
                    isPermissionsSection = false;
                    isFormsSection = true;
                } else if (o.getString("title").toLowerCase().contains("permissions")) {
                    isPermissionsSection = true;
                    isFormsSection = false;
                } else {
                    Log.w(TAG, "Unrecognized holder " + o.getString("title"));
                }
            } else if (o.getString("type").equals("checkbox")) {
                String title = o.getString("title").trim();
                String v = o.getString("value");
                boolean value = v.equals("1") || v.equals("true");
                if (isFormsSection) {
                    parentalForms.put(title, value);
                } else if (isPermissionsSection) {
                    parentalPermissions.put(title, value);
                }
            }
        }

        return new Demographic(name, passLength, customFields, parentalForms, parentalPermissions);
    }

    // searches through the given result to find the field that has either the title, id, or column_name given
    // prefers to find a matching title, then matching id, then matching columnName
    private JSONObject findField(@NonNull JSONArray result, @NonNull String title, @NonNull String id, @NonNull String columnName) throws JSONException {
        // result title
        for (int i = 0; i < result.length(); i++) {
            if (!(result.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result.getJSONObject(i);
            if (field.getString("title").equals(title)) {
                Log.v(TAG, String.format("\"%s\" retrieved via title", title));
                return field;
            }
        }

        // result id
        for (int i = 0; i < result.length(); i++) {
            if (!(result.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result.getJSONObject(i);
            if (field.getString("id").equals(id)) {
                Log.v(TAG, String.format("\"%s\" retrieved via id", title));
                return field;
            }
        }

        // result columnName
        for (int i = 0; i < result.length(); i++) {
            if (!(result.get(i) instanceof JSONObject)) {
                continue;
            }

            JSONObject field = result.getJSONObject(i);
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
