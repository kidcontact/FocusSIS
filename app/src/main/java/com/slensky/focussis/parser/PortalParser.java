package com.slensky.focussis.parser;

import android.util.Pair;

import com.joestelmach.natty.DateGroup;
import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.data.Portal;
import com.slensky.focussis.data.PortalAlert;
import com.slensky.focussis.data.PortalAssignment;
import com.slensky.focussis.data.PortalCourse;
import com.slensky.focussis.data.PortalEvent;
import com.slensky.focussis.util.JSONUtil;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import com.slensky.focussis.util.DateUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 3/12/18.
 */

public class PortalParser extends FocusPageParser {
    private static final String TAG = "PortalParser";

    @Override
    public Portal parse(String html) {
        Document portal = Jsoup.parse(html);

        List<PortalCourse> courses = new ArrayList<>();

        Elements featuredProgramsRows = portal.select("[class^=portal_block_Featured] table table table > tbody > tr ~ tr");
        for (Element tr : featuredProgramsRows) {
            String urlStart = "Modules.php?modname=Grades/StudentGBGrades.php?course_period_id=";
            Element courseNameLink = tr.selectFirst("a[href^=" + urlStart + "]");
            if (courseNameLink == null) {
                continue; // this isn't a course row
            }

            String id = courseNameLink.attr("href").substring(urlStart.length());

            // \u00A0 is a nonbreaking space, sometimes found in Focus fields inconsistently + for unknown reasons
            String name = courseNameLink.text().replace('\u00A0', ' ').trim();

            // remember, courseNameLink is a <a>, so the parent of that is the <td> child of tr
            Element periodTd = tr.child(courseNameLink.parent().elementSiblingIndex() + 1);
            String period = periodTd.text().replace('\u00A0', ' ').trim();
            if (period.toLowerCase().startsWith("period ")) {
                period = period.substring("period ".length());
            }

            Element teacherTd = tr.child(periodTd.elementSiblingIndex() + 1);
            String teacher = teacherTd.text().replace('\u00A0', ' ').trim();
            // there are double spaces in the name field sometimes, make sure to get rid of double or triple spaces (to be safe)
            teacher.replace("  ", " ").replace("  ", " ");

            // the course grade link is the first link with the appropriate prefix that appears after teacherTd
            Element courseGradeLink = null;
            for (int i = teacherTd.elementSiblingIndex() + 1; i < tr.childNodeSize(); i++) {
                courseGradeLink = tr.child(i).selectFirst("a[href^=" + urlStart + "]");
                if (courseGradeLink != null) {
                    break;
                }
            }
            String courseGrade = courseGradeLink.text();
            int courseGradePercent = -1;
            String courseGradeLetter = null;
            if (courseGrade.contains("%")) {
                courseGradePercent = Integer.parseInt(courseGrade.substring(0, courseGrade.indexOf('%')));
                courseGradeLetter = courseGrade.substring(courseGrade.indexOf(' ') + 1);
            }

            Element emailInput = tr.selectFirst("input.EmailTeacher");
            String teacherEmail = null;
            if (emailInput != null) {
                teacherEmail = emailInput.attr("value");
            }

            courses.add(new PortalCourse(new ArrayList<PortalAssignment>(), id, courseGradeLetter, name, courseGradePercent, period, teacher, teacherEmail));
        }

        List<PortalEvent> events = new ArrayList<>();
        Element upcoming = portal.selectFirst("td.portal_block_Upcoming");
        Elements links = upcoming.getElementsByTag("a");
        final ArrayList<String> comments = new ArrayList<>();
        upcoming.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof Comment) {
                    comments.add(((Comment) node).getData().trim());
                }
            }

            @Override
            public void tail(Node node, int depth) {
                // unused
            }
        });

        for (int i = 0; i < comments.size(); i++) {
            Element a = links.get(i + 1);
            String c = comments.get(i);
            if (a.text().contains(":")) {
                String description = a.text().substring(a.text().indexOf(": ") + 2);
                int year = Integer.parseInt(c.substring(0, 4));
                int month = Integer.parseInt(c.substring(4, 6));
                int day = Integer.parseInt(c.substring(6, 8));
                DateTime date = new DateTime(year, month, day, 0, 0);
                events.add(new PortalEvent(description, date));
            }
        }

        Element alerts = portal.selectFirst("td.portal_block_Alerts").selectFirst("td.BoxContent");
        Element ul = alerts.selectFirst("ul");
        if (ul != null) {
            Elements li = ul.getElementsByTag("li");

            // id to use for new courses if necessary (courses that have assignments but show up nowhere else
            // these courses should not be shown on the main course page
            String newCourseId = "-1";
            for (int i = 0; i < li.size(); i+= 2) {
                HyphenatedCourseInformation courseInformation = parseHyphenatedCourseInformation(li.get(i).text(), false, true);

                // attempt to match the assignments to an existing course
                // a course is considered a match if it shares the same period and teacher as these assignments
                // if the course has no listed period, just try to match the teacher
                PortalCourse matchedCourse = null;
                for (PortalCourse course : courses) {
                    if ((course.getPeriod().equals(courseInformation.getPeriod()) && course.getTeacher().equals(courseInformation.getTeacher()))
                            || (courseInformation.getPeriod() == null && course.getTeacher().equals(courseInformation.getTeacher()))) {
                        matchedCourse = course;
                        break;
                    }
                }

                if (matchedCourse == null) {
                    // most likely an advisory assignment, so make an advisory course to place it in
                    String email = null;
                    for (PortalCourse course : courses) {
                        if (course.getTeacher().equals(courseInformation.getTeacher())
                                && course.getTeacherEmail() != null) {
                            email = course.getTeacherEmail();
                            break;
                        }
                    }

                    matchedCourse = new PortalCourse(null, newCourseId, null,
                            "Advisory", -1, "Advisory", courseInformation.getTeacher(), email);
                    courses.add(matchedCourse);
                    newCourseId = Integer.toString(Integer.parseInt(newCourseId) - 1);
                }

                List<PortalAssignment> assignments = new ArrayList<>();
                for (Element tr : li.get(i + 1).getElementsByTag("tr")) {
                    Elements td = tr.getElementsByTag("td");
                    String name = td.get(0).text().replace("\n", "");

                    List<DateGroup> groups = DateUtil.nattyDateParser.parse(td.get(1).text().trim().substring(5));
                    DateTime due = new DateTime(groups.get(0).getDates().get(0));
                    assignments.add(new PortalAssignment(name, due, matchedCourse.getName(),
                            matchedCourse.getPeriod(), matchedCourse.isAdvisory(), matchedCourse.getTeacher(),
                            matchedCourse.getTeacherEmail()));
                }
                matchedCourse.setAssignments(assignments);
            }
        }

        Elements alertLinks = alerts.select(":root > a");
        List<PortalAlert> alertsList = new ArrayList<>();
        for (Element a: alertLinks) {
            String message = a.text().replace("\n", "").trim();
            String url = a.attr("href");
            alertsList.add(new PortalAlert(message, url));
        }

        Pair<List<MarkingPeriod>, List<Integer>> mp = getMarkingPeriods(html);
        return new Portal(courses, events, alertsList, mp.first, mp.second);
    }

}
