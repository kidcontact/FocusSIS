package com.slensky.focussis.parser;

import android.util.Log;

import com.slensky.focussis.data.FinalGrade;
import com.slensky.focussis.data.FinalGrades;

import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesParser extends FocusPageParser {
    private static final String TAG = "FinalGradesParser";
    private JSONObject json;

    @Override
    public FinalGrades parse(String jsonStr) {
        JSONObject json;
        try {
            json = new JSONObject(jsonStr);
        } catch (JSONException e) {
            throw new FocusParseException("Final grades API response is not valid JSON", e);
        }

        List<FinalGrade> finalGrades = new ArrayList<>();

        try {
            if (json.getJSONObject("result").get("grades") instanceof JSONObject) {
                JSONObject grades = json.getJSONObject("result").getJSONObject("grades");
                Iterator<?> keys = grades.keys();

                while (keys.hasNext()) {
                    String key = (String)keys.next();
                    if (grades.get(key) instanceof JSONObject ) {
                        JSONObject grade = grades.getJSONObject(key);

                        try {
                            String id = StringEscapeUtils.unescapeJavaScript(grade.getString("id"));
                            String syear = StringEscapeUtils.unescapeJavaScript(grade.getString("syear"));
                            String name = StringEscapeUtils.unescapeJavaScript(grade.getString("course_title"));
                            Object affectsGpaObj = grade.get("affects_gpa");
                            boolean affectsGpa = !JSONObject.NULL.equals(affectsGpaObj)
                                    && ((String) affectsGpaObj).toLowerCase().equals("y")
                                    && !JSONObject.NULL.equals(grade.get("gpa_points"))
                                    && !JSONObject.NULL.equals(grade.get("weighted_gpa_points"));

                            double gpaPoints = -1;
                            double weightedGpaPoints = -1;
                            if (affectsGpa) {
                                try {
                                    gpaPoints = Double.parseDouble(grade.getString("gpa_points"));
                                    gpaPoints = Double.parseDouble(grade.getString("weighted_gpa_points"));
                                } catch (NumberFormatException e) {
                                    Log.w(TAG, "gpa_points/weighted_gpa_points is not a number!");
                                    e.printStackTrace();
                                    affectsGpa = false;
                                }
                            }

                            String[] teacherSplit = grade.getString("teacher").split(", ");
                            String teacher = teacherSplit[0];
                            if (teacherSplit.length > 1) {
                                teacher = teacherSplit[1] + " " + teacherSplit[0];
                            }
                            teacher = StringEscapeUtils.unescapeJavaScript(teacher);

                            String courseId = StringEscapeUtils.unescapeJavaScript(grade.getString("course_period_id"));
                            String courseNum = StringEscapeUtils.unescapeJavaScript(grade.getString("course_num"));

                            String percentGrade = null;
                            Object percentGradeObj = grade.get("percent_grade");
                            if (!JSONObject.NULL.equals(percentGradeObj) && !((String) percentGradeObj).equals("-1")) {
                                percentGrade = StringEscapeUtils.unescapeJavaScript((String) percentGradeObj);
                            }

                            String letterGrade = null;
                            Object letterGradeObj = grade.get("grade_title");
                            if (!JSONObject.NULL.equals(letterGradeObj)) {
                                letterGrade = StringEscapeUtils.unescapeJavaScript((String) letterGradeObj);
                            }

                            double credits = -1;
                            double creditsEarned = -1;
                            Object creditsObj = grade.get("credits");
                            Object creditsEarnedObj = grade.get("credits_earned");
                            if (!JSONObject.NULL.equals(creditsObj) && !JSONObject.NULL.equals(creditsEarnedObj)) {
                                credits = Double.parseDouble((String) creditsObj);
                                creditsEarned = Double.parseDouble((String) creditsEarnedObj);
                            }

                            int gradeLevel = -1;
                            Object gradeLevelObj = grade.get("gradelevel_title");
                            if (!JSONObject.NULL.equals(gradeLevelObj)) {
                                gradeLevel = Integer.parseInt((String) gradeLevelObj);
                            }

                            DateTime lastUpdated = null;
                            Object lastUpdatedObj = grade.get("last_updated_date");
                            if (!JSONObject.NULL.equals(lastUpdatedObj)) {
                                lastUpdated = new DateTime((String) lastUpdatedObj);
                            }

                            String location = StringEscapeUtils.unescapeJavaScript(grade.getString("location_title"));
                            String mpId = StringEscapeUtils.unescapeJavaScript(grade.getString("marking_period_id"));

                            // initialize defaults for these values - it may not always be possible to find the actual values
                            String mpTitle = mpId;
                            String mpSortOrder = "1000";
                            String yearTitle;
                            try {
                                int mpSyear = Integer.parseInt(grade.getString("syear"));
                                yearTitle = String.format("%d-%d", mpSyear, mpSyear + 1);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "syear is not an int!");
                                e.printStackTrace();
                                yearTitle = grade.getString("syear");
                            }

                            // attempt to find actual values using the same method in the original focus page
                            JSONObject markingPeriod = getObjectFromDomainById("marking_period", mpId.replace("E", ""));
                            if (markingPeriod != null) {
                                mpTitle = markingPeriod.getString("title");
                                if (markingPeriod.has("sort_order")) {
                                    mpSortOrder = markingPeriod.getString("sort_order");
                                }
                                String yearId = mpId;
                                JSONObject parentMarkingPeriod = markingPeriod;
                                while (parentMarkingPeriod != null && !JSONObject.NULL.equals(markingPeriod.get("parent_id"))) {
                                    yearId = parentMarkingPeriod.getString("parent_id");
                                    parentMarkingPeriod = getObjectFromDomainById("marking_period", yearId);
                                }

                                JSONObject year = getObjectFromDomainById("year", yearId);
                                if (year != null) {
                                    yearTitle = year.getString("title");
                                }
                            }
                            else {
                                Log.e(TAG, String.format("Marking period id %s not found", mpId));
                            }

                            mpTitle = StringEscapeUtils.unescapeJavaScript(mpTitle);
                            mpSortOrder = StringEscapeUtils.unescapeJavaScript(mpSortOrder);
                            yearTitle = StringEscapeUtils.unescapeJavaScript(yearTitle);

                            String gradeScaleTitle;
                            JSONObject gradeScale = getObjectFromDomainById("grade_scale", grade.getString("grade_scale_id"));
                            if (gradeScale != null) {
                                gradeScaleTitle = gradeScale.getString("title");
                                gradeScaleTitle = StringEscapeUtils.unescapeJavaScript(gradeScaleTitle);
                            }
                            else {
                                Log.e(TAG, String.format("Grade scale id %s not found", grade.getString("grade_scale_id")));
                                gradeScaleTitle = "Default";
                            }

                            String gradSubject = null;
                            if (!JSONObject.NULL.equals(grade.get("grad_subject_id"))) {
                                JSONObject gradSubjectObj = getObjectFromDomainById("grad_subject", grade.getString("grad_subject_id"));
                                if (gradSubjectObj != null) {
                                    gradSubject = gradSubjectObj.getString("title");
                                    gradSubject = StringEscapeUtils.unescapeJavaScript(gradSubject);
                                }
                                else {
                                    Log.e(TAG, String.format("Grad subject id %s not found", grade.getString("grad_subject_id")));
                                }
                            }

                            String comment = null;
                            if (!JSONObject.NULL.equals(grade.get("comment"))) {
                                comment = grade.getString("comment");
                                comment = StringEscapeUtils.unescapeJavaScript(comment);
                            }

                            finalGrades.add(new FinalGrade(id, syear, name, affectsGpa, gpaPoints, weightedGpaPoints, teacher, courseId, courseNum, percentGrade, letterGrade, credits, creditsEarned, gradeLevel, lastUpdated, location, mpId, mpTitle, yearTitle, gradeScaleTitle, gradSubject, comment));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSONException parsing individual final grade");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new FocusParseException("JSONException getting grades element of final grades result", e);
        }

        return new FinalGrades(finalGrades);
    }

    private JSONObject getObjectFromDomainById(String domainName, String id) {
        try {
            if (json.getJSONObject("result").getJSONObject("defaults").has(domainName)
                    && json.getJSONObject("result").getJSONObject("defaults").get(domainName) instanceof JSONObject) {
                JSONObject defaultDomain = json.getJSONObject("result").getJSONObject("defaults").getJSONObject(domainName);
                Iterator<?> domainKeys = defaultDomain.keys();
                while (domainKeys.hasNext()) {
                    String domainKey = (String) domainKeys.next();
                    if (!(defaultDomain.get(domainKey) instanceof JSONObject)) {
                        continue;
                    }

                    JSONObject subdomain = defaultDomain.getJSONObject(domainKey);
                    Iterator<?> subdomainKeys = subdomain.keys();
                    while(subdomainKeys.hasNext()) {
                        String subdomainKey = (String) subdomainKeys.next();
                        if (!(subdomain.get(subdomainKey) instanceof JSONObject)) {
                            continue;
                        }

                        if (subdomainKey.equals(id)) {
                            return subdomain.getJSONObject(subdomainKey);
                        }
                    }

                }
            }
            else {
                // Log.w(TAG, "defaults does not have domain " + domainName);
            }
        } catch (JSONException e) {
            Log.w(TAG, "JSONException while parsing defaults");
            e.printStackTrace();
        }

        try {
            if (json.getJSONObject("result").getJSONObject("domains").has(domainName)
                    && json.getJSONObject("result").getJSONObject("domains").get(domainName) instanceof JSONObject) {
                JSONObject domain = json.getJSONObject("result").getJSONObject("domains").getJSONObject(domainName);
                Iterator<?> domainKeys = domain.keys();
                while(domainKeys.hasNext()) {
                    String domainKey = (String) domainKeys.next();
                    if (!(domain.get(domainKey) instanceof JSONObject)) {
                        continue;
                    }

                    JSONObject subdomain = domain.getJSONObject(domainKey);
                    Iterator<?> subdomainKeys = subdomain.keys();
                    while(subdomainKeys.hasNext()) {
                        String subdomainKey = (String) subdomainKeys.next();
                        if (!(subdomain.get(subdomainKey) instanceof JSONObject)) {
                            continue;
                        }

                        if (subdomainKey.equals(id)) {
                            return subdomain.getJSONObject(subdomainKey);
                        }
                    }

                }
            }
            else {
                Log.w(TAG, "domains does not have domain " + domainName);
            }
        } catch (JSONException e) {
            Log.w(TAG, "JSONException parsing domains");
            e.printStackTrace();
        }

        return null;
    }

}
