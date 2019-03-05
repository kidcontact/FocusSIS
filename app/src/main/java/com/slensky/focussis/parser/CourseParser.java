package com.slensky.focussis.parser;

import android.util.Log;
import android.util.Pair;

import com.joestelmach.natty.DateGroup;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.slensky.focussis.data.Course;
import com.slensky.focussis.data.CourseAssignment;
import com.slensky.focussis.data.CourseCategory;
import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.util.DateUtil;
import com.slensky.focussis.util.JSONUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 3/17/18.
 */

public class CourseParser extends FocusPageParser {
    private static final String TAG = "CourseParser";

    @Override
    public Course parse(String html) {
        Document course = Jsoup.parse(html);

        int start = course.html().indexOf("course_period_id=") + "course_period_id=".length();
        int finish = course.html().substring(start).indexOf('&') + start;
        String id = course.html().substring(start, finish);

        String metadataFull = course.getElementsByTag("title").get(0).text();
        HyphenatedCourseInformation courseInformation = parseHyphenatedCourseInformation(metadataFull, true, false);

        int percentGrade = -1;
        String letterGrade = null;

        List<CourseCategory> categories = null;
        Element categoryTable = course.selectFirst("td.DarkGradientBG");
        boolean hasCategories = categoryTable != null;
        Log.d(TAG, "Course has categories: " + hasCategories);

        /* try method 1 of retrieving the student's grade in the course. If this doesn't work,
         * the grade will likely be found in the category table */
        Element currGradeElement = course.getElementById("currentStudentGrade[]");
        if (currGradeElement != null) {
            String currGrade = currGradeElement.text();
            currGrade = currGrade.replace('\u00A0', ' '); // replace non-breaking space with normal space
            if (currGrade.contains("%")) {
                percentGrade = Integer.parseInt(currGrade.substring(0, currGrade.indexOf('%')));
                letterGrade = currGrade.substring(currGrade.indexOf(' ') + 1);
            }

            /* if the currGradeElement has a parent with this class (the class expected to belong to
             * the category table), then the category table does not really exist. It seems that
             * classes with no categories have their category table "replaced" with this element
             */
            for (Element e : currGradeElement.parents()) {
                if (e.hasClass("DarkGradientBG")) {
                    hasCategories = false;
                    break;
                }
            }
        }


        if (hasCategories) {
            categories = new ArrayList<>();

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
                for (int i = 0; i < td.size(); i++) {
                    String trimmed = td.get(i).text().trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    scores.add(trimmed);
                }

                // the last element at the end of scores is now the weighted grade
                // use this to assign course grade if currGradeElement could not be found
                String weightedGrade = scores.get(scores.size() - 1);
                if (letterGrade == null && weightedGrade.contains("%")) {
                    percentGrade = Integer.parseInt(weightedGrade.substring(0, weightedGrade.indexOf('%')));
                    letterGrade = weightedGrade.substring(weightedGrade.indexOf(' ') + 1);
                }

                for (int i = 0; i < names.size(); i++) {
                    String n = names.get(i), w = weights.get(i), s = scores.get(i);

                    int weight = Integer.parseInt(w.substring(0, w.length() - 1));
                    String letter = null; // letter grade in the category
                    int percent = -1; // percent grade in the category

                    s = s.replace('\u00A0', ' '); // replace non-breaking space with normal space
                    String[] data = s.split("% ");
                    if (data.length == 2) {
                        percent = Integer.parseInt(data[0]);
                        letter = data[1];
                    }

                    categories.add(new CourseCategory(n, letter, percent, weight));
                }
            }
        }

        Pair<List<MarkingPeriod>, List<Integer>> mp = getMarkingPeriods(html);
        MarkingPeriod currentMarkingPeriod = null;
        for (MarkingPeriod m : mp.first) {
            if (m.isSelected()) {
                currentMarkingPeriod = m;
                break;
            }
        }

        /*
            extract data about each assignment from each row of the assignment table
            columns:
              - 0: name
              - 1: grade ratio
              - 2: grade name (percent grade, "Not Graded", etc.)
              - 3: comments
              - 4: assigned date
              - 5: due date
              - 6: category (or assignment files)
              - 7: assignment files (or last modified)
              - 8: last modified (only present if the course has categories)
         */

