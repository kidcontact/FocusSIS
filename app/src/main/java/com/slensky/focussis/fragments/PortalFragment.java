package com.slensky.focussis.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.slensky.focussis.R;
import com.slensky.focussis.activities.MainActivity;
import com.slensky.focussis.data.Course;
import com.slensky.focussis.data.CourseAssignment;
import com.slensky.focussis.data.GoogleCalendarEvent;
import com.slensky.focussis.data.PortalAssignment;
import com.slensky.focussis.data.PortalCourse;
import com.slensky.focussis.data.PortalEvent;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.util.GsonSingleton;

import org.json.JSONObject;

import com.slensky.focussis.data.Portal;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.util.Syncable;
import com.slensky.focussis.views.adapters.PortalAssignmentCourseAdapter;
import com.slensky.focussis.views.adapters.PortalEventAdapter;
import com.slensky.focussis.views.adapters.SelectableItemAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by slensky on 4/19/17.
 */

public class PortalFragment extends NetworkTabAwareFragment implements ActionMode.Callback, Syncable {
    private static final String TAG = "PortalFragment";

    private Portal portal;
    private CourseFragment currentCourseFragment;
    private static List<String> tabNames;
    private PortalEventAdapter eventAdapter;
    private PortalAssignmentCourseAdapter assignmentAdapter;

    private ActionMode actionMode;

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

