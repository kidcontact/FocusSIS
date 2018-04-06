package com.slensky.focussis.data;

/**
 * Created by slensky on 3/27/18.
 */

public class AddressContactDetail {

    public enum Type {
        PHONE,
        EMAIL,
        OTHER
    }
    private final String title;
    private final String value;
    private final Type type;

    public AddressContactDetail(String title, String value, Type type) {
        this.title = title;
        this.value = value;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

}
