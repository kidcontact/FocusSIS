package com.slensky.focussis.parser;

import android.util.Log;
import android.util.Pair;

import com.joestelmach.natty.DateGroup;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.slensky.focussis.data.AbsenceDay;
import com.slensky.focussis.data.AbsencePeriod;
import com.slensky.focussis.data.Absences;
import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.util.DateUtil;
import com.slensky.focussis.util.JSONUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slensky on 3/29/18.
 */

public class AbsencesParser extends FocusPageParser {
    private static final String TAG = "AbsencesParser";

    @Override
    public Absences parse(String html) {
        Document absences = Jsoup.parse(html);
        Element table = absences.select("table.WhiteDrawHeader").get(1);

        // some absences pages have a field for "Dismissed". others don't
        boolean hasDismissed = true;
        Pattern r1 = Pattern.compile("Absent: ([0-9]+) periods \\(during ([0-9]+) days\\) A Absent ([0-9]+) periods D Dismissed ([0-9]+) periods E Excused Absence ([0-9]+) periods -- ([0-9]+) days Other Marks: ([0-9]+) periods \\(during ([0-9]+) days\\) L Late ([0-9]+) periods T Tardy ([0-9]+) periods M Misc. Activity ([0-9]+) periods O Off Site/Field Trip ([0-9]+) periods");
        Pattern r2 = Pattern.compile("Absent: ([0-9]+) periods \\(during ([0-9]+) days\\) A Absent ([0-9]+) periods E Excused Absence ([0-9]+) periods -- ([0-9]+) days Other Marks: ([0-9]+) periods \\(during ([0-9]+) days\\) L Late ([0-9]+) periods T Tardy ([0-9]+) periods M Misc. Activity ([0-9]+) periods O Off Site/Field Trip ([0-9]+) periods");

        String tableText = table.text();
        Matcher m = r1.matcher(tableText);
        if (!m.find()) {
            hasDismissed = false;
            m = r2.matcher(tableText);
            if (!m.find()) {
                throw new FocusParseException("Regex pattern could not be matched on " + table.text());
            }
            Log.d(TAG, "Absences does not have Dismissed field");

        }

        int periodsAbsent = Integer.parseInt(m.group(1));
        int daysPartiallyAbsent = Integer.parseInt(m.group(2));
        int periodsAbsentUnexcused = Integer.parseInt(m.group(3));

        int idx = 4;
        int periodsDismissed = -1;
        if (hasDismissed) {
            periodsDismissed = Integer.parseInt(m.group(4));
            idx = 5;
        }

        int periodsAbsentExcused = Integer.parseInt(m.group(idx));
        int daysAbsentExcused = Integer.parseInt(m.group(idx + 1));
        int periodsOtherMarks = Integer.parseInt(m.group(idx + 2));
        int daysOtherMarks = Integer.parseInt(m.group(idx + 3));
        int periodsLate = Integer.parseInt(m.group(idx + 4));
        int periodsTardy = Integer.parseInt(m.group(idx + 5));
        int periodsMisc = Integer.parseInt(m.group(idx + 6));
        int periodsOffsite = Integer.parseInt(m.group(idx + 7));

        int start, end;
        String key1 = "Total Full Days Possible: ";
        String key2 = "Total Full Days Attended: ";
        String key3 = "Total Full Days Absent: ";
        String key4 = "Enrollment Dates: ";

        start = absences.text().indexOf(key1) + key1.length();
        end = start + absences.text().substring(start).indexOf(key2);
        float daysPossible = Float.parseFloat(absences.text().substring(start, end));

        start = absences.text().indexOf(key2) + key2.length();
        end = start + absences.text().substring(start).indexOf(key3);
        String attended = absences.text().substring(start, end);
        float daysAttended = Float.parseFloat(attended.substring(0, attended.indexOf(" ")));
        double daysAttendedPercent = Math.round(Float.parseFloat(attended.substring(attended.indexOf("(") + 1, attended.indexOf("%"))) * 100) / 100.0;

        start = absences.text().indexOf(key3) + key3.length();
        end = start + absences.text().substring(start).indexOf(key4);
        String absent = absences.text().substring(start, end);
        float daysAbsent = Float.parseFloat(absent.substring(0, absent.indexOf(" ")));
        double daysAbsentPercent = Math.round(Float.parseFloat(absent.substring(absent.indexOf("(") + 1, absent.indexOf("%"))) * 100) / 100.0;

        Elements headers = absences.select("td.LO_header");
        List<String> periodNames = new ArrayList<>();
        for (int i = 2; i < headers.size(); i++) {
            if (headers.get(i).parent().parent().tagName().equals("thead")) {
                periodNames.add(headers.get(i).text().trim());
            }
            else {
                break;
            }
        }

        List<AbsenceDay> absenceDays = new ArrayList<>();
        int count = 1;
        Element tr = absences.getElementById("LOy_row" + Integer.toString(count));
        while (tr != null) {
            Elements fields = tr.select("td.LO_field");

            if (!fields.isEmpty()) {
                List<DateGroup> groups = DateUtil.nattyDateParser.parse(fields.get(0).text());
                DateTime date = new DateTime(groups.get(0).getDates().get(0));
                Absences.Status dayStatus = Absences.Status.ABSENT;
                if (fields.get(1).text().toLowerCase().split(" ")[0].equals("present")) {
                    dayStatus = Absences.Status.PRESENT;
                }

                List<AbsencePeriod> periods = new ArrayList<>();
                for (int i = 0; i < periodNames.size(); i++) {
                    Element p = fields.get(i + 2);
                    String period = periodNames.get(i);

                    String name = null;
                    String teacher = null;
                    DateTime lastUpdated = null;
                    String lastUpdatedBy = null;

                    Element tooltip = p.selectFirst("div");
                    if (tooltip != null) {

                        String[] data = tooltip.attr("data-tooltip").split("<BR>");
                        HyphenatedCourseInformation courseInfo = parseHyphenatedCourseInformation(data[0], true, false);
                        name = courseInfo.getCourseName();

                        // courses on this page apparently do not always include meeting days
                        // c.put("days", courseInfo.getMeetingDays());

                        teacher = courseInfo.getTeacher();

                        groups = DateUtil.nattyDateParser.parse(data[1].substring("Last Modified: ".length()));
                        lastUpdated = new DateTime(groups.get(0).getDates().get(0));
                        String[] lastUpdatedBySplit = data[2].trim().split(", ");
                        lastUpdatedBy = lastUpdatedBySplit[1] + ' ' + lastUpdatedBySplit[0];
                    }

                    Absences.Status status;
                    String s = p.text().trim().toLowerCase();
                    if (s.isEmpty()) {
                        continue;
                    }
                    else if (s.equals("-")) {
                        continue; // possibly inserted for periods that don't happen that day?
                    }
                    else if (s.equals("a")) {
                        status = Absences.Status.ABSENT;
                    }
                    else if (s.equals("e")) {
                        status = Absences.Status.EXCUSED;
                    }
                    else if (s.equals("l")) {
                        status = Absences.Status.LATE;
                    }
                    else if (s.equals("t")) {
                        status = Absences.Status.TARDY;
                    }
                    else if (s.equals("o")) {
                        status = Absences.Status.OFFSITE;
                    }
                    else {
                        status = Absences.Status.MISC;
                    }

                    periods.add(new AbsencePeriod(lastUpdated, lastUpdatedBy, name, period, status, teacher));
                }

                absenceDays.add(new AbsenceDay(date, periods, dayStatus));
            }

            count += 1;
            tr = absences.getElementById("LOy_row" + Integer.toString(count));
        }

        Pair<List<MarkingPeriod>, List<Integer>> mp = getMarkingPeriods(html);
        return new Absences(mp.first, mp.second, absenceDays, daysPossible, daysAbsent, daysAbsentPercent, daysAttended, daysAttendedPercent, periodsAbsent, periodsAbsentUnexcused, periodsAbsentExcused, periodsDismissed, periodsOtherMarks, periodsLate, periodsTardy, periodsMisc, periodsOffsite, daysPartiallyAbsent, daysAbsentExcused, daysOtherMarks);
    }

}
