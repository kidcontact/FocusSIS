package org.kidcontact.focussis.parser;

import android.util.Log;

import com.joestelmach.natty.DateGroup;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kidcontact.focussis.util.DateUtil;
import org.kidcontact.focussis.util.JSONUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by slensky on 3/17/18.
 */

public class CourseParser extends PageParser {
    private static final String TAG = "CourseParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        Document course = Jsoup.parse(html);
        JSONObject json = new JSONObject();

        int start = course.html().indexOf("course_period_id=") + "course_period_id=".length();
        int finish = course.html().substring(start).indexOf('&') + start;
        json.put("id", course.html().substring(start, finish));

        String metadataFull = course.getElementsByTag("title").get(0).text();
        String[] metadata = metadataFull.split(" - ");
        json.put("name", metadata[0]);
        String period = metadata[1].toLowerCase();
        if (period.contains("period")) {
            json.put("period", Integer.parseInt(period.substring("period ".length())));
        }
        else {
            json.put("period", "advisory");
        }
        StringBuilder teacherName = new StringBuilder();
        for (String n: metadata[metadata.length - 1].split(" ")) {
            if (!n.isEmpty()) {
                teacherName.append(n + " ");
            }
        }
        teacherName.deleteCharAt(teacherName.length() - 1);
        json.put("teacher", teacherName.toString());

        Element categoryTable = course.selectFirst("td.DarkGradientBG");
        if (categoryTable != null) {
            String currGrade = course.getElementById("currentStudentGrade[]").text();
            currGrade = currGrade.replace('\u00A0', ' '); // replace non-breaking space with normal space
            if (currGrade.contains("%")) {
                json.put("percent_grade", Integer.parseInt(currGrade.substring(0, currGrade.indexOf('%'))));
                json.put("letter_grade", currGrade.substring(currGrade.indexOf(' ') + 1));
            }

            JSONArray categories = new JSONArray();

            Elements tr = categoryTable.getElementsByTag("tr");
            if (tr.size() > 0) {
                List<String> names = new ArrayList<>();
                Elements td = tr.get(0).getElementsByTag("td");
                for (int i = 0; i < td.size() - 1; i++) {
                    String trimmed = td.get(i).text().trim();
                    if (trimmed.isEmpty() || trimmed.equals("Weighted Grade")) {
                        continue;
                    }
                    names.add(trimmed);
                }

                List<String> weights = new ArrayList<>();
                for (Element e : tr.get(1).getElementsByTag("td")) {
                    if (e.text().trim().isEmpty()) {
                        continue;
                    }
                    weights.add(e.text().trim());
                }

                List<String> scores = new ArrayList<>();
                td = tr.get(2).getElementsByTag("td");
                for (int i = 0; i < td.size() - 1; i++) {
                    String trimmed = td.get(i).text().trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    scores.add(trimmed);
                }

                for (int i = 0; i < names.size(); i++) {
                    String n = names.get(i), w = weights.get(i), s = scores.get(i);
                    JSONObject category = new JSONObject();
                    category.put("name", n);
                    category.put("percent_weight", Integer.parseInt(w.substring(0, w.length() - 1)));
                    s = s.replace('\u00A0', ' '); // replace non-breaking space with normal space
                    String[] data = s.split("% ");
                    if (data.length == 2) {
                        category.put("percent_grade", Integer.parseInt(data[0]));
                        category.put("letter_grade", data[1]);
                    }
                    categories.put(category);
                }

                json.put("categories", categories);

            }
        }


        JSONArray assignments = new JSONArray();
        int count = 1;
        Element tr = course.getElementById("LOy_row" + Integer.toString(count));
        while (tr != null) {
            Elements td = tr.select("td.LO_field");
            JSONObject assignment = new JSONObject();
            String name = td.get(0).text();

            Element div = td.get(0).selectFirst("div");
            if (div != null) {
                int descStart = div.attr("onmouseover").indexOf("\",\"") + 3;
                int descEnd = div.attr("onmouseover").indexOf("\"],[\"");
                String description = div.attr("onmouseover").substring(descStart, descEnd);
                description = description.replace("\\r\\n", "\n");
                assignment.put("description", StringEscapeUtils.unescapeJava(description));
            }

            String status;
            Element gradeImg = tr.selectFirst("img");
            if (gradeImg != null) {
                status = gradeImg.attr("src").equals("assets/check.png") ? "pass" : "fail";
            }
            else {
                String[] gradeRatio = td.get(1).text().split(" / ");
                try {
                    assignment.put("max_grade", Integer.parseInt(gradeRatio[1]));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Error parsing max_grade as int, max_grade: " + gradeRatio[1]);
                    assignment.put("max_grade_string", td.get(1).text());
                }
                gradeRatio[0] = gradeRatio[0].toLowerCase();
                if (gradeRatio[0].equals("*") || gradeRatio[0].equals("e") || gradeRatio[0].equals("excluded")) {
                    status = "excluded";
                }
                else if (gradeRatio[0].equals("ng")) {
                    status = "ng";
                }
                else if (gradeRatio[0].equals("m") || gradeRatio[0].equals("missing")) {
                    status = "missing";
                }
                else if (gradeRatio[1].equals("0") || gradeRatio[0].equals("extra credit") || gradeRatio[0].equals("extra") || gradeRatio[0].equals("ec")) {
                    status = "extra";
                    try {
                        assignment.put("student_grade", Float.parseFloat(gradeRatio[0]));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Error parsing student_grade as int, student_grade: " + gradeRatio[0]);
                        assignment.put("student_grade_string", td.get(1).text());
                    }
                }
                else {
                    status = "graded";
                    try {
                        assignment.put("student_grade", Float.parseFloat(gradeRatio[0]));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Error parsing student_grade as int, student_grade: " + gradeRatio[0]);
                        assignment.put("student_grade_string", td.get(1).text());
                    }

                    String[] overallGrade = td.get(2).text().split("% ");
                    try {
                        assignment.put("percent_overall_grade", Integer.parseInt(overallGrade[0]));
                        assignment.put("letter_overall_grade", overallGrade[1]);
                    } catch (NumberFormatException e) {
                        assignment.put("overall_grade_string", td.get(2).text());
                    }
                }
            }

            if (!td.get(3).text().trim().isEmpty()) {
                assignment.put("comment", td.get(3).text().trim());
            }

            if (!td.get(6).text().trim().isEmpty()) {
                assignment.put("category", td.get(6).text().trim());
            }

            List<DateGroup> groups = DateUtil.nattyDateParser.parse(td.get(4).text());
            String assigned = DateUtil.ISO_DATE_FORMATTER.format(groups.get(0).getDates().get(0));
            groups = DateUtil.nattyDateParser.parse(td.get(5).text());
            String due = DateUtil.ISO_DATE_FORMATTER.format(groups.get(0).getDates().get(0));

            assignment.put("name", name);
            assignment.put("status", status);
            assignment.put("assigned", assigned);
            assignment.put("due", due);

            assignments.put(assignment);
            count += 1;
            tr = course.getElementById("LOy_row" + Integer.toString(count));

        }

        if (assignments.length() > 0) {
            json.put("assignments", assignments);
        }

        return JSONUtil.concatJson(json, this.getMarkingPeriods(html));
    }

}
