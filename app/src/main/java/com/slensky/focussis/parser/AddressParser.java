package com.slensky.focussis.parser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 3/27/18.
 */

public class AddressParser extends FocusPageParser {
    private static final String TAG = "AddressParser";

    @Override
    public JSONObject parse(String jsonStr) throws JSONException {
        JSONArray json = new JSONArray(jsonStr);
        JSONArray result0 = json.getJSONObject(0).getJSONArray("result");
        JSONArray result1 = json.getJSONObject(1).getJSONArray("result");
        JSONObject parsed = new JSONObject();

        parsed.put("address", result0.getJSONObject(0).getString("address"));
        if (!JSONObject.NULL.equals(result0.getJSONObject(0).get("address2"))) {
            parsed.put("apt", result0.getJSONObject(0).getString("address2"));
        }
        parsed.put("city", result0.getJSONObject(0).getString("city"));
        parsed.put("state", result0.getJSONObject(0).getString("state"));
        parsed.put("zip", result0.getJSONObject(0).getString("zipcode"));
        if (!JSONObject.NULL.equals(result0.getJSONObject(0).get("phone")) && result0.getJSONObject(0).getString("phone").length() > 4) {
            parsed.put("phone", sanitizePhoneNumber(result0.getJSONObject(0).getString("phone")));
        }

        List<JSONObject> contacts = new ArrayList<>();
        for (int i = 0; i < result1.length(); i++) {
            JSONObject c = result1.getJSONObject(i);
            JSONObject parsedC = new JSONObject();

            String first = c.getString("first_name");
            Object middleObj = c.get("middle_name");
            String middle = JSONObject.NULL.equals(middleObj) ? null : (String) middleObj;
            String last = c.getString("last_name");
            parsedC.put("name", first + " " + (middle != null ? middle + " " : "") + last);
            parsedC.put("emergency", c.getBoolean("_emergency"));

            if (!JSONObject.NULL.equals(c.get("_student_relation"))) {
                parsedC.put("relationship", c.getString("_student_relation"));
            }
            parsedC.put("custody", c.getBoolean("_custody"));

            if (!JSONObject.NULL.equals(c.get("email"))) {
                parsedC.put("email", c.getString("email"));
            }

            if (c.has("_details")) {
                JSONArray details = new JSONArray();
                Iterator<?> keys = c.getJSONObject("_details").keys();
                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    if ( c.getJSONObject("_details").get(key) instanceof JSONObject ) {
                        JSONObject d = c.getJSONObject("_details").getJSONObject(key);
                        if (JSONObject.NULL.equals(d.get("value")) || d.getString("value").length() == 0) {
                            continue;
                        }
                        JSONObject parsedD = new JSONObject();

                        parsedD.put("title", d.getString("title"));
                        if (d.getString("title").toLowerCase().contains("phone") && sanitizePhoneNumber(d.getString("value")).length() > 4) {
                            parsedD.put("type", "phone");
                            parsedD.put("value", sanitizePhoneNumber(d.getString("value")));
                        }
                        else if (d.getString("title").toLowerCase().contains("email")
                                || d.getString("title").toLowerCase().contains("e-mail")) {
                            parsedD.put("type", "email");
                            parsedD.put("value", d.getString("value"));
                        }
                        else {
                            Log.w(TAG, "Unknown detail type " + d.getString("title"));
                            parsedD.put("type", "other");
                            parsedD.put("value", d.getString("value"));
                        }
                        details.put(parsedD);
                    }
                }
                parsedC.put("details", details);
            }

            String addressId = c.getString("_address_id");
            boolean foundAddress = false;
            for (int j = 0; j < result0.length(); j++) {
                JSONObject addressJson = result0.getJSONObject(j);
                if (addressJson.getString("address_id").equals(addressId)) {
                    foundAddress = true;
                    parsedC.put("address", addressJson.getString("address"));
                    if (!JSONObject.NULL.equals(addressJson.get("address2"))) {
                        parsed.put("apt", addressJson.getString("address2"));
                    }
                    parsedC.put("city", addressJson.getString("city"));
                    parsedC.put("state", addressJson.getString("state"));
                    parsedC.put("zip", addressJson.getString("zipcode"));
                    if (!JSONObject.NULL.equals(addressJson.get("phone")) && addressJson.getString("phone").length() > 4) {
                        parsedC.put("phone", sanitizePhoneNumber(addressJson.getString("phone")));
                    }
                }
            }
            parsedC.put("has_address", foundAddress);
            contacts.add(parsedC);
        }

        // contacts with custody should be listed first, followed by contacts without custody, followed by emergency contacts
        Collections.sort(contacts, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObject, JSONObject t1) {
                try {
                    if (jsonObject.getBoolean("custody")) {
                        return -1;
                    }
                    else if (jsonObject.getBoolean("emergency")) {
                        return 1;
                    }
                    return 0;
                } catch (JSONException e) {
                    Log.e(TAG, "Could not find expected booleans for custody/emergency on contact!");
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        parsed.put("contacts", new JSONArray(contacts));

        return parsed;
    }

}
