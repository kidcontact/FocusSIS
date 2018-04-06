package com.slensky.focussis.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.slensky.focussis.util.TermUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 4/28/17.
 */

public class Schedule extends MarkingPeriodPage {

    private static final String TAG = Schedule.class.getName();
    private final List<ScheduleCourse> courses;

    public Schedule(JSONObject scheduleJSON) {
        super(scheduleJSON);
        List<ScheduleCourse> courses;
        try {
            JSONArray coursesJSON = scheduleJSON.getJSONArray("courses");
            courses = new ArrayList<>();

            for (int i = 0; i < coursesJSON.length(); i ++) {
                JSONObject courseJSON = coursesJSON.getJSONObject(i);
                ScheduleCourse.Term term = TermUtil.stringToTerm(courseJSON.getString("term"));
                int period;
                if (courseJSON.has("period")) {
                    period = courseJSON.getInt("period");
                }
                else {
                    period = 0;
                }
                courses.add(new ScheduleCourse(
                        courseJSON.getString("days"),
                        courseJSON.getString("name"),
                        period,
                        courseJSON.getString("room"),
                        courseJSON.getString("teacher"),
                        term));
            }
        } catch (JSONException e) {
            courses = null;
            Log.e(TAG, "Error parsing schedule courses");
        }

        this.courses = courses;
    }

    public List<ScheduleCourse> getCourses() {
        return courses;
    }

}
