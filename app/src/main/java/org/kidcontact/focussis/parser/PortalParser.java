package org.kidcontact.focussis.parser;

import android.util.Log;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.kidcontact.focussis.util.DateUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 3/12/18.
 */

public class PortalParser extends PageParser {
    private static final String TAG = "PortalParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        Document portal = Jsoup.parse(html);
        JSONObject json = new JSONObject();

        Element featuredProgs = portal.selectFirst("td:contains(Featured Programs)").parent().parent();
        Elements links = featuredProgs.getElementsByTag("a");

        JSONObject courses = new JSONObject();
        String urlStart = "Modules.php?modname=Grades/StudentGBGrades.php?course_period_id=";
        for (Element a : links) {
            if (a.hasAttr("href") && a.attr("href").startsWith(urlStart)) {
                String id = a.attr("href").substring(urlStart.length());
                if (!courses.has(id)) {
                    JSONObject course = new JSONObject();
                    course.put("id", id);
                    courses.put(id, course);
                }
                JSONObject course = courses.getJSONObject(id);

                String t = a.text().replace('\u00A0', ' '); // replace non-breaking space with normal space
                if (t.contains("%")) {
                    course.put("percent_grade", Integer.parseInt(t.substring(0, t.indexOf('%'))));
                    course.put("letter_grade", t.substring(t.indexOf(' ') + 1));
                }
                else if (t.contains("Period")) {
                    String[] data = t.split(" - ");
                    course.put("name", data[0]);
                    course.put("period", Integer.parseInt(data[1].substring("Period ".length())));
                    course.put("days", data[data.length - 3]);
                    StringBuilder teacherName = new StringBuilder();
                    for (String n: data[data.length - 1].split(" ")) {
                        if (!n.isEmpty()) {
                            teacherName.append(n + " ");
                        }
                    }
                    teacherName.deleteCharAt(teacherName.length() - 1);
                    course.put("teacher", teacherName.toString());
                }
            }
        }

        JSONArray events = new JSONArray();
        Element upcoming = portal.selectFirst("td.portal_block_Upcoming");
        links = upcoming.getElementsByTag("a");
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
                int period = -1;
                try {
                    period = Integer.parseInt(periodStr.substring(periodStr.length() - 1));
                } catch (NumberFormatException e) {
                    // assignment belongs to advisory
                }

                JSONObject course = null;
                Iterator<?> keys = courses.keys();
                while(keys.hasNext()) {
                    String key = (String) keys.next();
                    if (courses.get(key) instanceof JSONObject && ((JSONObject) courses.get(key)).getInt("period") == period) {
                        course = (JSONObject) courses.get(key);
                        break;
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
                    newCourse.put("period", -1);
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
                    newCourse.put("teacher", teacherName.toString());
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
        return this.concatJson(json, this.getMarkingPeriods(html));
    }

}
