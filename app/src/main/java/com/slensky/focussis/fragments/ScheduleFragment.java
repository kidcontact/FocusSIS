package com.slensky.focussis.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.slensky.focussis.data.Schedule;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.util.GsonSingleton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by slensky on 4/27/17.
 */

public class ScheduleFragment extends NetworkTabAwareFragment {
    private static final String TAG = "ScheduleFragment";
    private static List<String> tabNames;
    private Schedule schedule;

    static {
        tabNames = Arrays.asList("COURSES", "SCHOOL");
    }

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

    protected void onSuccess(Schedule response) {
        schedule = response;
        if (isAdded()) {
            List<Fragment> tempTabFragments = new ArrayList<>();
            Bundle args = new Bundle();
            Gson gson = GsonSingleton.getInstance();
            args.putString(getString(com.slensky.focussis.R.string.EXTRA_SCHEDULE), gson.toJson(schedule));

            Fragment coursesFragment = new ScheduleCoursesTabFragment();
            coursesFragment.setArguments(args);
            tempTabFragments.add(coursesFragment);

            Fragment schoolFragment = new ScheduleSchoolTabFragment();
            schoolFragment.setArguments(args);
            tempTabFragments.add(schoolFragment);

            tabFragments = tempTabFragments;
            requestFinished = true;
        }
    }

    @Override
    protected void makeRequest() {
        api.getSchedule(new FocusApi.Listener<Schedule>() {
            @Override
            public void onResponse(Schedule response) {
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
        return true;
    }

    @Override
    public List<String> getTabNames() {
        return tabNames;
    }
}
