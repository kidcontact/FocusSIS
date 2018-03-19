package org.kidcontact.focussis.network;

import org.kidcontact.focussis.data.CalendarEvent;

/**
 * Created by slensky on 3/21/17.
 */

public class ApiBuilder {
    //private static final String base = "http://perry.kidcontact.org:5000/api/v3/";
    private static final String base = "http://10.10.43.179:5000/api/v3/";
    //private static final String base = "http://192.168.15.136:5000/api/v3/";

    public static String getSessionUrl() {
            return base + "session";
            }

    public static String getPortalUrl() {
            return base + "portal";
            }

    public static String getCourseUrl(String id) {
        return base + "courses/" + id;
    }

    public static String getScheduleUrl() {
        return base + "schedule";
    }

    public static String getCalendarUrl(int year, int month) {
        return base + "calendar/" + Integer.toString(year) + "/" + Integer.toString(month);
    }

    public static String getCalendarEventUrl(CalendarEvent e) {
        if (e.getType() == CalendarEvent.EventType.OCCASION) {
            return base + "calendar/occasions/" + e.getId();
        }
        else if (e.getType() == CalendarEvent.EventType.ASSIGNMENT) {
            return base + "calendar/assignments/" + e.getId();
        }
        return null;
    }

    public static String getDemographicUrl() {
        return base + "demographic";
    }

    public static String getAddressUrl() {
        return base + "address";
    }

    public static String getReferralsUrl() {
        return base + "referrals";
    }

    public static String getAbsencesUrl() {
        return base + "absences";
    }

}
