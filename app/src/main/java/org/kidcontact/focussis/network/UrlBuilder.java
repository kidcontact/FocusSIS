package org.kidcontact.focussis.network;

/**
 * Created by slensky on 3/12/18.
 */

public class UrlBuilder {
    public enum FocusUrl {
        LOGIN,
        LOGOUT,
        PORTAL,
        COURSE,
        SCHEDULE,
        CALENDAR,
        EVENT,
        ASSIGNMENT,
        STUDENT,
        ABSENCES,
        REFERRALS,
        FINAL_GRADES,
        API,
    }
    private static String tld = "https://focus.asdnh.org/";

    public static String get(FocusUrl url) {
        switch (url) {
            case LOGIN:
                return UrlBuilder.tld + "focus/index.php";
            case LOGOUT:
                return UrlBuilder.tld + "focus/index.php?logout";
            case PORTAL:
                return UrlBuilder.tld + "focus/Modules.php?modname=misc/Portal.php";
            case COURSE:
                return UrlBuilder.tld + "focus/Modules.php?modname=Grades/StudentGBGrades.php?course_period_id=%s";
            case SCHEDULE:
                return UrlBuilder.tld + "focus/Modules.php?modname=Scheduling/Schedule.php";
            case CALENDAR:
                return UrlBuilder.tld + "focus/Modules.php?modname=School_Setup/Calendar.php&month=%d&year=%d";
            case EVENT:
                return UrlBuilder.tld + "focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&event_id=%s";
            case ASSIGNMENT:
                return UrlBuilder.tld + "focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&assignment_id=%s";
            case STUDENT:
                return UrlBuilder.tld + "focus/Modules.php?modname=Students/Student.php";
            case ABSENCES:
                return UrlBuilder.tld + "focus/Modules.php?modname=Attendance/StudentSummary.php";
            case REFERRALS:
                return UrlBuilder.tld + "focus/Modules.php?force_package=SIS&modname=Discipline/Referrals.php";
            case FINAL_GRADES:
                return UrlBuilder.tld + "focus/Modules.php?force_package=SIS&modname=Grades/StudentRCGrades.php";
            case API:
                return UrlBuilder.tld + "focus/API/APIEndpoint.php";
            default:
                return UrlBuilder.tld;
        }
    }
}
