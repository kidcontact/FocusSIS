package com.slensky.focussis.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.slensky.focussis.data.CalendarEvent;
import com.slensky.focussis.data.FocusPreferences;
import com.slensky.focussis.data.Student;

import org.json.JSONObject;

/**
 * Created by slensky on 5/3/18.
 * Generates test data offline instead of connecting to Focus
 */

public class FocusDebugApi extends FocusApi {

    private static final int FAKE_LOAD_TIME_MS = 1000;

    public FocusDebugApi(String username, String password, Context context) {
        super(username, password, context);
    }

    @Override
    public Request login(Response.Listener<Boolean> listener, Response.ErrorListener errorListener) {
        return super.login(listener, errorListener);
    }

    @Override
    public Request logout(Response.Listener<Boolean> listener, Response.ErrorListener errorListener) {
        return super.logout(listener, errorListener);
    }

    @Override
    public Request getPortal(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getPortal(listener, errorListener);
    }

    @Override
    public Request getCourse(String id, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getCourse(id, listener, errorListener);
    }

    @Override
    public Request getSchedule(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getSchedule(listener, errorListener);
    }

    @Override
    public Request getCalendar(int year, int month, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getCalendar(year, month, listener, errorListener);
    }

    @Override
    public Request getCalendarEvent(String id, CalendarEvent.EventType eventType, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getCalendarEvent(id, eventType, listener, errorListener);
    }

    @Override
    public Request getDemographic(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getDemographic(listener, errorListener);
    }

    @Override
    public Request getAddress(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getAddress(listener, errorListener);
    }

    @Override
    public Request getReferrals(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getReferrals(listener, errorListener);
    }

    @Override
    public Request getAbsences(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getAbsences(listener, errorListener);
    }

    @Override
    public Request getFinalGrades(FinalGradesType type, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getFinalGrades(type, listener, errorListener);
    }

    @Override
    public Request getPreferences(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.getPreferences(listener, errorListener);
    }

    @Override
    public Request setPreferences(FocusPreferences preferences, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.setPreferences(preferences, listener, errorListener);
    }

    @Override
    public Request changePassword(String currentPassword, String newPassword, String verifyNewPassword, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return super.changePassword(currentPassword, newPassword, verifyNewPassword, listener, errorListener);
    }

    @Override
    public boolean isSessionExpired() {
        return super.isSessionExpired();
    }

    @Override
    public long getSessionTimeout() {
        return super.getSessionTimeout();
    }

    @Override
    public void setSessionTimeout(long sessionTimeout) {
        super.setSessionTimeout(sessionTimeout);
    }

    @Override
    public boolean isLoggedIn() {
        return super.isLoggedIn();
    }

    @Override
    public void setLoggedIn(boolean loggedIn) {
        super.setLoggedIn(loggedIn);
    }

    @Override
    public boolean hasSession() {
        return super.hasSession();
    }

    @Override
    public Student getStudent() {
        return super.getStudent();
    }

}
