package com.slensky.focussis.parser;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.data.Student;
import com.slensky.focussis.util.JSONUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slensky on 3/26/18.
 */

public class StudentParser extends FocusPageParser {
    private static final String TAG = "StudentParser";

    @Override
    public Student parse(String html) throws JSONException {
        // Student constructor parameters
        String id;
        int grade;
        DateTime birthdate = null;
        String pictureUrl;
        Bitmap picture;
        String apiUrl;
        Map<String, Map<String, String>> methods;

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
                    id = context.getString("user_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.w(TAG, "method 1 of getting student ID from context failed");
                    id = context.getJSONObject("obj_info").getString("student_id");
                }
                JSONArray enrollment = context.getJSONObject("obj_info").getJSONArray("enrollment");
                int highestGrade = 0;
                for (int i = 0; i < enrollment.length(); i++) {
                    int cGrade = Integer.parseInt(enrollment.getJSONObject(i).getString("grade_level"));
                    if (cGrade > highestGrade) {
                        highestGrade = cGrade;
                    }
                }
                grade = highestGrade;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(TAG, "method 1 of getting student ID and grade from context failed");
                String[] data = context.getString("print_header").split("<br>")[1].split(" - ");
                id = data[0];
                grade = Integer.parseInt(data[1]);
            }

            if (context.getJSONObject("obj_info").has("birthdate")) {
                String birthdateStr = context.getJSONObject("obj_info").getString("birthdate");
                birthdateStr = birthdateStr.replace(' ', 'T'); // fix format to ISO time
                birthdate = new DateTime(birthdateStr);
            } else {
                Log.i(TAG, "Birthdate not found in context[obj_info] while parsing Student page (may no longer be present for any student...)");
            }
            pictureUrl = context.getJSONObject("obj_info").getString("image");
        } else {
            throw new FocusParseException("Parsing student page failed, could not find context JSON");
        }

        // extract API related information from the page
        Document student = Jsoup.parse(html);
        String apiScript = student.selectFirst(".site-content > script").html();
        Pattern pUrl = Pattern.compile("var url( )*?=( )*?\".*\";");
        m = pUrl.matcher(apiScript);
        if (m.find()) {
            String url = m.group(0);
            apiUrl = url.substring(url.indexOf('"') + 1, url.length() - 2);
        } else {
            throw new FocusParseException("Parsing student page failed, could not find student API url");
        }

        Pattern pMethods = Pattern.compile("var methods( )*?=( )*?\\{.*\\}");
        m = pMethods.matcher(apiScript);
        if (m.find()) {
            String methodsStr = m.group(0);
            methodsStr = methodsStr.substring(methodsStr.indexOf("{"));
            JSONObject controllersJson = new JSONObject(methodsStr);
            methods = new HashMap<>();
            Iterator<?> keys = controllersJson.keys();
            while(keys.hasNext()) {
                String key = (String) keys.next();
                if (controllersJson.get(key) instanceof JSONObject) {
                    Map<String, String> methodsMap = new HashMap<>();
                    JSONObject methodsJson = controllersJson.getJSONObject(key);
                    Iterator<?> keys2 = methodsJson.keys();
                    while(keys2.hasNext()) {
                        String key2 = (String) keys2.next();
                        if (methodsJson.get(key2) instanceof String) {
                            methodsMap.put(key2, methodsJson.getString(key2));
                        }
                    }
                    methods.put(key, methodsMap);
                }
            }
        } else {
            throw new FocusParseException("Parsing student page failed, could not find student API methods");
        }

        Pair<List<MarkingPeriod>, List<Integer>> mp = getMarkingPeriods(html);
        return new Student(mp.first, mp.second, id, grade, birthdate, pictureUrl, apiUrl, methods);
    }

}
