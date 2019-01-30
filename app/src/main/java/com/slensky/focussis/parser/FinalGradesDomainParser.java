package com.slensky.focussis.parser;

import android.util.Log;

import com.slensky.focussis.data.domains.GradSubjectDomain;
import com.slensky.focussis.data.domains.GradeScaleDomain;
import com.slensky.focussis.data.domains.MarkingPeriodDomain;
import com.slensky.focussis.data.domains.SchoolDomain;
import com.slensky.focussis.util.GsonSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FinalGradesDomainParser {
    private static final String TAG = "FinalGradesDomainParser";

    private List<GradeScaleDomain.GradeScaleDomainKey> requiredGradeScaleDomains;
    private List<GradSubjectDomain.GradSubjectDomainKey> requiredGradSubjectDomains;
    private List<MarkingPeriodDomain.MarkingPeriodDomainKey> requiredMarkingPeriodDomains;

    private final GradeScaleDomain gradeScaleDomain;
    private final GradSubjectDomain gradSubjectDomain;
    private final MarkingPeriodDomain markingPeriodDomain;

    public FinalGradesDomainParser(GradeScaleDomain gradeScaleDomain, GradSubjectDomain gradSubjectDomain, MarkingPeriodDomain markingPeriodDomain) {
        this.gradeScaleDomain = gradeScaleDomain;
        this.gradSubjectDomain = gradSubjectDomain;
        this.markingPeriodDomain = markingPeriodDomain;
    }

    public void parseRequirements(String jsonStr) {
        requiredGradSubjectDomains = new ArrayList<>();
        requiredGradeScaleDomains = new ArrayList<>();
        requiredMarkingPeriodDomains = new ArrayList<>();
        JSONObject json;
        try {
            json = new JSONObject(jsonStr);
        } catch (JSONException e) {
            throw new FocusParseException("Final grades API response is not valid JSON", e);
        }

        JSONObject result;
        try {
            result = json.getJSONObject("result");
        } catch (JSONException e) {
            throw new FocusParseException("No result in final grade API response", e);
        }

        Log.d(TAG, "Looking for existing domains in final grades response");
        try {
            if (result.has("domains") && result.get("domains") instanceof JSONObject) {
                importDomains(result.getJSONObject("domains"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException looking for existing domains in result['domains']");
        }
        try {
            if (result.has("defaults") && result.get("defaults") instanceof JSONObject) {
                importDomains(result.getJSONObject("defaults"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException looking for existing domains in result['defaults']");
        }

        Log.d(TAG, "Trying to find all required domains for grades in final grades response");
        try {
            if (result.get("grades") instanceof JSONObject) {
                JSONObject grades = result.getJSONObject("grades");
                Iterator<String> keys = grades.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    if (grades.get(key) instanceof JSONObject ) {
                        JSONObject grade = grades.getJSONObject(key);

                        try {
                            String syear = getNullOrString(grade, "syear");
                            String mpId = getNullOrString(grade, "marking_period_id");
                            String schoolId = getNullOrString(grade, "school_id");
                            String gradeScaleId = getNullOrString(grade, "grade_scale_id");

                            boolean needsGradeScaleDomain = true;
                            for (GradeScaleDomain.GradeScaleDomainKey k : gradeScaleDomain.getMembers().keySet()) {
                                if (k.getSchoolId().equals(schoolId) && k.getsYear().equals(syear)) {
                                    needsGradeScaleDomain = false;
                                    break;
                                }
                            }
                            if (needsGradeScaleDomain) {
                                requiredGradeScaleDomains.add(new GradeScaleDomain.GradeScaleDomainKey(schoolId, syear));
                            }

                            boolean needsGradSubjectDomain = true;
                            for (GradSubjectDomain.GradSubjectDomainKey k : gradSubjectDomain.getMembers().keySet()) {
                                if (k.getsYear().equals(syear)) {
                                    needsGradSubjectDomain = false;
                                    break;
                                }
                            }
                            if (needsGradSubjectDomain) {
                                requiredGradSubjectDomains.add(new GradSubjectDomain.GradSubjectDomainKey(syear));
                            }

                            boolean needsMarkingPeriodDomain = true;
                            for (MarkingPeriodDomain.MarkingPeriodDomainKey k : markingPeriodDomain.getMembers().keySet()) {
                                if (k.getSchoolId().equals(schoolId) && k.getsYear().equals(syear)) {
                                    needsMarkingPeriodDomain = false;
                                    break;
                                }
                            }
                            if (needsMarkingPeriodDomain) {
                                requiredMarkingPeriodDomains.add(new MarkingPeriodDomain.MarkingPeriodDomainKey(schoolId, syear));
                            }

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

    }

    public void parseDomainRequest(String jsonStr) {
        JSONObject json;
        try {
            json = new JSONObject(jsonStr);
        } catch (JSONException e) {
            throw new FocusParseException("Final grades domain request response is not valid JSON " + jsonStr, e);
        }

        JSONObject result;
        try {
            result = json.getJSONObject("result");
        } catch (JSONException e) {
            throw new FocusParseException("No result in domain request response " + jsonStr, e);
        }

        try {
            importDomains(result);
        } catch (JSONException e) {
            throw new FocusParseException("Error importing domains from domain request response", e);
        }
    }

    private String getNullOrString(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.has(key) && !JSONObject.NULL.equals(jsonObject.get(key))) {
            return jsonObject.getString(key);
        }
        return null;
    }

    private void importDomains(JSONObject domains) throws JSONException {
        if (domains.has("grade_scale") && domains.get("grade_scale") instanceof JSONObject) {
            JSONObject gradeScale = domains.getJSONObject("grade_scale");
            Iterator<String> keys = gradeScale.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!(gradeScale.get(key) instanceof JSONObject)) {
                    continue;
                }
                String[] decomposedKey = key.split(";");
                if (decomposedKey.length != 2) {
                    throw new JSONException("grade scale key " + key + " does not have right number of parameters");
                }

                JSONObject domain = gradeScale.getJSONObject(key);
                Iterator<String> domainKeys = domain.keys();
                Map<String, GradeScaleDomain.GradeScale> gradeScaleMap = new HashMap<>();
                while (domainKeys.hasNext()) {
                    String domainKey = domainKeys.next();
                    if (!(domain.get(domainKey) instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject member = domain.getJSONObject(domainKey);

                    gradeScaleMap.put(domainKey,
                            new GradeScaleDomain.GradeScale(member.getString("id"),
                                    member.getString("title"),
                                    "Y".equals(member.get("default_scale"))));
                }

                gradeScaleDomain.addMember(new GradeScaleDomain.GradeScaleDomainKey(decomposedKey[0], decomposedKey[1]), gradeScaleMap);
            }
        }

        if (domains.has("grad_subject") && domains.get("grad_subject") instanceof JSONObject) {
            JSONObject gradSubject = domains.getJSONObject("grad_subject");
            Iterator<String> keys = gradSubject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!(gradSubject.get(key) instanceof JSONObject)) {
                    continue;
                }

                JSONObject domain = gradSubject.getJSONObject(key);
                Iterator<String> domainKeys = domain.keys();
                Map<String, GradSubjectDomain.GradSubject> gradSubjectMap = new HashMap<>();
                while (domainKeys.hasNext()) {
                    String domainKey = domainKeys.next();
                    if (!(domain.get(domainKey) instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject member = domain.getJSONObject(domainKey);

                    gradSubjectMap.put(domainKey,
                            new GradSubjectDomain.GradSubject(member.getString("id"), member.getString("short_name"), member.getString("title")));
                }

                gradSubjectDomain.addMember(new GradSubjectDomain.GradSubjectDomainKey(key), gradSubjectMap);
            }
        }

        if (domains.has("marking_period") && domains.get("marking_period") instanceof JSONObject) {
            JSONObject markingPeriod = domains.getJSONObject("marking_period");
            Iterator<String> keys = markingPeriod.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!(markingPeriod.get(key) instanceof JSONObject)) {
                    continue;
                }
                String[] decomposedKey = key.split(";");
                if (decomposedKey.length != 2) {
                    throw new JSONException("marking period key " + key + " does not have right number of parameters");
                }

                JSONObject domain = markingPeriod.getJSONObject(key);
                Iterator<String> domainKeys = domain.keys();
                Map<String, MarkingPeriodDomain.MarkingPeriod> markingPeriodMap = new HashMap<>();
                while (domainKeys.hasNext()) {
                    String domainKey = domainKeys.next();
                    if (!(domain.get(domainKey) instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject member = domain.getJSONObject(domainKey);

                    markingPeriodMap.put(domainKey,
                            new MarkingPeriodDomain.MarkingPeriod(member.getString("id"),
                                    member.getString("title")));
                }

                markingPeriodDomain.addMember(new MarkingPeriodDomain.MarkingPeriodDomainKey(decomposedKey[0], decomposedKey[1]), markingPeriodMap);
            }
        }
    }

    public List<GradeScaleDomain.GradeScaleDomainKey> getRequiredGradeScaleDomains() {
        return requiredGradeScaleDomains;
    }

    public List<GradSubjectDomain.GradSubjectDomainKey> getRequiredGradSubjectDomains() {
        return requiredGradSubjectDomains;
    }

    public List<MarkingPeriodDomain.MarkingPeriodDomainKey> getRequiredMarkingPeriodDomains() {
        return requiredMarkingPeriodDomains;
    }

}
