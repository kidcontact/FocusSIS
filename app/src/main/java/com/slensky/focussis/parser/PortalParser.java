package com.slensky.focussis.parser;

import com.joestelmach.natty.DateGroup;
import com.slensky.focussis.util.JSONUtil;

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
    public JSONObject parse(String html) throws JSONException {
        Document portal = Jsoup.parse(html);
        JSONObject json = new JSONObject();

        JSONObject courses = new JSONObject();

        Elements featuredProgramsRows = portal.select("[class^=portal_block_Featured] table table table > tbody > tr ~ tr");
        for (Element tr : featuredProgramsRows) {
            String urlStart = "Modules.php?modname=Grades/StudentGBGrades.php?course_period_id=";
            Element courseNameLink = tr.selectFirst("a[href^=" + urlStart + "]");
            if (courseNameLink == null) {
                continue; // this isn't a course row
            }

            JSONObject course = new JSONObject();

            String id = courseNameLink.attr("href").substring(urlStart.length());
            course.put("id", id);

            // \u00A0 is a nonbreaking space, sometimes found in Focus fields inconsistently + for unknown reasons
            String name = courseNameLink.text().replace('\u00A0', ' ').trim();
            course.put("name", name);

            // remember, courseNameLink is a <a>, so the parent of that is the <td> child of tr
            Element periodTd = tr.child(courseNameLink.parent().elementSiblingIndex() + 1);
            String period = periodTd.text().replace('\u00A0', ' ').trim();
            if (period.toLowerCase().startsWith("period ")) {
                period = period.substring("period ".length());
            }
            course.put("period", period);

            Element teacherTd = tr.child(periodTd.elementSiblingIndex() + 1);
            String teacher = teacherTd.text().replace('\u00A0', ' ').trim();
            // there are double spaces in the name field sometimes, make sure to get rid of double or triple spaces (to be safe)
            teacher.replace("  ", " ").replace("  ", " ");
            course.put("teacher", teacher);

            // the course grade link is the first link with the appropriate prefix that appears after teacherTd
            Element courseGradeLink = null;
            for (int i = teacherTd.elementSiblingIndex() + 1; i < tr.childNodeSize(); i++) {
                courseGradeLink = tr.child(i).selectFirst("a[href^=" + urlStart + "]");
                if (courseGradeLink != null) {
                    break;
                }
            }
            String courseGrade = courseGradeLink.text();
            if (courseGrade.contains("%")) {
                course.put("percent_grade", Integer.parseInt(courseGrade.substring(0, courseGrade.indexOf('%'))));
                course.put("letter_grade", courseGrade.substring(courseGrade.indexOf(' ') + 1));
            }

            Element emailInput = tr.selectFirst("input.EmailTeacher");
            if (emailInput != null) {
                course.put("teacher_email", emailInput.attr("value"));
            }

            courses.put(id, course);
        }

        JSONArray events = new JSONArray();
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
                JSONObject event = new JSONObject();
                event.put("description", a.text().substring(a.text().indexOf(": ") + 2));
                String year = c.substring(0, 4);
                String month = c.substring(4, 6);
                String day = c.substring(6, 8);
                event.put("date", year + "-" + month + "-" + day + "T00:00:00");
                events.put(event);
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
                String[] data = li.get(i).text().split(" - ");
                String periodStr = data[0];
                String period = periodStr;
                if (periodStr.toLowerCase().startsWith("period ")) {
                    period = periodStr.substring(periodStr.length() - 1);
                }

                String teacher = data[data.length - 1].replace('\u00A0', ' ').trim();
                // there are double spaces in the name field sometimes, make sure to get rid of double or triple spaces (to be safe)
                teacher.replace("  ", " ").replace("  ", " ");

                // attempt to match the assignments to an existing course
                // a course is considered a match if it shares the same period as these assignments, but sharing the same teacher name is preferred
                JSONObject course = null;
                boolean matchesTeacher = false;
                Iterator<?> keys = courses.keys();
                while(keys.hasNext()) {
                    String key = (String) keys.next();
                    if (courses.get(key) instanceof JSONObject && courses.getJSONObject(key).getString("period").equals(period)) {
                        matchesTeacher = courses.getJSONObject(key).getString("teacher").equals(teacher);
                        course = (JSONObject) courses.get(key);
                        if (matchesTeacher) {
                            break;
                        }
                    }
                }

                JSONArray assignments = new JSONArray();
                for (Element tr : li.get(i + 1).getElementsByTag("tr")) {
                    JSONObject a = new JSONObject();
                    Elements td = tr.getElementsByTag("td");
                    a.put("name", td.get(0).text().replace("\n", ""));

                    List<DateGroup> groups = DateUtil.nattyDateParser.parse(td.get(1).text().trim().substring(5));
                    String date = DateUtil.ISO_DATE_FORMATTER.format(groups.get(0).getDates().get(0));
                    a.put("due", date);
                    assignments.put(a);
                }

                if (course != null) {
                    course.put("assignments", assignments);
                }
                else {
                    // most likely an advisory assignment, so make an advisory course to place it in
                    JSONObject newCourse = new JSONObject();
                    newCourse.put("period", 0);
                    newCourse.put("id", newCourseId);
                    newCourse.put("name", data[0]);
                    newCourse.put("days", data[1]);
                    StringBuilder teacherName = new StringBuilder();
                    for (String n : data[data.length - 1].split(" ")) {
                        if (!n.isEmpty()) {
                            teacherName.append(n + " ");
                        }
                    }
                    teacherName.deleteCharAt(teacherName.length() - 1);
                    String newTeacher = teacherName.toString();
                    newCourse.put("teacher", newTeacher);

                    JSONObject c = null;
                    Iterator<?> k = courses.keys();
                    while(k.hasNext()) {
                        String key = (String) k.next();
                        if (courses.get(key) instanceof JSONObject
                                && courses.getJSONObject(key).getString("teacher").equals(newTeacher)
                                && courses.getJSONObject(key).has("teacher_email")) {
                            newCourse.put("teacher_email", courses.getJSONObject(key).getString("teacher_email"));
                            break;
                        }
                    }


                    newCourse.put("assignments", assignments);
                    courses.put(newCourseId, newCourse);
                    newCourseId = Integer.toString(Integer.parseInt(newCourseId) - 1);
                }
            }
        }

        Elements alertLinks = alerts.select(":root > a");
        JSONArray alertsJson = new JSONArray();
        for (Element a: alertLinks) {
            JSONObject alertJson = new JSONObject();
            alertJson.put("message", a.text().replace("\n", "").trim());
            alertJson.put("url", a.attr("href"));
            alertsJson.put(alertJson);
        }

        if (alertsJson.length() > 0) {
            json.put("alerts", alertsJson);
        }
        if (courses.length() > 0) {
            json.put("courses", courses);
        }
        if (events.length() > 0) {
            json.put("events", events);
        }
        return JSONUtil.concatJson(json, this.getMarkingPeriods(html));
    }

}
