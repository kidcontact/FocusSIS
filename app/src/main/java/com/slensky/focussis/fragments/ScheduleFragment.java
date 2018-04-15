package com.slensky.focussis.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.data.Schedule;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.util.TermUtil;

import org.json.JSONObject;

import com.slensky.focussis.data.ScheduleCourse;

import java.util.List;

/**
 * Created by slensky on 4/27/17.
 */

public class ScheduleFragment extends NetworkTabAwareFragment {
    private static final String TAG = "ScheduleFragment";
    private Schedule schedule;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(com.slensky.focussis.R.string.schedule_label);
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_schedule, container, false);
        return view;
    }

    @Override
    protected void onSuccess(JSONObject response) {
        schedule = new Schedule(response);
        View view = getView();
        if (view != null) {
            LayoutInflater inflater = LayoutInflater.from(view.getContext());

            TextView header = (TextView) view.findViewById(com.slensky.focussis.R.id.text_schedule_header);
            header.setText(schedule.getCurrentMarkingPeriod().getName());

            TableLayout table = (TableLayout) view.findViewById(com.slensky.focussis.R.id.table_courses);
            table.removeAllViews();
            TableRow headerRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_schedule_course_header, table, false);
            table.addView(headerRow);

            List<ScheduleCourse> courses = schedule.getCourses();
            for (ScheduleCourse c : courses) {
                TableRow courseRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_schedule_course, table, false);
                TextView name = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_name);
                name.setText(c.getName());
                TextView period = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_period);
                String periodStr;
                if (c.isAdvisory()) {
                    periodStr = "Advisory";
                }
                else {
                    periodStr = "Period " + Integer.toString(c.getPeriod());
                }
                period.setText(periodStr);
                TextView teacher = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_teacher);
                teacher.setText(c.getTeacher());
                TextView days = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_days);
                days.setText(c.getDays());
                TextView room = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_room);
                room.setText(c.getRoom().split(" ")[0]); // changes jr/sr area into just jr/sr for brevity
                TextView term = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_term);
                term.setText(TermUtil.termToString(c.getTerm()));
                table.addView(courseRow);
            }

        }

        requestFinished = true;
    }

    @Override
    protected void makeRequest() {
        api.getSchedule(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onError(error);
            }
        });
    }

    @Override
    public boolean hasTabs() {
        return false;
    }

    @Override
    public List<String> getTabNames() {
        return null;
    }
}
