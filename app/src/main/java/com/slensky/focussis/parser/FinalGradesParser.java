package com.slensky.focussis.parser;

import android.util.Log;

import com.slensky.focussis.data.FinalGrade;
import com.slensky.focussis.data.FinalGrades;
import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.data.domains.GradSubjectDomain;
import com.slensky.focussis.data.domains.GradeScaleDomain;
import com.slensky.focussis.data.domains.MarkingPeriodDomain;
import com.slensky.focussis.data.domains.SchoolDomain;

import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesParser extends FocusPageParser {
    private static final String TAG = "FinalGradesParser";
    private JSONObject json;

    private final GradeScaleDomain gradeScaleDomain;
    private final GradSubjectDomain gradSubjectDomain;
    private final MarkingPeriodDomain markingPeriodDomain;
    private final SchoolDomain schoolDomain;

    public FinalGradesParser(GradeScaleDomain gradeScaleDomain, GradSubjectDomain gradSubjectDomain, MarkingPeriodDomain markingPeriodDomain, SchoolDomain schoolDomain) {
        this.gradeScaleDomain = gradeScaleDomain;
        this.gradSubjectDomain = gradSubjectDomain;
        this.markingPeriodDomain = markingPeriodDomain;
        this.schoolDomain = schoolDomain;
    }

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
                                lastUpdated = new DateTime(lastUpdatedObj);
                            }

                            String location = StringEscapeUtils.unescapeJavaScript(grade.getString("location_title"));
                            String mpId = grade.getString("marking_period_id");
                            String schoolId = grade.getString("school_id");

                            String mpTitle = mpId;
                            boolean foundTitle = false;
                            for (MarkingPeriodDomain.MarkingPeriodDomainKey k : markingPeriodDomain.getMembers().keySet()) {
                                if (k.getSchoolId().equals(schoolId) && k.getsYear().equals(syear)) {
                                    Map<String, MarkingPeriodDomain.MarkingPeriod> domain = markingPeriodDomain.getMembers().get(k);
                                    // mpId may sometimes only be present without the "E" (exam) prefix. Not sure. TODO
                                    if (domain.containsKey(mpId) || domain.containsKey(mpId.replace("E", ""))) {
                                        if (domain.containsKey(mpId)) {
                                            mpTitle = domain.get(mpId).getTitle();
                                        } else {
                                            mpTitle = domain.get(mpId.replace("E", "")).getTitle();
                                        }

                                        foundTitle = true;
                                        break;
                                    }
                                }
                            }
                            if (!foundTitle) {
                                Log.w(TAG, "Title for marking period " + mpId + " could not be found");
                            }
                            mpTitle = StringEscapeUtils.unescapeJavaScript(mpTitle);

                            String gradeScaleId = grade.getString("grade_scale_id");
                            String gradeScaleTitle = "Default";
                            boolean foundGradeScale = false;
                            for (GradeScaleDomain.GradeScaleDomainKey k : gradeScaleDomain.getMembers().keySet()) {
                                if (k.getSchoolId().equals(schoolId) && k.getsYear().equals(syear)) {
                                    Map<String, GradeScaleDomain.GradeScale> domain = gradeScaleDomain.getMembers().get(k);
                                    if (domain.containsKey(gradeScaleId)) {
                                        gradeScaleTitle = domain.get(gradeScaleId).getTitle();
                                        foundGradeScale = true;
                                        break;
                                    }
                                }
                            }
                            if (!foundGradeScale) {
                                Log.w(TAG, "Title for grade scale " + gradeScaleId + " could not be found");
                            }
                            gradeScaleTitle = StringEscapeUtils.unescapeJavaScript(gradeScaleTitle);


                            String gradSubjectId = !JSONObject.NULL.equals(grade.get("grad_subject_id"))
                                    ? grade.getString("grad_subject_id") : null;
                            String gradSubject = null;
                            if (gradSubjectId != null) {
                                boolean foundGradSubject = false;
                                for (GradSubjectDomain.GradSubjectDomainKey k : gradSubjectDomain.getMembers().keySet()) {
                                    if (k.getsYear().equals(syear)) {
                                        Map<String, GradSubjectDomain.GradSubject> domain = gradSubjectDomain.getMembers().get(k);
                                        if (domain.containsKey(gradSubject)) {
                                            gradeScaleTitle = domain.get(gradSubjectId).getTitle();
                                            foundGradeScale = true;
                                            break;
                                        }
                                    }
                                }
                                if (!foundGradSubject) {
                                    Log.w(TAG, "Title for grade scale " + gradSubjectId + " could not be found");
                                }
                            }

                            String comment = null;
                            if (!JSONObject.NULL.equals(grade.get("comment"))) {
                                comment = grade.getString("comment");
                                comment = StringEscapeUtils.unescapeJavaScript(comment);
                            }

                            finalGrades.add(new FinalGrade(id, syear, name, affectsGpa, gpaPoints, weightedGpaPoints, teacher, courseId, courseNum, percentGrade, letterGrade, credits, creditsEarned, gradeLevel, lastUpdated, location, mpId, mpTitle, gradeScaleTitle, gradSubject, comment));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSONException parsing individual final grade");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new FocusParseException("JSONException getting grades element of final grades result " + jsonStr, e);
        }

        Collections.sort(finalGrades, Collections.<FinalGrade>reverseOrder());
        return new FinalGrades(finalGrades);
    }

}
