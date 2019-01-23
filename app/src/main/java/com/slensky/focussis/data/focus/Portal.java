package com.slensky.focussis.data.focus;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by slensky on 4/14/17.
 */

public class Portal extends MarkingPeriodPage {
    private static final String TAG = "Portal";

    private final List<PortalCourse> courses;
    private final List<PortalEvent> events;
    private final List<PortalAlert> alerts;

    public Portal(@NonNull List<PortalCourse> courses, @NonNull List<PortalEvent> events, @NonNull List<PortalAlert> alerts, List<MarkingPeriod> markingPeriods, List<Integer> markingPeriodYears) {
        super(markingPeriods, markingPeriodYears);
        this.courses = courses;
        this.events = events;
        this.alerts = alerts;
    }

    public List<PortalCourse> getCourses() {
        return courses;
    }

    public List<PortalEvent> getEvents() {
        return events;
    }

    public boolean hasCourses() {
        return courses.size() > 0;
    }

    public boolean hasEvents() {
        return events.size() > 0;
    }

    public List<PortalAlert> getAlerts() {
        return alerts;
    }

}
