package com.slensky.focussis.data;

import android.util.Log;

import com.slensky.focussis.util.SchoolSingleton;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 4/14/17.
 */

public class Portal extends MarkingPeriodPage {
    private static final String TAG = "Portal";

    private final List<PortalCourse> courses;
    private final List<PortalEvent> events;
    private final PortalAlert[] alerts;

    public Portal(JSONObject portalJSON) {
        super(portalJSON);
        List<PortalCourse> courses;
        List<PortalEvent> events;
        PortalAlert[] alerts;

        try {
            JSONObject coursesJSON = portalJSON.getJSONObject("courses");
            Iterator<String> coursesIterator = coursesJSON.keys();
            courses = new ArrayList<PortalCourse>();

            while (coursesIterator.hasNext()) {
                JSONObject courseJSON = coursesJSON.getJSONObject(coursesIterator.next());

                String days = courseJSON.getString("days");
                String id = courseJSON.getString("id");
                String name = courseJSON.getString("name");
                String teacher = courseJSON.getString("teacher");
                String teacherEmail;
                if (courseJSON.has("teacher_email")) {
                    teacherEmail = courseJSON.getString("teacher_email");
                }
                else {
                    teacherEmail = SchoolSingleton.getInstance().getSchool().getTeacherEmailFromName(teacher);
                    Log.e(TAG, "Portal course JSON does not have teacher email for " + name + ", falling back to " + teacherEmail);
                }
                String period = courseJSON.getString("period");
                String letterGrade = null;
                int percentGrade = -1;
                if (courseJSON.has("letter_grade") && courseJSON.has("percent_grade")) {
                    letterGrade = courseJSON.getString("letter_grade");
                    percentGrade = courseJSON.getInt("percent_grade");
                }

                List<PortalAssignment> assignments = new ArrayList<>();
                if (courseJSON.has("assignments")) {
                    JSONArray assignmentsJSON = courseJSON.getJSONArray("assignments");
                    for (int i = 0; i < assignmentsJSON.length(); i++) {
                        JSONObject assignmentJSON = assignmentsJSON.getJSONObject(i);
                        assignments.add(new PortalAssignment(assignmentJSON.getString("name"), new DateTime(assignmentJSON.getString("due")), name, period, teacher, teacherEmail));
                    }
                }

                courses.add(new PortalCourse(assignments, days, id, letterGrade, name, percentGrade, period, teacher, teacherEmail));

            }

        } catch (JSONException e) {
            courses = null;
            e.printStackTrace();
            Log.e(TAG, "Error parsing portal courses");
        }

        try {
            JSONArray eventsJSON = portalJSON.getJSONArray("events");
            events = new ArrayList<PortalEvent>();
            for (int i = 0; i < eventsJSON.length(); i++) {
                JSONObject eventJSON = eventsJSON.getJSONObject(i);
                events.add(new PortalEvent(eventJSON.getString("description"), new DateTime(eventJSON.getString("date"))));
            }
        } catch (JSONException e) {
            events = null;
            Log.e(TAG, "Error parsing portal events");
        }

        try {
            JSONArray alertsJson = portalJSON.getJSONArray("alerts");
            alerts = new PortalAlert[alertsJson.length()];
            for (int i = 0; i < alertsJson.length(); i++) {
                JSONObject a = alertsJson.getJSONObject(i);
                alerts[i] = new PortalAlert(a.getString("message"), a.getString("url"));
            }
        } catch (JSONException e) {
            alerts = null;
            Log.w(TAG, "No alerts found in portal JSON");
        }

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
        return courses != null;
    }

    public boolean hasEvents() {
        return events != null;
    }

    public PortalAlert[] getAlerts() {
        return alerts;
    }

}