        List<CourseAssignment> assignments = new ArrayList<>();
        int count = 1;
        Element tr = course.getElementById("LOy_row" + Integer.toString(count));
        while (tr != null) {
            Elements td = tr.select("td.LO_field");
            String name = td.get(0).text();

            String description = null;
            Element div = td.get(0).selectFirst("div");
            if (div != null) {
                int descStart = div.attr("onmouseover").indexOf("\",\"") + 3;
                int descEnd = div.attr("onmouseover").indexOf("\"],[\"");
                description = div.attr("onmouseover").substring(descStart, descEnd);
                description = description.replace("\\r\\n", "\n");
                description = StringEscapeUtils.unescapeJava(description).trim();
            }

            CourseAssignment.Status status = CourseAssignment.Status.OTHER;
            float studentGrade = -1;
            String studentGradeString = null;
            int maxGrade = -1;
            String maxGradeString = null;
            String overallGradeString = null;
            String overallLetterGrade = null; // assignment letter grade
            int overallPercentGrade = -1; // assignment percent grade
            String fullGradeRatioString = null;

            Element gradeImg = tr.selectFirst("img");
            if (gradeImg != null) {
                status = gradeImg.attr("src").equals("assets/check.png") ? CourseAssignment.Status.PASS : CourseAssignment.Status.FAIL;
            }
            else {
                String[] gradeRatio = td.get(1).text().split(" / ");
                if (gradeRatio.length < 2) {
                    Log.w(TAG, "Error parsing grade ratio string, using full string " + td.get(1).text());
                    fullGradeRatioString = td.get(1).text();
                }
                else {
                    if (NumberUtils.isDigits(gradeRatio[1].trim())) {
                        maxGrade = Integer.parseInt(gradeRatio[1].trim());
                    } else {
                        Log.w(TAG, "Error parsing max_grade as int, max_grade: " + gradeRatio[1]);
                        maxGradeString = gradeRatio[1];
                    }

                    String gr0 = gradeRatio[0].toLowerCase();
                    if (gr0.equals("*") || gr0.equals("e") || gr0.equals("excluded")) {
                        status = CourseAssignment.Status.EXCLUDED;
                    }
                    else if (gr0.equals("ng")) {
                        status = CourseAssignment.Status.NOT_GRADED;
                    }
                    else if (gr0.equals("m") || gr0.equals("missing")) {
                        status = CourseAssignment.Status.MISSING;
                    }
                    else if (gradeRatio[1].equals("0") || gr0.equals("extra credit") || gr0.equals("extra") || gr0.equals("ec")) {
                        status = CourseAssignment.Status.EXTRA_CREDIT;
                        if (NumberUtils.isNumber(gradeRatio[0])) {
                            studentGrade = NumberUtils.createNumber(gradeRatio[0]).floatValue();
                        } else {
                            Log.w(TAG, "Error parsing studentGrade as number, studentGrade: " + gradeRatio[0]);
                            studentGradeString = gradeRatio[0];
                        }
                    }
                    else {
                        status = CourseAssignment.Status.GRADED;
                        if (NumberUtils.isNumber(gradeRatio[0])) {
                            studentGrade = NumberUtils.createNumber(gradeRatio[0]).floatValue();
                        } else {
                            Log.w(TAG, "Error parsing studentGrade as number, studentGrade: " + gradeRatio[0]);
                            studentGradeString = gradeRatio[0];
                        }
                    }
                }
                if (status == null || status.equals(CourseAssignment.Status.GRADED)) {
                    String[] overallGrade = td.get(2).text().split("% ");
                    if (NumberUtils.isDigits(overallGrade[0])) {
                        status = CourseAssignment.Status.GRADED;
                        overallPercentGrade = Integer.parseInt(overallGrade[0]);
                        overallLetterGrade = overallGrade[1];
                    } else {
                        status = CourseAssignment.Status.OTHER;
                        overallGradeString = td.get(2).text();
                }
                }
            }

            String comment = null;
            if (!td.get(3).text().trim().isEmpty()) {
                comment = td.get(3).text().trim();
            }

            String category = null;
            if (hasCategories && !td.get(6).text().trim().isEmpty()) {
                category = td.get(6).text().trim();
            }

            List<DateGroup> groups = DateUtil.nattyDateParser.parse(td.get(4).text());
            DateTime assigned = new DateTime(groups.get(0).getDates().get(0));
            groups = DateUtil.nattyDateParser.parse(td.get(5).text());
            DateTime due = new DateTime(groups.get(0).getDates().get(0));

            int lastModifiedIdx = hasCategories ? 8 : 7;
            DateTime lastModified = assigned;
            if (!td.get(lastModifiedIdx).text().trim().isEmpty()) {
                groups = DateUtil.nattyDateParser.parse(td.get(lastModifiedIdx).text());
                lastModified = new DateTime(groups.get(0).getDates().get(0));
            }

            assignments.add(new CourseAssignment(name, assigned, due, lastModified, category, maxGrade, maxGradeString, studentGrade, studentGradeString, fullGradeRatioString, overallLetterGrade, overallPercentGrade, overallGradeString, description, status, currentMarkingPeriod.getId()));

            count += 1;
            tr = course.getElementById("LOy_row" + Integer.toString(count));

        }

        return new Course(mp.first, mp.second, assignments, categories, id, letterGrade, courseInformation.getCourseName(), percentGrade, courseInformation.getPeriod(), courseInformation.getTeacher());
    }

}
