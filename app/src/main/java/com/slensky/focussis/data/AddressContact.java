package com.slensky.focussis.data;

import java.util.List;

/**
 * Created by slensky on 5/22/17.
 */

public class AddressContact {

    private final String address;
    private final String apartment;
    private final String city;
    private final String phone;
    private final String email;
    private final String name;
    private final String relationship;
    private final String state;
    private final String zip;
    private final boolean custody;
    private final boolean emergency;
    private final List<AddressContactDetail> details;

    public AddressContact(String address, String apartment, String city, String phone, String email, String name, String relationship, String state, String zip, boolean custody, boolean emergency, List<AddressContactDetail> details) {
        this.address = address;
        this.apartment = apartment;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.name = name;
        this.relationship = relationship;
        this.state = state;
        this.zip = zip;
        this.custody = custody;
        this.emergency = emergency;
        this.details = details;
    }

    public boolean hasAddress() {
        return address != null;
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

    public boolean hasPhone() {
        return phone != null;
    }

    public String getPhone() {
        return phone;
    }

    public boolean hasEmail() {
        return email != null;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean hasRelationship() {
        return relationship != null;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public boolean isCustody() {
        return custody;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public boolean hasDetails() {
        return details != null && details.size() > 0;
    }

    public List<AddressContactDetail> getDetails() {
        return details;
    }

}
