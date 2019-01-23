package com.slensky.focussis.data.network;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.slensky.focussis.data.focus.Absences;
import com.slensky.focussis.data.focus.Address;
import com.slensky.focussis.data.focus.Calendar;
import com.slensky.focussis.data.focus.CalendarEvent;
import com.slensky.focussis.data.focus.CalendarEventDetails;
import com.slensky.focussis.data.focus.Course;
import com.slensky.focussis.data.focus.Demographic;
import com.slensky.focussis.data.focus.FinalGrades;
import com.slensky.focussis.data.focus.FocusPreferences;
import com.slensky.focussis.data.focus.PasswordResponse;
import com.slensky.focussis.data.focus.Portal;
import com.slensky.focussis.data.focus.Referrals;
import com.slensky.focussis.data.focus.Schedule;

@SuppressWarnings("UnusedReturnValue")
public interface FocusApi {

    Request login(String username, String password, Listener<Boolean> listener, Response.ErrorListener errorListener);

    Request logout(Listener<Boolean> listener, Response.ErrorListener errorListener);

    Request getPortal(Listener<Portal> listener, Response.ErrorListener errorListener);

    Request getCourse(String id, Listener<Course> listener, Response.ErrorListener errorListener);

    Request getSchedule(Listener<Schedule> listener, Response.ErrorListener errorListener);

    Request getCalendar(int year, int month, Listener<Calendar> listener, Response.ErrorListener errorListener);

    // alternative to assignment is event
    Request getCalendarEvent(String id, CalendarEvent.EventType eventType, Listener<CalendarEventDetails> listener, Response.ErrorListener errorListener);

    void getDemographic(Listener<Demographic> listener, Response.ErrorListener errorListener);

    void getAddress(Listener<Address> listener, Response.ErrorListener errorListener);

    Request getReferrals(Listener<Referrals> listener, Response.ErrorListener errorListener);

    Request getAbsences(Listener<Absences> listener, Response.ErrorListener errorListener);

    Request getFinalGrades(FinalGradesType type, Listener<FinalGrades> listener, Response.ErrorListener errorListener);

    Request getPreferences(Listener<FocusPreferences> listener, Response.ErrorListener errorListener);

    Request setPreferences(FocusPreferences preferences, Listener<FocusPreferences> listener, Response.ErrorListener errorListener);

    Request changePassword(String currentPassword, String newPassword, String verifyNewPassword, Listener<PasswordResponse> listener, Response.ErrorListener errorListener);

    boolean isSessionExpired();

    long getSessionTimeout();

    void setSessionTimeout(long sessionTimeout);

    boolean isLoggedIn();

    void setLoggedIn(boolean loggedIn);

    boolean hasSession();

    void cancelAll(RequestQueue.RequestFilter filter);

    enum FinalGradesType {
        COURSE_HISTORY,
        CURRENT_SEMESTER,
        CURRENT_SEMESTER_EXAMS,
        ALL_SEMESTERS,
        ALL_SEMESTERS_EXAMS
    }

    interface Listener<T> {
        /** Called when a response is received. */
        void onResponse(T response);
    }

}
