package com.slensky.focussis.parser;

import android.support.annotation.Nullable;

import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;

/**
 * Created by slensky on 3/12/18.
 * Contains parsing functions common to parsing multiple focus pages
 */

public abstract class FocusPageParser {
    private static final String TAG = "FocusPageParser";
    public abstract Object parse(String html) throws JSONException;

    static protected JSONObject getMarkingPeriods(String html) throws JSONException {
        Document page = Jsoup.parse(html);
        JSONObject json = new JSONObject();

        Elements years = page.getElementsByAttributeValue("name", "side_syear").first().children();
        JSONArray availableYears = new JSONArray();
        int selectedYear = -1;
        for (int i = 0; i < years.size(); i++) {
            if (years.get(i).hasAttr("selected")) {
                selectedYear = Integer.parseInt(years.get(i).attr("value"));
            }
            availableYears.put(Integer.parseInt(years.get(i).attr("value")));
        }

        Elements mps = page.getElementsByAttributeValue("name", "side_mp").first().children();
        JSONObject markingPeriods = new JSONObject();
        for (Element mp : mps) {
            JSONObject mpd = new JSONObject();
            mpd.put("id", mp.attr("value"));
            mpd.put("name", mp.text());
            mpd.put("year", selectedYear);
            if (mp.hasAttr("selected")) {
                mpd.put("selected", true);
            }
            markingPeriods.put(mp.attr("value"), mpd);
        }

        json.put("mps", markingPeriods);
        json.put("mp_years", availableYears);

        return json;
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
     */
    public static class HyphenatedCourseInformation {
        private final String courseName;
        @Nullable private final String period; // "Period x" should be shortened to "x"
        private final boolean periodIsInt;
        private final String meetingDays; // ex. "MWH"
        private final String section; // ex. "Red", "003", "12"
        private final String teacher;

        public HyphenatedCourseInformation(String courseName, @Nullable String period, boolean periodIsInt, String meetingDays, String section, String teacher) {
            this.courseName = courseName;
            this.period = period;
            this.periodIsInt = periodIsInt;
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
                    Objects.equals(meetingDays, that.meetingDays) &&
                    Objects.equals(section, that.section) &&
                    Objects.equals(teacher, that.teacher);
        }

        @Override
        public int hashCode() {

            return Objects.hash(courseName, period, periodIsInt, meetingDays, section, teacher);
        }

        @Override
        public String toString() {
            return "HyphenatedCourseInformation{" +
                    "courseName='" + courseName + '\'' +
                    ", period='" + period + '\'' +
                    ", periodIsInt=" + periodIsInt +
                    ", meetingDays='" + meetingDays + '\'' +
                    ", section='" + section + '\'' +
                    ", teacher='" + teacher + '\'' +
                    '}';
        }

    }

    public static HyphenatedCourseInformation parseHyphenatedCourseInformation(String c, boolean hasCourseName, boolean parseMeetingDays) {
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

        if (parseMeetingDays) {
            meetingDays = info[i + 1];
        }

        // work backwards to get teacher + section

        // focus doesn't account for middle names properly in this page, so teachers without
        // middle names have two spaces in between their first and last!
        teacher = info[info.length - 1].replace("  ", " ");

        section = info[info.length - 2];

        return new HyphenatedCourseInformation(courseName, period, periodIsInt, meetingDays, section, teacher);
    }

}
