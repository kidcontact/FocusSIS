package com.slensky.focussis.parser;

import android.util.Log;

import com.slensky.focussis.data.Address;
import com.slensky.focussis.data.AddressContact;
import com.slensky.focussis.data.AddressContactDetail;

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
    public Address parse(String jsonStr) throws JSONException {
        JSONArray json = new JSONArray(jsonStr);
        JSONArray result0 = json.getJSONObject(0).getJSONArray("result");
        JSONArray result1 = json.getJSONObject(1).getJSONArray("result");


        String address = result0.getJSONObject(0).getString("address");
        String apt = null;
        if (!JSONObject.NULL.equals(result0.getJSONObject(0).get("address2"))) {
            apt = result0.getJSONObject(0).getString("address2");
        }
        String city = result0.getJSONObject(0).getString("city");
        String state = result0.getJSONObject(0).getString("state");
        String zip = result0.getJSONObject(0).getString("zipcode");
        String phone = null;
        if (!JSONObject.NULL.equals(result0.getJSONObject(0).get("phone")) && result0.getJSONObject(0).getString("phone").length() > 4) {
            phone = sanitizePhoneNumber(result0.getJSONObject(0).getString("phone"));
        }

        List<AddressContact> contacts = new ArrayList<>();
        for (int i = 0; i < result1.length(); i++) {
            JSONObject c = result1.getJSONObject(i);

            String first = c.getString("first_name");
            Object middleObj = c.get("middle_name");
            String middle = JSONObject.NULL.equals(middleObj) ? null : (String) middleObj;
            String last = c.getString("last_name");
            String name = first + " " + (middle != null ? middle + " " : "") + last;
            boolean emergency = c.getBoolean("_emergency");

            String relationship = null;
            if (!JSONObject.NULL.equals(c.get("_student_relation"))) {
                relationship = c.getString("_student_relation");
            }
            boolean custody = c.getBoolean("_custody");

            String email =  null;
            if (!JSONObject.NULL.equals(c.get("email"))) {
                email = c.getString("email");
            }

            List<AddressContactDetail> details = null;
            if (c.has("_details")) {
                details = new ArrayList<>();
                Iterator<?> keys = c.getJSONObject("_details").keys();
                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    if ( c.getJSONObject("_details").get(key) instanceof JSONObject ) {
                        JSONObject d = c.getJSONObject("_details").getJSONObject(key);
                        if (JSONObject.NULL.equals(d.get("value")) || d.getString("value").length() == 0) {
                            continue;
                        }

                        String title = d.getString("title");
                        AddressContactDetail.Type type = AddressContactDetail.Type.OTHER;
                        String value;
                        if (d.getString("title").toLowerCase().contains("phone") && sanitizePhoneNumber(d.getString("value")).length() > 4) {
                            type = AddressContactDetail.Type.PHONE;
                            value = sanitizePhoneNumber(d.getString("value"));
                        }
                        else if (d.getString("title").toLowerCase().contains("email")
                                || d.getString("title").toLowerCase().contains("e-mail")) {
                            type = AddressContactDetail.Type.EMAIL;
                            value = d.getString("value");
                        }
                        else {
                            Log.w(TAG, "Unknown detail type " + d.getString("title"));
                            value = d.getString("value");
                        }
                        details.add(new AddressContactDetail(title, value, type));
                    }
                }
            }

            String addressId = c.getString("_address_id");
            String cAddress = null;
            String cApt = null;
            String cCity = null;
            String cState = null;
            String cZip = null;
            String cPhone = null;
            for (int j = 0; j < result0.length(); j++) {
                JSONObject addressJson = result0.getJSONObject(j);
                if (addressJson.getString("address_id").equals(addressId)) {
                    cAddress = addressJson.getString("address");
                    if (!JSONObject.NULL.equals(addressJson.get("address2"))) {
                        cApt = addressJson.getString("address2");
                    }
                    cCity = addressJson.getString("city");
                    cState = addressJson.getString("state");
                    cZip = addressJson.getString("zipcode");
                    if (!JSONObject.NULL.equals(addressJson.get("phone")) && addressJson.getString("phone").length() > 4) {
                        cPhone = sanitizePhoneNumber(addressJson.getString("phone"));
                    }
                    break;
                }
            }
            contacts.add(new AddressContact(cAddress, cApt, cCity, cPhone, email, name, relationship, cState, cZip, custody, emergency, details));
        }

        // contacts with custody should be listed first, followed by contacts without custody, followed by emergency contacts
        Collections.sort(contacts, new Comparator<AddressContact>() {
            @Override
            public int compare(AddressContact c, AddressContact c1) {
                if (c.isCustody()) {
                    return -1;
                }
                else if (c.isEmergency()) {
                    return 1;
                }
                return 0;
            }
        });

        return new Address(address, apt, city, state, zip, phone, contacts);
    }

}
