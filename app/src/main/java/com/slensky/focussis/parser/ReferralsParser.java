package com.slensky.focussis.parser;

import android.util.Log;

import com.slensky.focussis.util.JSONUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.Iterator;

/**
 * Created by slensky on 3/23/18.
 */

public class ReferralsParser extends FocusPageParser {
    private static final String TAG = "ReferralsParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject referrals = new JSONObject();
        json.put("referrals", referrals);

        boolean hasReferrals = html.contains("var records = {");
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
                        JSONObject referral = new JSONObject();

                        Iterator<?> recordKeys = record.keys();
                        while (recordKeys.hasNext()) {
                            String recordKey = (String) recordKeys.next();
                            if (!recordKey.startsWith("CUSTOM_")) {
                                continue;
                            }

                            Object violation = record.get(recordKey);
                            if (violation != null) {
                                if (violation instanceof JSONArray) {
                                    for (int i = 0; i < ((JSONArray) violation).length(); i++) {
                                        if (!((JSONArray) violation).getString(i).trim().isEmpty()) {
                                            violation = ((JSONArray) violation).getString(i).trim();
                                            break;
                                        }
                                    }
                                }
                                if (!(violation instanceof String)) {
                                    continue;
                                }
                                if (((String) violation).toLowerCase().trim().equals("other") || ((String) violation).toLowerCase().trim().length() < 2) {
                                    continue;
                                }

                                if (recordKey.endsWith("_1")) {
                                    referral.put("violation", violation);
                                }
                                else {
                                    referral.put("other_violation", violation);
                                }
                            }
                        }

                        referral.put("id", key);
                        referral.put("creation_date", record.getString("CREATION_DATE"));
                        referral.put("display", record.getString("DISPLAY").equals("Y"));
                        referral.put("entry_date", record.getString("ENTRY_DATE"));
                        referral.put("last_updated", record.getString("LAST_UPDATED"));
                        referral.put("notification_sent", record.getInt("NOTIFICATION_SENT") == 1);
                        referral.put("processed", record.getString("PROCESSED").equals("Y"));

                        if (record.getString("SUSPENSION_BEGIN") != null) {
                            referral.put("suspension_begin", record.getString("SUSPENSION_BEGIN"));
                            referral.put("suspension_end", record.getString("SUSPENSION_END"));
                        }

                        referral.put("school_year", record.getInt("SYEAR"));
                        referral.put("school", record.getString("_school"));

                        String[] studentName = Jsoup.parse(record.getString("_student")).text().trim().split(",");
                        String[] staffName = record.getString("_staff_name").split(",");

                        if (staffName.length > 1) {
                            referral.put("teacher", (staffName[1] + " " + staffName[0]).trim());
                        }
                        else {
                            referral.put("teacher", staffName[0]);
                        }

                        if (studentName.length > 1) {
                            referral.put("name", (studentName[1] + " " + studentName[0]).trim());
                        }
                        else {
                            referral.put("name", studentName[0]);
                        }
                        referral.put("grade", Integer.parseInt(record.getString("_grade")));

                        referrals.put(key, referral);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(TAG, "JSONException attempting to parse referral records");
            }
        }

        return JSONUtil.concatJson(json, this.getMarkingPeriods(html));
    }

}
