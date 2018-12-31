package com.slensky.focussis.util;

import com.slensky.focussis.data.ScheduleCourse;

/**
 * Created by slensky on 4/28/17.
 */

public class TermUtil {

    public static ScheduleCourse.Term stringToTerm(String term) {
        term = term.toLowerCase();
        if (term.equals("q1")) {
            return ScheduleCourse.Term.Q1;
        }
        else if (term.equals("q2")) {
            return ScheduleCourse.Term.Q2;
        }
        else if (term.equals("q3")) {
            return ScheduleCourse.Term.Q3;
        }
        else if (term.equals("q4")) {
            return ScheduleCourse.Term.Q4;
        }
        else if (term.equals("s1")) {
            return ScheduleCourse.Term.S1;
        }
        else if (term.equals("s2")) {
            return ScheduleCourse.Term.S2;
        }
        else {
            return ScheduleCourse.Term.YEAR;
        }
    }

    public static String termToString(ScheduleCourse.Term term) {
        switch (term) {
            case Q1:
                return "Quarter 1";
            case Q2:
                return "Quarter 2";
            case Q3:
                return "Quarter 3";
            case Q4:
                return "Quarter 4";
            case S1:
                return "Semester 1";
            case S2:
                return "Semester 2";
            default:
                return "Full Year";
        }
    }

    public static  String termToStringAbbr(ScheduleCourse.Term term) {
        switch (term) {
            case Q1:
                return "Q1";
            case Q2:
                return "Q2";
            case Q3:
                return "Q3";
            case Q4:
                return "Q4";
            case S1:
                return "S1";
            case S2:
                return "S2";
            default:
                return "Full Year";
        }
    }

}
