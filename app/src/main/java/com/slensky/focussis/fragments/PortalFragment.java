package com.slensky.focussis.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.slensky.focussis.util.GsonSingleton;

import org.json.JSONObject;

import com.slensky.focussis.data.Portal;
import com.slensky.focussis.network.FocusApiSingleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by slensky on 4/19/17.
 */

public class PortalFragment extends NetworkTabAwareFragment {

    private Portal portal;
    private CourseFragment currentCourseFragment;
    private static List<String> tabNames;

    static {
        tabNames = Arrays.asList("COURSES", "EVENTS", "ASSIGNMENTS");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(com.slensky.focussis.R.string.portal_label);
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_portal, container, false);
        return view;
    }

    @Override
    protected void onSuccess(JSONObject response) {
        portal = new Portal(response);

        if (isAdded()) {
            List<Fragment> tempTabFragments = new ArrayList<>();
            Bundle args = new Bundle();
            Gson gson = GsonSingleton.getInstance();
            args.putString(getString(com.slensky.focussis.R.string.EXTRA_PORTAL), gson.toJson(portal));

            Fragment courseFragment = new PortalCoursesTabFragment();
            courseFragment.setArguments(args);
            tempTabFragments.add(courseFragment);

            Fragment eventFragment = new PortalEventsTabFragment();
            eventFragment.setArguments(args);
            tempTabFragments.add(eventFragment);

            Fragment assignmentFragment = new PortalAssignmentsTabFragment();
            assignmentFragment.setArguments(args);
            tempTabFragments.add(assignmentFragment);

            tabFragments = tempTabFragments;
            requestFinished = true;
        }
    }

    @Override
    public void refresh() {
        if (tabFragments != null && tabFragments.get(0).isAdded()) {
            FragmentManager fm = tabFragments.get(0).getChildFragmentManager();
            Fragment fragment = fm.findFragmentById(com.slensky.focussis.R.id.fragment_container);
            if (fragment instanceof CourseFragment) {
                currentCourseFragment = ((CourseFragment) fragment);
                currentCourseFragment.refresh();
                return;
            }
        }
        requestFinished = false;
        networkFailed = false;
        api.getPortal(new Response.Listener<JSONObject>() {
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
    public boolean hasNetworkError() {
        if (tabFragments != null && tabFragments.get(0).isAdded()) {
            FragmentManager fm = tabFragments.get(0).getChildFragmentManager();
            Fragment fragment = fm.findFragmentById(com.slensky.focussis.R.id.fragment_container);
            if (fragment instanceof CourseFragment) {
                return ((CourseFragment) fragment).hasNetworkError();
            }
        }
        return super.hasNetworkError();
    }

    @Override
    public boolean isRequestFinished() {
        if (tabFragments != null && tabFragments.get(0).isAdded()) {
            FragmentManager fm = tabFragments.get(0).getChildFragmentManager();
            Fragment fragment = fm.findFragmentById(com.slensky.focussis.R.id.fragment_container);
            if (fragment instanceof CourseFragment) {
                return ((CourseFragment) fragment).isRequestFinished();
            }
        }
        return super.isRequestFinished();
    }

    @Override
    public void onFragmentLoad() {

    }

    @Override
    public boolean hasTabs() {
        return true;
    }

    @Override
    public List<String> getTabNames() {
        return tabNames;
    }

    @Override
    public boolean isCurrentFragmentNested() {
        if (tabFragments != null && tabFragments.get(0).isAdded()) {
            FragmentManager fm = tabFragments.get(0).getChildFragmentManager();
            Fragment fragment = fm.findFragmentById(com.slensky.focussis.R.id.fragment_container);
            if (fragment instanceof CourseFragment) {
                return true;
            }
        }
        return false;
    }
}
