package com.slensky.focussis.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 5/21/17.
 */

public class Address {

    private final String TAG = "Address";
    private final String address;
    private final String apartment;
    private final String city;
    private final String state;
    private final String zip;
    private final String phone;
    private final List<AddressContact> contacts;

    public Address(JSONObject addressJSON) {
        String address = null;
        String apartment = null;
        String city = null;
        String state = null;
        String zip = null;
        String phone = null;
        List<AddressContact> contacts = new ArrayList<>();

        try {
            address = addressJSON.getString("address");
        } catch (JSONException e) {
            Log.e(TAG, "address not found in JSON");
        }

        try {
            apartment = addressJSON.getString("apt");
        } catch (JSONException e) {
            Log.w(TAG, "apartment not found in JSON");
        }

        try {
            city = addressJSON.getString("city");
        } catch (JSONException e) {
            Log.e(TAG, "city not found in JSON");
        }

        try {
            state = addressJSON.getString("state");
        } catch (JSONException e) {
            Log.e(TAG, "state not found in JSON");
        }

        try {
            zip = addressJSON.getString("zip");
        } catch (JSONException e) {
            Log.e(TAG, "zip not found in JSON");
        }

        try {
            phone = addressJSON.getString("phone");
        } catch (JSONException e) {
            Log.w(TAG, "phone not found in JSON");
        }

        try {
            JSONArray contactsJSON = addressJSON.getJSONArray("contacts");
            for (int i = 0; i < contactsJSON.length(); i++) {
                JSONObject contactJSON = contactsJSON.getJSONObject(i);

                String cAddress = null;
                String cApartment = null;
                String cCity = null;
                String cPhone = null;
                String cEmail = null;
                String cName = null;
                String cRelationship = null;
                String cState = null;
                String cZip = null;
                boolean cCustody = false;
                boolean cEmergency = false;
                List<AddressContactDetail> cDetails = null;
                if (contactJSON.has("address")) {
                    try {
                        cAddress = contactJSON.getString("address");
                    } catch (JSONException e) {
                        Log.e(TAG, "address not found in contact JSON");
                    }
                    try {
                        cApartment = contactJSON.getString("apt");
                    } catch (JSONException e) {
                        Log.w(TAG, "apartment not found in contact JSON");
                    }
                    try {
                        cCity = contactJSON.getString("city");
                    } catch (JSONException e) {
                        Log.e(TAG, "city not found in contact JSON");
                    }
                    try {
                        cState = contactJSON.getString("state");
                    } catch (JSONException e) {
                        Log.e(TAG, "state not found in contact JSON");
                    }
                    try {
                        cZip = contactJSON.getString("zip");
                    } catch (JSONException e) {
                        Log.e(TAG, "zip not found in contact JSON");
                    }
                }

                try {
                    cPhone = contactJSON.getString("phone");
                } catch (JSONException e) {
                    Log.w(TAG, "phone not found in contact JSON");
                }
                try {
                    cEmail = contactJSON.getString("email");
                } catch (JSONException e) {
                    Log.w(TAG, "email not found in contact JSON");
                }
                try {
                    cName = contactJSON.getString("name");
                } catch (JSONException e) {
                    Log.e(TAG, "name not found in contact JSON");
                }
                try {
                    cRelationship = contactJSON.getString("relationship");
                    cRelationship = Character.toUpperCase(cRelationship.charAt(0)) + cRelationship.substring(1);
                } catch (JSONException e) {
                    Log.w(TAG, "relationship not found in contact JSON");
                }
                try {
                    cCustody = contactJSON.getBoolean("custody");
                } catch (JSONException e) {
                    Log.e(TAG, "custody not found in contact JSON");
                }
                try {
                    cEmergency = contactJSON.getBoolean("emergency");
                } catch (JSONException e) {
                    Log.e(TAG, "emergency not found in contact JSON");
                }

                if (contactJSON.has("details")) {
                    try {
                        JSONArray detailsJSON = contactJSON.getJSONArray("details");
                        cDetails = new ArrayList<>();
                        for (int j = 0; j < detailsJSON.length(); j++) {
                            String title = detailsJSON.getJSONObject(j).getString("title");
                            String value = detailsJSON.getJSONObject(j).getString("value");
                            AddressContactDetail.Type type = AddressContactDetail.Type.OTHER;
                            if (detailsJSON.getJSONObject(j).getString("type").equals("phone")) {
                                type = AddressContactDetail.Type.PHONE;
                            }
                            else if (detailsJSON.getJSONObject(j).getString("type").equals("email")) {
                                type = AddressContactDetail.Type.EMAIL;
                            }
                            cDetails.add(new AddressContactDetail(title, value, type));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "error parsing contact details");
                    }
                }

                contacts.add(new AddressContact(cAddress, cApartment, cCity, cPhone, cEmail, cName, cRelationship, cState, cZip, cCustody, cEmergency, cDetails));
            }
        } catch (JSONException e) {
            Log.e(TAG, "contacts not found in JSON");
        }

        this.address = address;
        this.apartment = apartment;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.phone = phone;
        this.contacts = contacts;

    }

    public String getAddress() {
        return address;
    }

    public String getApartment() {
        return apartment;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public boolean hasPhone() {
        return phone != null;
    }

    public String getPhone() {
        return phone;
    }

    public List<AddressContact> getContacts() {
        return contacts;
    }
}
