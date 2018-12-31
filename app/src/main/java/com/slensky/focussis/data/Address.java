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
    private Student student;
    private final String address;
    private final String apartment;
    private final String city;
    private final String state;
    private final String zip;
    private final String phone;
    private final List<AddressContact> contacts;

    public Address(String address, String apartment, String city, String state, String zip, String phone, List<AddressContact> contacts) {
        this.address = address;
        this.apartment = apartment;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.phone = phone;
        this.contacts = contacts;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
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
