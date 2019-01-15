package com.slensky.focussis.data.network;

import com.slensky.focussis.util.SchoolSingleton;

/**
 * Created by slensky on 3/12/18.
 */

public class FocusEndpoints {

    private static String getTld() {
        return SchoolSingleton.getInstance().getSchool().getFocusTld();
        // return "http://10.0.2.2/";
    }

    public static String getLoginEndpoint() {
        return getTld() + "/focus/index.php";
    }

    public static String getLogoutEndpoint() {
        return getTld() + "/focus/index.php?logout";
    }

    public static String getPortalEndpoint() {
        return getTld() + "/focus/Modules.php?modname=misc/Portal.php";
    }

    public static String getCourseEndpoint(String coursePeriodId) {
        return getTld() + "/focus/Modules.php?modname=Grades/StudentGBGrades.php?course_period_id=" + coursePeriodId;
    }

    public static String getScheduleEndpoint() {
        return getTld() + "/focus/Modules.php?modname=Scheduling/Schedule.php";
    }

    public static String getCalendarEndpoint(int month, int year) {
        return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&month=" + month + "&year=" + year;
    }

    public static String getCalendarEventEndpoint(String eventId) {
        return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&event_id=" + eventId;
    }

    public static String getCalendarAssignmentEndpoint(String assignmentId) {
        return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&assignment_id=" + assignmentId;
    }

    public static String getStudentEndpoint() {
        return getTld() + "/focus/Modules.php?modname=Students/Student.php";
    }

    public static String getAbsencesEndpoint() {
        return getTld() + "/focus/Modules.php?modname=Attendance/StudentSummary.php";
    }

    public static String getReferralsEndpoint() {
        return getTld() + "/focus/Modules.php?force_package=SIS&modname=Discipline/Referrals.php";
    }

    public static String getFinalGradesEndpoint() {
        return getTld() + "/focus/Modules.php?force_package=SIS&modname=Grades/StudentRCGrades.php";
    }

    public static String getLegacyApiEndpoint() {
        return getTld() + "/focus/legacy_API/APIEndpoint.php";
    }

    public static String getPreferencesEndpoint() {
        return getTld() + "/focus/Modules.php?force_package=SIS&modname=Users/Preferences.php";
    }

    public static String getPasswordChangeEndpoint() {
        return getTld() + "/focus/Modules.php?modname=Users/Preferences.php&system_tab=&tab=password";
    }

}
