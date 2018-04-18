package com.slensky.focussis.data;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

/**
 * Created by slensky on 4/14/17.
 */

public class PortalAssignment implements Comparable<PortalAssignment> {

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

    @Override
    public int compareTo(@NonNull PortalAssignment assignment) {
        if (due.compareTo(assignment.due) != 0) {
            return due.compareTo(assignment.due);
        }
        return assignment.name.compareTo(name);
    }
}
