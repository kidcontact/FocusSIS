package com.slensky.focussis.parser;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.slensky.focussis.util.JSONUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slensky on 3/26/18.
 */

public class StudentParser extends FocusPageParser {
    private static final String TAG = "StudentParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        JSONObject json = new JSONObject();
        // json found in the html of the student page contains useful information
        // extract the student ID and grade by using it
        // we also include fallback mechanisms if the format of this json is changed in the future
        Pattern r = Pattern.compile("context( )*?:( )*?\\{.*\\}");
        Matcher m = r.matcher(html);
        if (m.find()) {
            String contextStr = m.group(0);
            contextStr = contextStr.substring(contextStr.indexOf("{"));
            JSONObject context = new JSONObject(contextStr);
            try {
                try {
                    json.put("id", context.getString("user_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.w(TAG, "method 1 of getting student ID from context failed");
                    json.put("id", context.getJSONObject("obj_info").getString("student_id"));
                }
                JSONArray enrollment = context.getJSONObject("obj_info").getJSONArray("enrollment");
                int highestGrade = 0;
                for (int i = 0; i < enrollment.length(); i++) {
                    int grade = Integer.parseInt(enrollment.getJSONObject(i).getString("grade_level"));
                    if (grade > highestGrade) {
                        highestGrade = grade;
                    }
                }
                json.put("grade", highestGrade);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(TAG, "method 1 of getting student ID and grade from context failed");
                String[] data = context.getString("print_header").split("<br>")[1].split(" - ");
                json.put("id", data[0]);
                json.put("grade", Integer.parseInt(data[1]));
            }

            if (context.getJSONObject("obj_info").has("birthdate")) {
                String birthdate = context.getJSONObject("obj_info").getString("birthdate");
                birthdate = birthdate.replace(' ', 'T'); // fix format to ISO time
                json.put("birthdate", birthdate);
            }
            json.put("picture", context.getJSONObject("obj_info").getString("image"));
        }

        // extract API related information from the page
        Document student = Jsoup.parse(html);
        String apiScript = student.selectFirst(".site-content > script").html();
        Pattern pUrl = Pattern.compile("var url( )*?=( )*?\".*\";");
        m = pUrl.matcher(apiScript);
        if (m.find()) {
            String url = m.group(0);
            url = url.substring(url.indexOf('"') + 1, url.length() - 2);
            json.put("api_url", url);
        }

        Pattern pMethods = Pattern.compile("var methods( )*?=( )*?\\{.*\\}");
        m = pMethods.matcher(apiScript);
        if (m.find()) {
            String methodsStr = m.group(0);
            methodsStr = methodsStr.substring(methodsStr.indexOf("{"));
            JSONObject controllersJson = new JSONObject(methodsStr);
            json.put("methods", controllersJson);
        }

        return JSONUtil.concatJson(json, this.getMarkingPeriods(html));
    }

}
