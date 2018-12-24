package com.slensky.focussis.parser;

import android.util.Log;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesParser extends FocusPageParser {
    private static final String TAG = "FinalGradesParser";
    private JSONObject json;

    @Override
    public JSONObject parse(String jsonStr) throws JSONException {
        json = new JSONObject(jsonStr);
        JSONObject parsed = new JSONObject();
        JSONObject parsedGrades = new JSONObject();

        if (json.getJSONObject("result").get("grades") instanceof JSONObject) {
            JSONObject grades = json.getJSONObject("result").getJSONObject("grades");
            Iterator<?> keys = grades.keys();

            while(keys.hasNext()) {
                String key = (String)keys.next();
                if (grades.get(key) instanceof JSONObject ) {
                    JSONObject grade = grades.getJSONObject(key);
                    JSONObject parsedGrade = new JSONObject();

                    parsedGrade.put("id", grade.getString("id"));
                    parsedGrade.put("syear", grade.getString("syear"));
                    parsedGrade.put("name", grade.getString("course_title"));
                    Object affectsGpa = grade.get("affects_gpa");
                    parsedGrade.put("affects_gpa",
                            !JSONObject.NULL.equals(affectsGpa)
                                    && ((String) affectsGpa).toLowerCase().equals("y")
                                    && !JSONObject.NULL.equals(grade.get("gpa_points"))
                                    && !JSONObject.NULL.equals(grade.get("weighted_gpa_points")));
                    if (parsedGrade.getBoolean("affects_gpa")) {
                        try {
                            parsedGrade.put("gpa_points", Double.parseDouble(grade.getString("gpa_points")));
                            parsedGrade.put("weighted_gpa_points", Double.parseDouble(grade.getString("weighted_gpa_points")));
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "gpa_points/weighted_gpa_points is not a number!");
                            e.printStackTrace();
                            parsedGrade.put("affects_gpa", false);
                        }

                    }
                    String[] teacher = grade.getString("teacher").split(", ");
                    if (teacher.length > 1) {
                        parsedGrade.put("teacher", teacher[1] + " " + teacher[0]);
                    }
                    else {
                        Log.w(TAG, String.format("Teacher name %s is not comma delimited", teacher[0]));
                        parsedGrade.put("teacher", teacher[0]);
                    }
                    parsedGrade.put("course_id", grade.getString("course_period_id"));
                    parsedGrade.put("course_num", grade.getString("course_num"));
                    Object percentGrade = grade.get("percent_grade");
                    if (!JSONObject.NULL.equals(percentGrade) && !((String) percentGrade).equals("-1")) {
                        parsedGrade.put("percent_grade", percentGrade);
                    }
                    Object letterGrade = grade.get("grade_title");
                    if (!JSONObject.NULL.equals(letterGrade)) {
                        parsedGrade.put("letter_grade", letterGrade);
                    }
                    Object credits = grade.get("credits");
                    Object creditsEarned = grade.get("credits_earned");
                    if (!JSONObject.NULL.equals(credits) && !JSONObject.NULL.equals(creditsEarned)) {
                        parsedGrade.put("credits", Double.parseDouble((String) credits));
                        parsedGrade.put("credits_earned", Double.parseDouble((String) creditsEarned));
                    }
                    Object gradeLevel = grade.get("gradelevel_title");
                    if (!JSONObject.NULL.equals(gradeLevel)) {
                        parsedGrade.put("grade_level", Integer.parseInt((String) gradeLevel));
                    }
                    Object lastUpdated = grade.get("last_updated_date");
                    if (!JSONObject.NULL.equals(lastUpdated)) {
                        parsedGrade.put("last_updated", (String) lastUpdated);
                    }
                    parsedGrade.put("location", grade.getString("location_title"));
                    parsedGrade.put("mp_id", grade.getString("marking_period_id"));

                    JSONObject markingPeriod = getObjectFromDomainById("marking_period", parsedGrade.getString("mp_id").replace("E", ""));
                    if (markingPeriod != null) {
                        parsedGrade.put("mp_title", markingPeriod.getString("title"));
                        //parsedGrade.put("mp_sort_order", markingPeriod.getString("sort_order"));
                        String yearId = parsedGrade.getString("mp_id");
                        JSONObject parentMarkingPeriod = markingPeriod;
                        while (parentMarkingPeriod != null && !JSONObject.NULL.equals(markingPeriod.get("parent_id"))) {
                            yearId = parentMarkingPeriod.getString("parent_id");
                            parentMarkingPeriod = getObjectFromDomainById("marking_period", yearId);
                        }

                        JSONObject year = getObjectFromDomainById("year", yearId);
                        if (year != null) {
                            parsedGrade.put("year_title", year.getString("title"));
                        }
                        else {
                            try {
                                int syear = Integer.parseInt(grade.getString("syear"));
                                parsedGrade.put("year_title", String.format("%d-%d", syear, syear + 1));
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "syear is not an int!");
                                e.printStackTrace();
                                parsedGrade.put("year_title", grade.getString("syear"));
                            }
                        }
                    }
                    else {
                        Log.e(TAG, String.format("Marking period id %s not found", parsedGrade.getString("mp_id")));
                        parsedGrade.put("mp_title", parsedGrade.get("mp_id"));
                        parsedGrade.put("mp_sort_order", "1000");
                        try {
                            int syear = Integer.parseInt(grade.getString("syear"));
                            parsedGrade.put("year_title", String.format("%d-%d", syear, syear + 1));
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "syear is not an int!");
                            e.printStackTrace();
                            parsedGrade.put("year_title", grade.getString("syear"));
                        }
                    }

                    JSONObject gradeScale = getObjectFromDomainById("grade_scale", grade.getString("grade_scale_id"));
                    if (gradeScale != null) {
                        parsedGrade.put("grade_scale_title", gradeScale.getString("title"));
                    }
                    else {
                        Log.e(TAG, String.format("Grade scale id %s not found", grade.getString("grade_scale_id")));
                        parsedGrade.put("grade_scale_title", "Default");
                    }

                    if (!JSONObject.NULL.equals(grade.get("grad_subject_id"))) {
                        JSONObject gradSubject = getObjectFromDomainById("grad_subject", grade.getString("grad_subject_id"));
                        if (gradSubject != null) {
                            parsedGrade.put("grad_subject", gradSubject.getString("title"));
                        }
                        else {
                            Log.e(TAG, String.format("Grad subject id %s not found", grade.getString("grad_subject_id")));
                        }
                    }

                    if (!JSONObject.NULL.equals(grade.get("comment"))) {
                        parsedGrade.put("comment", grade.getString("comment"));
                    }

                    // unescape characters in all fields
                    Iterator<?> gradeKeys = parsedGrade.keys();
                    while (gradeKeys.hasNext()) {
                        String gradeKey = (String) gradeKeys.next();
                        if (parsedGrade.get(gradeKey) instanceof String) {
                            String val = parsedGrade.getString(gradeKey);
                            parsedGrade.put(gradeKey, StringEscapeUtils.unescapeJavaScript(val));
                        }
                    }

                    parsedGrades.put(parsedGrade.getString("id"), parsedGrade);
                }
            }
        }

        parsed.put("grades", parsedGrades);
        return parsed;
    }

    private JSONObject getObjectFromDomainById(String domainName, String id) throws JSONException {
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
