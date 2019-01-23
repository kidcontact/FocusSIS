package com.slensky.focussis.data.network;

import com.slensky.focussis.util.SchoolSingleton;

/**
 * Created by slensky on 3/12/18.
 */

class FocusEndpoints {

    private static String getTld() {
        return SchoolSingleton.getInstance().getSchool().getFocusTld();
        // return "http://10.0.2.2/";
    }

    static String getLoginEndpoint() {
        return getTld() + "/focus/index.php";
    }

    static String getLogoutEndpoint() {
        return getTld() + "/focus/index.php?logout";
    }

    static String getPortalEndpoint() {
        return getTld() + "/focus/Modules.php?modname=misc/Portal.php";
    }

    static String getCourseEndpoint(String coursePeriodId) {
        return getTld() + "/focus/Modules.php?modname=Grades/StudentGBGrades.php?course_period_id=" + coursePeriodId;
    }

    static String getScheduleEndpoint() {
        return getTld() + "/focus/Modules.php?modname=Scheduling/Schedule.php";
    }

    static String getCalendarEndpoint(int month, int year) {
        return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&month=" + month + "&year=" + year;
    }

    static String getCalendarEventEndpoint(String eventId) {
        return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&event_id=" + eventId;
    }

    static String getCalendarAssignmentEndpoint(String assignmentId) {
        return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&assignment_id=" + assignmentId;
    }

    static String getStudentEndpoint() {
        return getTld() + "/focus/Modules.php?modname=Students/Student.php";
    }

    static String getAbsencesEndpoint() {
        return getTld() + "/focus/Modules.php?modname=Attendance/StudentSummary.php";
    }

    static String getReferralsEndpoint() {
        return getTld() + "/focus/Modules.php?force_package=SIS&modname=Discipline/Referrals.php";
    }

    static String getFinalGradesEndpoint() {
        return getTld() + "/focus/Modules.php?force_package=SIS&modname=Grades/StudentRCGrades.php";
    }

    static String getLegacyApiEndpoint() {
        return getTld() + "/focus/legacy_API/APIEndpoint.php";
    }

    static String getPreferencesEndpoint() {
        return getTld() + "/focus/Modules.php?force_package=SIS&modname=Users/Preferences.php";
    }

    static String getPasswordChangeEndpoint() {
        return getTld() + "/focus/Modules.php?modname=Users/Preferences.php&system_tab=&tab=password";
    }

}
