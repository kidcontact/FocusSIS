package com.slensky.focussis.parser;

import android.util.Log;
import android.util.Pair;

import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.data.Referral;
import com.slensky.focussis.data.Referrals;
import com.slensky.focussis.util.JSONUtil;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 3/23/18.
 */

public class ReferralsParser extends FocusPageParser {
    private static final String TAG = "ReferralsParser";

    @Override
    public Referrals parse(String html) {
        boolean hasReferrals = html.contains("var records = {");

        List<Referral> referrals = new ArrayList<>();
        if (hasReferrals) {
            int start = html.indexOf("var records = {") + "var records = ".length();
            int end = html.substring(start).indexOf(";\n") + start;

            try {
                JSONObject records = new JSONObject(html.substring(start, end));
                Iterator<?> keys = records.keys();
                while(keys.hasNext()) {
                    String key = (String) keys.next();
                    if (records.get(key) instanceof JSONObject) {
                        JSONObject record = records.getJSONObject(key);

                        try {
                            String violation = null;
                            String otherViolation = null;

                            Iterator<?> recordKeys = record.keys();
                            while (recordKeys.hasNext()) {
                                String recordKey = (String) recordKeys.next();
                                if (!recordKey.startsWith("CUSTOM_")) {
                                    continue;
                                }

                                Object violationObj = record.get(recordKey);
                                if (violationObj != null) {
                                    if (violationObj instanceof JSONArray) {
                                        for (int i = 0; i < ((JSONArray) violationObj).length(); i++) {
                                            if (!((JSONArray) violationObj).getString(i).trim().isEmpty()) {
                                                violationObj = ((JSONArray) violationObj).getString(i).trim();
                                                break;
                                            }
                                        }
                                    }
                                    if (!(violationObj instanceof String)) {
                                        continue;
                                    }
                                    if (((String) violationObj).toLowerCase().trim().equals("other") || ((String) violationObj).toLowerCase().trim().length() < 2) {
                                        continue;
                                    }

                                    if (recordKey.endsWith("_1")) {
                                        violation = (String) violationObj;
                                    }
                                    else {
                                        otherViolation = (String) violationObj;
                                    }
                                }
                            }

                            String id = key;
                            DateTime creationDate = new DateTime(record.getString("CREATION_DATE"));
                            boolean display = record.getString("DISPLAY").equals("Y");
                            DateTime entryDate = new DateTime(record.getString("ENTRY_DATE"));
                            DateTime lastUpdated = new DateTime(record.getString("LAST_UPDATED"));
                            boolean notificationSent = record.getInt("NOTIFICATION_SENT") == 1;
                            boolean processed = record.getString("PROCESSED").equals("Y");

                            // unused in ui, don't bother parsing
//                        if (record.getString("SUSPENSION_BEGIN") != null) {
//                            referral.put("suspension_begin", record.getString("SUSPENSION_BEGIN"));
//                            referral.put("suspension_end", record.getString("SUSPENSION_END"));
//                        }

                            int schoolYear = record.getInt("SYEAR");
                            String school = record.getString("_school");

                            String[] studentName = Jsoup.parse(record.getString("_student")).text().trim().split(",");
                            String[] staffName = record.getString("_staff_name").split(",");

                            String teacher;
                            if (staffName.length > 1) {
                                teacher = (staffName[1] + " " + staffName[0]).trim();
                            }
                            else {
                                teacher = staffName[0];
                            }

                            String name;
                            if (studentName.length > 1) {
                                name = (studentName[1] + " " + studentName[0]).trim();
                            }
                            else {
                                name = studentName[0];
                            }
                            int grade = Integer.parseInt(record.getString("_grade"));

                            referrals.add(new Referral(creationDate, entryDate, lastUpdated, display, grade, id, name, notificationSent, processed, school, schoolYear, teacher, violation, otherViolation));
                        } catch (JSONException e) {
                            throw new FocusParseException("JSONException attempting to parseRequirements referral " + record.toString(), e);
                        }
                    }
                }

            } catch (JSONException e) {
                throw new FocusParseException("JSONException attempting to parseRequirements referral records JSON " + html.substring(start, end), e);
            }
        }

        Pair<List<MarkingPeriod>, List<Integer>> mp = getMarkingPeriods(html);
        return new Referrals(mp.first, mp.second, referrals);
    }

}
