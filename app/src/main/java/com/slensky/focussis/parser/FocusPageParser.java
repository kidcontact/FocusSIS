package com.slensky.focussis.parser;

import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.data.ScheduleCourse;
import com.slensky.focussis.util.TermUtil;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by slensky on 3/12/18.
 * Contains parsing functions common to parsing multiple focus pages
 */

public abstract class FocusPageParser {
    private static final String TAG = "FocusPageParser";
    public abstract Object parse(String html) throws JSONException;

    static protected Pair<List<MarkingPeriod>, List<Integer>> getMarkingPeriods(String html) {
        Document page = Jsoup.parse(html);
        Elements years = page.getElementsByAttributeValue("name", "side_syear").first().children();
        List<Integer> availableYears = new ArrayList<>();
        int selectedYear = -1;
        for (int i = 0; i < years.size(); i++) {
            int y = Integer.parseInt(years.get(i).attr("value"));
            if (years.get(i).hasAttr("selected")) {
                selectedYear = y;
            }
            availableYears.add(y);
        }

        Elements mps = page.getElementsByAttributeValue("name", "side_mp").first().children();
        List<MarkingPeriod> markingPeriods = new ArrayList<>();
        for (Element mp : mps) {
            String id = mp.attr("value");
            String name = mp.text();
            boolean selected = mp.hasAttr("selected");
            markingPeriods.add(new MarkingPeriod(id, selectedYear, selected, name));
        }

        return new Pair<>(markingPeriods, availableYears);
    }

    static String sanitizePhoneNumber(String phoneNumber) {
        return phoneNumber
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .replace(" ", "");
    }

    /* Hyphenated course information (ex. "Physical Education - Period 1 - H - Red - Andrea Tarr")
     * is found on the portal, course, schedule, and absences pages.
     *
     * The course name at index 0 is omitted in the portal alerts block and on the schedule page,
     * and some courses may not have a listed period. Ex:
     *
     *  "Language Arts - - MWF - Red - Mollie Souter van Wagner" (period # missing)
     *  "- MWF - Red - Mollie Souter van Wagner" (no course name, period # missing)
     *
     * The color ("Red") seems to only be present on 6th grade courses. For other grades, this
     * section is replaced with a number (meaning unclear).
     *
     * "Study Period - Period 1 - MWH - 003 - Jennifer Cava"
     *
     * Periods are NOT unique, sixth grade accounts commonly have multiple courses in the same period.
     *
     * Detailed information about information sections on various pages (12/24/18)
     *   Portal courses, featured programs: has course name, has meeting days
     *   Portal courses, alerts: no course name, has meeting days
     *   Schedule: no course name, does not always have meeting days (assume no)
     *   Absences: has course name, does not always have meeting days (assume no)
     *   Calendar event details: no course name, has meeting days, sometimes contains term
     */
    public static class HyphenatedCourseInformation {
        private final String courseName;
        @Nullable private final String period; // "Period x" should be shortened to "x"
        private final boolean periodIsInt;
        private final ScheduleCourse.Term term;
        private final String meetingDays; // ex. "MWH"
        private final String section; // ex. "Red", "003", "12"
        private final String teacher;

        public HyphenatedCourseInformation(String courseName, @Nullable String period, boolean periodIsInt, ScheduleCourse.Term term, String meetingDays, String section, String teacher) {
            this.courseName = courseName;
            this.period = period;
            this.periodIsInt = periodIsInt;
            this.term = term;
            this.meetingDays = meetingDays;
            this.section = section;
            this.teacher = teacher;
        }

        public String getCourseName() {
            return courseName;
        }

        @Nullable
        public String getPeriod() {
            return period;
        }

        public boolean isPeriodIsInt() {
            return periodIsInt;
        }

        public ScheduleCourse.Term getTerm() {
            return term;
        }

        public String getMeetingDays() {
            return meetingDays;
        }

        public String getSection() {
            return section;
        }

        public String getTeacher() {
            return teacher;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HyphenatedCourseInformation that = (HyphenatedCourseInformation) o;
            return periodIsInt == that.periodIsInt &&
                    Objects.equals(courseName, that.courseName) &&
                    Objects.equals(period, that.period) &&
                    term == that.term &&
                    Objects.equals(meetingDays, that.meetingDays) &&
                    Objects.equals(section, that.section) &&
                    Objects.equals(teacher, that.teacher);
        }

        @Override
        public int hashCode() {
            return Objects.hash(courseName, period, periodIsInt, term, meetingDays, section, teacher);
        }

        @Override
        public String toString() {
            return "HyphenatedCourseInformation{" +
                    "courseName='" + courseName + '\'' +
                    ", period='" + period + '\'' +
                    ", periodIsInt=" + periodIsInt +
                    ", term=" + term +
                    ", meetingDays='" + meetingDays + '\'' +
                    ", section='" + section + '\'' +
                    ", teacher='" + teacher + '\'' +
                    '}';
        }
    }

    public static HyphenatedCourseInformation parseHyphenatedCourseInformation(String c, boolean hasCourseName, boolean parseMeetingDays, boolean mightHaveTerm) {
        String courseName = null, period = null, meetingDays = null, section = null, teacher = null;
        boolean periodIsInt = false;
        c = c.trim();

        /* for course information like "- MWF - Red - Mollie Souter van Wagner", add a leading space
         * to make splitting work properly */
        if (c.startsWith("- ")) {
            c = " " + c;
        }

        /* for course information with a blank section like "Language Arts - - MWF - Red - Mollie Souter van Wagner",
         * add a space to make splitting work properly
         */
        c = c.replace(" - - ", " -  - ");

        int i = 0;
        String[] info = c.split(" - ");
        if (hasCourseName) {
            courseName = info[i];
            i++;
        }

        // if the course has a period
        if (!info[i].isEmpty()) {
            period = info[i];
            // shorten "Period x" to "x"
            if (period.toLowerCase().startsWith("period ")) {
                period = info[i].substring("period ".length());
                periodIsInt = NumberUtils.isDigits(period);
            }
        }

        // if course has a section term, it will be after the period term and the info array will have a length 1 longer than anticipated
        ScheduleCourse.Term term = ScheduleCourse.Term.YEAR;
        if (mightHaveTerm) {
            int anticipatedLength = BooleanUtils.toInteger(hasCourseName) + BooleanUtils.toInteger(parseMeetingDays) + 3; /* period, section, teacher */
            if (info.length == anticipatedLength + 1) {
                Log.d("HyphenatedCourseInfoP", c + " has term");
                term = TermUtil.stringToTerm(info[i + 1]);
            }
        }

        // work backwards to get teacher + section + meeting days

        // focus doesn't account for middle names properly in this page, so teachers without
        // middle names have two spaces in between their first and last!
        Log.d("HyphenatedCourseInfoP", c);
        teacher = info[info.length - 1].replace("  ", " ").replace('\u00A0', ' ');

        section = info[info.length - 2];

        if (parseMeetingDays) {
            meetingDays = info[info.length - 3];
        }

        return new HyphenatedCourseInformation(courseName, period, periodIsInt, term, meetingDays, section, teacher);
    }

    public static HyphenatedCourseInformation parseHyphenatedCourseInformation(String c, boolean hasCourseName, boolean parseMeetingDays) {
        return parseHyphenatedCourseInformation(c, hasCourseName, parseMeetingDays, false);
    }

}
