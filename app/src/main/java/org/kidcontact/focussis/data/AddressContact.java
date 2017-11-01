package org.kidcontact.focussis.data;

/**
 * Created by slensky on 5/22/17.
 */

public class AddressContact {

    private final String address;
    private final String apartment;
    private final String cellPhone;
    private final String city;
    private final String email;
    private final String homePhone;
    private final String name;
    private final String privateEmail;
    private final String relationship;
    private final String state;
    private final String zip;

    public AddressContact(String address, String apartment, String cellPhone, String city, String email, String homePhone, String name, String privateEmail, String relationship, String state, String zip) {
        this.address = address;
        this.apartment = apartment;
        this.cellPhone = cellPhone;
        this.city = city;
        this.email = email;
        this.homePhone = homePhone;
        this.name = name;
        this.privateEmail = privateEmail;
        this.relationship = relationship;
        this.state = state;
        this.zip = zip;
    }

    public String getAddress() {
        return address;
    }

    public String getApartment() {
        return apartment;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public String getCity() {
        return city;
    }

    public String getEmail() {
        return email;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public String getName() {
        return name;
    }

    public String getPrivateEmail() {
        return privateEmail;
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
}