    protected void onSuccess(Portal response) {
        portal = response;

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
    protected void makeRequest() {
        if (selectedTab == 0 && tabFragments != null && tabFragments.get(0).isAdded()) {
            FragmentManager fm = tabFragments.get(0).getChildFragmentManager();
            Fragment fragment = fm.findFragmentById(com.slensky.focussis.R.id.fragment_container);
            if (fragment instanceof CourseFragment) {
                currentCourseFragment = ((CourseFragment) fragment);
                currentCourseFragment.refresh();
                return;
            }
        }
        api.getPortal(new FocusApi.Listener<Portal>() {
            @Override
            public void onResponse(Portal response) {
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
        if (selectedTab == 0 && tabFragments != null && tabFragments.get(0).isAdded()) {
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
        if (selectedTab == 0 && tabFragments != null && tabFragments.get(0).isAdded()) {
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
        eventAdapter = null;
        assignmentAdapter = null;
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
        if (selectedTab == 0 && tabFragments != null && tabFragments.get(0).isAdded()) {
            FragmentManager fm = tabFragments.get(0).getChildFragmentManager();
            Fragment fragment = fm.findFragmentById(com.slensky.focussis.R.id.fragment_container);
            if (fragment instanceof CourseFragment) {
                return true;
            }
        }
        return false;
    }

    public CourseFragment getCourseFragment() {
        if (selectedTab == 0 && tabFragments != null && tabFragments.get(0).isAdded()) {
            FragmentManager fm = tabFragments.get(0).getChildFragmentManager();
            Fragment fragment = fm.findFragmentById(com.slensky.focussis.R.id.fragment_container);
            if (fragment instanceof CourseFragment) {
                return (CourseFragment) fragment;
            }
        }
        return null;
    }

    public void onItemSelected(SelectableItemAdapter adapter, boolean isLongClick, int pos) {
        if (eventAdapter == null && adapter instanceof PortalEventAdapter) {
            eventAdapter = (PortalEventAdapter) adapter;
        }
        if (assignmentAdapter == null && adapter instanceof PortalAssignmentCourseAdapter) {
            assignmentAdapter = (PortalAssignmentCourseAdapter) adapter;
        }

        if (getActivity() != null && actionMode == null && isLongClick) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
        }
        if (actionMode != null) {
            adapter.toggleSelection(pos);
            updateActionModeTitle();
        }
    }

    private void updateActionModeTitle() {
        int selectedEventCount = 0, selectedAssignmentCount = 0;
        if (eventAdapter != null) {
            selectedEventCount = eventAdapter.getSelectedCount();
        }
        if (assignmentAdapter != null) {
            selectedAssignmentCount = assignmentAdapter.getSelectedCount();
        }

        if (selectedEventCount + selectedAssignmentCount > 0) {
            actionMode.setTitle(String.valueOf(selectedEventCount + selectedAssignmentCount) + " selected");
        } else {
            actionMode.finish();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.portal_action, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_export:
                exportSelection();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (eventAdapter != null) {
            eventAdapter.removeSelection();
        }
        if (assignmentAdapter != null) {
            assignmentAdapter.removeSelection();
        }
        actionMode = null;
    }

    public void destroyActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void exportSelection() {
        if (!(getActivity() instanceof MainActivity)) {
            Log.e(TAG, "Selection could not be exported because activity is null or not MainActivity");
            return;
        }

        List<GoogleCalendarEvent> events = new ArrayList<>();
        if (eventAdapter != null) {
            List<PortalEvent> portalEvents = eventAdapter.getEvents();
            SparseBooleanArray selectedItems = eventAdapter.getSelected();
            for (int i = 0; i < selectedItems.size(); i++) {
                int key = selectedItems.keyAt(i);
                if (selectedItems.get(key)) {
                    events.add(portalEvents.get(key));
                }
            }
        }
        if (assignmentAdapter != null) {
            List<PortalCourse> portalCourses = assignmentAdapter.getCourses();
            SparseBooleanArray selectedItems = assignmentAdapter.getSelected();
            for (int i = 0; i < selectedItems.size(); i++) {
                int key = selectedItems.keyAt(i);
                if (selectedItems.get(key)) {
                    for (PortalAssignment assignment : portalCourses.get(key).getAssignments()) {
                        if (!assignment.isDescriptionSet()) {
                            Log.d(TAG, "Assignment does not have set description, running getAssignmentDescriptions");
                            getAssignmentDescriptions(getContext(), portalCourses, new Runnable() {
                                @Override
                                public void run() {
                                    exportSelection();
                                }
                            });
                            return;
                        }
                    }
                    events.addAll(portalCourses.get(key).getAssignments());
                }
            }
        }

        if (events.size() > 0) {
            Log.d(TAG, "Exporting " + events.size() + " events to calendar");
            ((MainActivity) getActivity()).exportEventsToCalendar(events, true, new Runnable() {
                @Override
                public void run() {
                    destroyActionMode();
                }
            });
        }
        else {
            Log.w(TAG, "Not exporting events to calendar because events list is empty");
        }

    }

    public void sync() {
        if (!(getActivity() instanceof MainActivity) || getContext() == null) {
            Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
            return;
        }

        new MaterialDialog.Builder(getContext())
                .title(R.string.sync_to_google_calendar)
                .items(R.array.sync_dialog_options)
                .itemsCallbackMultiChoice(
                        new Integer[]{0, 1},
                        new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                boolean allowSelectionChange =
                                        which.length
                                                >= 1; // selection count must stay above 1, the new (un)selection is included
                                // in the which array
                                if (!allowSelectionChange) {
                                    Toast.makeText(getContext(), R.string.sync_dialog_min_select_error, Toast.LENGTH_SHORT).show();
                                }
                                return allowSelectionChange;
                            }
                        })
                .positiveText(R.string.sync_to_google_calendar_positive)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        List<Integer> selected = Arrays.asList(dialog.getSelectedIndices());
                        performSync(selected.contains(0), selected.contains(1));
                    }
                })
                .negativeText(R.string.cancel)
                .alwaysCallMultiChoiceCallback() // the callback will always be called, to check if
                // (un)selection is still allowed
                .show();

    }

    private void performSync(final boolean exportEvents, final boolean exportAssignments) {
        if (!(getActivity() instanceof MainActivity) || getContext() == null) {
            Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
            return;
        }

        PortalEventsTabFragment eventsTabFragment = getEventsTabFragment();
        PortalAssignmentsTabFragment assignmentsTabFragment = getAssignmentsTabFragment();
        List<GoogleCalendarEvent> events = new ArrayList<>();


        if (exportEvents && eventsTabFragment != null && eventsTabFragment.getAdapter() != null) {
            Log.d(TAG, "Exporting all events");
            events.addAll(eventsTabFragment.getAdapter().getEvents());
        }
        if (exportAssignments && assignmentsTabFragment != null && assignmentsTabFragment.getAdapter() != null) {
            Log.d(TAG, "Exporting all assignments");
            for (PortalCourse portalCourse : assignmentsTabFragment.getAdapter().getCourses()) {
                for (PortalAssignment assignment : portalCourse.getAssignments()) {
                    if (!assignment.isDescriptionSet()) {
                        Log.d(TAG, "Assignment does not have set description, running getAssignmentDescriptions");
                        getAssignmentDescriptions(getContext(), assignmentsTabFragment.getAdapter().getCourses(), new Runnable() {
                            @Override
                            public void run() {
                                performSync(exportEvents, exportAssignments);
                            }
                        });
                        return;
                    }
                }
                events.addAll(portalCourse.getAssignments());
            }
        }

        if (events.size() > 0) {
            Log.d(TAG, "Exporting " + events.size() + " events to calendar");
            ((MainActivity) getActivity()).exportEventsToCalendar(events, true, null);
        }
    }

    private void getAssignmentDescriptions(@NonNull final Context context, Collection<PortalCourse> courses, final Runnable callback) {
        final List<PortalCourse> coursesToDownload = new ArrayList<>();
        for (PortalCourse course : courses) {
            boolean isRealCourse = true;
            try {
                isRealCourse = Integer.parseInt(course.getId()) >= 0;
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (!isRealCourse) {
                // this is a course that has been added by the parser, likely an advisory course
                // no descriptions exist for any of the assignments
                for (PortalAssignment assignment : course.getAssignments()) {
                    assignment.setDescription(null);
                }
                continue;
            }

            for (PortalAssignment assignment : course.getAssignments()) {
                if (!assignment.isDescriptionSet()) {
                    coursesToDownload.add(course);
                    break;
                }
            }
        }

        if (coursesToDownload.size() == 0) {
            callback.run();
            return;
        }

        final List<Request> requests = new ArrayList<>();
        final boolean[] hasError = new boolean[]{false};
        final MaterialDialog progress = new MaterialDialog.Builder(context)
                .content(R.string.portal_get_assignment_descriptions_progress)
                .progress(false, coursesToDownload.size(), true)
                .negativeText(R.string.cancel)
                .canceledOnTouchOutside(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        for (Request request : requests) {
                            Log.i(TAG, "Cancelling request");
                            request.cancel();
                        }
                    }
                })
                .build();
        progress.show();

        for (final PortalCourse portalCourse : coursesToDownload) {
            Request request = api.getCourse(portalCourse.getId(), new FocusApi.Listener<Course>() {
                @Override
                public void onResponse(Course course) {
                    for (PortalAssignment portalAssignment : portalCourse.getAssignments()) {
                        if (!portalAssignment.isCustomAssignment() && course.hasAssignments()) {
                            for (CourseAssignment courseAssignment : course.getAssignments()) {
                                if (courseAssignment.getName().equals(portalAssignment.getName()) && courseAssignment.getDue().equals(portalAssignment.getDue()) && courseAssignment.hasDescription()) {
                                    Log.d(TAG, "Adding description " + courseAssignment.getDescription());
                                    portalAssignment.setDescription(courseAssignment.getDescription());
                                }
                            }
                        }
                        if (!portalAssignment.isDescriptionSet()) {
                            portalAssignment.setDescription(null);
                        }
                    }
                    progress.incrementProgress(1);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hasError[0] = true;
                }
            });
            requests.add(request);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!progress.isCancelled() && progress.getCurrentProgress() != progress.getMaxProgress()) {
                    if (!(getActivity() instanceof MainActivity) || getContext() == null) {
                        Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
                        return;
                    }

                    if (hasError[0]) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                for (Request request : requests) {
                                    request.cancel();
                                }
                                new MaterialDialog.Builder(context)
                                        .content(R.string.export_network_error)
                                        .positiveText(R.string.ok)
                                        .show();
                            }
                        });
                        return;
                    }
                }

                if (!progress.isCancelled()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            callback.run();
                        }
                    });
                }
                Log.d(TAG, "Exiting assignment descriptions thread");

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /* callbacks for when custom assignments are modified on the course page */
    private PortalEventsTabFragment getEventsTabFragment() {
        if (tabFragments != null && tabFragments.get(1).isAdded()) {
            return (PortalEventsTabFragment) tabFragments.get(1);
        }
        return null;
    }

    private PortalAssignmentsTabFragment getAssignmentsTabFragment() {
        if (tabFragments != null && tabFragments.get(2).isAdded()) {
            return (PortalAssignmentsTabFragment) tabFragments.get(2);
        }
        return null;
    }

    public void onAssignmentCreated(String courseId, CourseAssignment assignment) {
        if (getAssignmentsTabFragment() != null) {
            getAssignmentsTabFragment().onAssignmentCreated(courseId, assignment);
        }
    }

    public void onAssignmentDeleted(String courseId, CourseAssignment assignment) {
        if (getAssignmentsTabFragment() != null) {
            getAssignmentsTabFragment().onAssignmentDeleted(courseId, assignment);
        }
    }

    public void onAssignmentModified(String courseId, CourseAssignment from, CourseAssignment to) {
        if (getAssignmentsTabFragment() != null) {
            getAssignmentsTabFragment().onAssignmentModified(courseId, from, to);
        }
    }
}
