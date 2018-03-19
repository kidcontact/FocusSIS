package org.kidcontact.focussis.network;

import java.net.URL;

/**
 * Created by slensky on 3/12/18.
 */

public class UrlBuilder {
    public enum FocusUrl {
        LOGIN,
        PORTAL,
        COURSE_PRE,
        SCHEDULE,
        CALENDAR_PRE,
        EVENT_PRE,
        ASSIGNMENT_PRE,
        DEMOGRAPHIC,
        ABSENCES,
        REFERRALS,
        ADDRESS,
        FINAL_GRADES,
        API,
    }
    private static String tld = "https://focus.asdnh.org/";

    public static String get(FocusUrl url) {
        switch (url) {
            case LOGIN:
                return UrlBuilder.tld + "focus/index.php";
            case PORTAL:
                return UrlBuilder.tld + "focus/Modules.php?modname=misc/Portal.php";
            case COURSE_PRE:
                return UrlBuilder.tld + "focus/Modules.php?modname=Grades/StudentGBGrades.php?course_period_id=";
            case SCHEDULE:
                return UrlBuilder.tld + "focus/Modules.php?modname=Scheduling/Schedule.php";
            case CALENDAR_PRE:
                return UrlBuilder.tld + "focus/Modules.php?modname=School_Setup/Calendar.php&";
            case EVENT_PRE:
                return UrlBuilder.tld + "focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&event_id=";
            case ASSIGNMENT_PRE:
                return UrlBuilder.tld + "focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&assignment_id=";
            case DEMOGRAPHIC:
                return UrlBuilder.tld + "focus/Modules.php?modname=Students/Student.php";
            case ABSENCES:
                return UrlBuilder.tld + "focus/Modules.php?modname=Attendance/StudentSummary.php";
            case REFERRALS:
                return UrlBuilder.tld + "focus/Modules.php?force_package=SIS&modname=Discipline/Referrals.php";
            case ADDRESS:
                return UrlBuilder.tld + "focus/Modules.php?modname=Students/Student.php&include=Address";
            case FINAL_GRADES:
                return UrlBuilder.tld + "focus/Modules.php?force_package=SIS&modname=Grades/StudentRCGrades.php";
            case API:
                return UrlBuilder.tld + "focus/API/APIEndpoint.php";
            default:
                return UrlBuilder.tld;
        }
    }
}
