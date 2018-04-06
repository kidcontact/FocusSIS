package com.slensky.focussis.data;

import org.joda.time.DateTime;

/**
 * Created by slensky on 4/14/17.
 */

public class PortalAssignment {

    private final String name;
    private final DateTime due;

    public PortalAssignment(String name, DateTime due) {
        this.name = name;
        this.due = due;
    }

    public String getName() {
        return name;
    }

    public DateTime getDue() {
        return due;
    }
}
