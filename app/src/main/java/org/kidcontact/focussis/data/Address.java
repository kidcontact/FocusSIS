package org.kidcontact.focussis.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 5/21/17.
 */

public class Address extends MarkingPeriodPage {

    private final String TAG = "Address";
    private final String address;
    private final String apartment;
    private final String city;
    private final String state;
    private final String zip;
    private final String phone;
    private final List<AddressContact> contacts;

    public Address(JSONObject addressJSON) {
        super(addressJSON);

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
            Log.e(TAG, "apartment not found in JSON");
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
            Log.e(TAG, "phone not found in JSON");
        }

        try {
            JSONArray contactsJSON = addressJSON.getJSONArray("contacts");
            for (int i = 0; i < contactsJSON.length(); i++) {
                JSONObject contactJSON = contactsJSON.getJSONObject(i);

                String cAddress = null;
                String cApartment = null;
                String cCellPhone = null;
                String cCity = null;
                String cEmail = null;
                String cHomePhone = null;
                String cName = null;
                String cPrivateEmail = null;
                String cRelationship = null;
                String cState = null;
                String cZip = null;
                try {
                    cAddress = contactJSON.getString("address");
                } catch (JSONException e) {
                    Log.e(TAG, "address not found in contact JSON");
                }
                try {
                    cApartment = contactJSON.getString("apt");
                } catch (JSONException e) {
                    Log.e(TAG, "apartment not found in contact JSON");
                }
                try {
                    cCellPhone = contactJSON.getString("cell_phone");
                } catch (JSONException e) {
                    Log.e(TAG, "cell phone not found in contact JSON");
                }
                try {
                    cCity = contactJSON.getString("city");
                } catch (JSONException e) {
                    Log.e(TAG, "city not found in contact JSON");
                }
                try {
                    cEmail = contactJSON.getString("email");
                } catch (JSONException e) {
                    Log.e(TAG, "email not found in contact JSON");
                }
                try {
                    cHomePhone = contactJSON.getString("home_phone");
                } catch (JSONException e) {
                    Log.e(TAG, "home phone not found in contact JSON");
                }
                try {
                    cName = contactJSON.getString("name");
                } catch (JSONException e) {
                    Log.e(TAG, "name not found in contact JSON");
                }
                try {
                    cPrivateEmail = contactJSON.getString("private_email");
                } catch (JSONException e) {
                    Log.e(TAG, "private email not found in contact JSON");
                }
                try {
                    cRelationship = contactJSON.getString("relationship");
                    cRelationship = Character.toUpperCase(cRelationship.charAt(0)) + cRelationship.substring(1);
                } catch (JSONException e) {
                    Log.e(TAG, "relationship not found in contact JSON");
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
                contacts.add(new AddressContact(cAddress, cApartment, cCellPhone, cCity, cEmail, cHomePhone, cName, cPrivateEmail, cRelationship, cState, cZip));
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

    public String getPhone() {
        return phone;
    }

    public List<AddressContact> getContacts() {
        return contacts;
    }
}
