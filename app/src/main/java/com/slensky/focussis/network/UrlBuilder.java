package com.slensky.focussis.network;

import com.slensky.focussis.school.ASD;
import com.slensky.focussis.util.SchoolSingleton;

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
        PREFERENCES,
        PREFERENCES_PASSWORD
    }

    private static String getTld() {
        return SchoolSingleton.getInstance().getSchool().getFocusTld();
        // return "http://10.0.2.2/";
    }

    public static String get(FocusUrl url) {
        switch (url) {
            case LOGIN:
                return getTld() + "/focus/index.php";
            case LOGOUT:
                return getTld() + "/focus/index.php?logout";
            case PORTAL:
                return getTld() + "/focus/Modules.php?modname=misc/Portal.php";
            case COURSE:
                return getTld() + "/focus/Modules.php?modname=Grades/StudentGBGrades.php?course_period_id=%s";
            case SCHEDULE:
                return getTld() + "/focus/Modules.php?modname=Scheduling/Schedule.php";
            case CALENDAR:
                return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&month=%d&year=%d";
            case EVENT:
                return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&event_id=%s";
            case ASSIGNMENT:
                return getTld() + "/focus/Modules.php?modname=School_Setup/Calendar.php&modfunc=detail&assignment_id=%s";
            case STUDENT:
                return getTld() + "/focus/Modules.php?modname=Students/Student.php";
            case ABSENCES:
                return getTld() + "/focus/Modules.php?modname=Attendance/StudentSummary.php";
            case REFERRALS:
                return getTld() + "/focus/Modules.php?force_package=SIS&modname=Discipline/Referrals.php";
            case FINAL_GRADES:
                return getTld() + "/focus/Modules.php?force_package=SIS&modname=Grades/StudentRCGrades.php";
            case API:
                return getTld() + "/focus/legacy_API/APIEndpoint.php";
            case PREFERENCES:
                return getTld() + "/focus/Modules.php?force_package=SIS&modname=Users/Preferences.php";
            case PREFERENCES_PASSWORD:
                return getTld() + "/focus/Modules.php?modname=Users/Preferences.php&system_tab=&tab=password";
            default:
                return getTld();
        }
    }
}
