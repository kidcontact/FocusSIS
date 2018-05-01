package com.slensky.focussis.fragments;

/**
 * Created by slensky on 4/2/17.
 */
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.slensky.focussis.activities.MainActivity;
import com.slensky.focussis.data.CourseAssignment;
import com.slensky.focussis.data.PortalAssignment;
import com.slensky.focussis.util.CourseAssignmentFileHandler;
import com.slensky.focussis.util.GsonSingleton;
import com.slensky.focussis.util.RecyclerClickListener;
import com.slensky.focussis.util.RecyclerTouchListener;
import com.slensky.focussis.views.adapters.PortalAssignmentCourseAdapter;

import com.slensky.focussis.data.Portal;
import com.slensky.focussis.data.PortalCourse;
import com.slensky.focussis.views.DividerItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PortalAssignmentsTabFragment extends Fragment{
    private static final String TAG = "AssignmentsTabFragment";

    private PortalFragment portalFragment;
    private View view;
    private RecyclerView recyclerView;
    private PortalAssignmentCourseAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Portal portal;
    private List<PortalAssignment> replacedAssignments = new ArrayList<>();

    private boolean showCustomAssignments;

    public PortalAssignmentsTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = GsonSingleton.getInstance();
        portal = gson.fromJson(getArguments().getString(getString(com.slensky.focussis.R.string.EXTRA_PORTAL)), Portal.class);
        if (getActivity() != null && getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getCurrentFragment() instanceof PortalFragment) {
            portalFragment = (PortalFragment) ((MainActivity) getActivity()).getCurrentFragment();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(com.slensky.focussis.R.layout.view_portal_empty, container, false);
        showCustomAssignments = PreferenceManager.getDefaultSharedPreferences(view.getContext()).getBoolean("show_custom_assignments_in_portal", true);
        boolean hasAssignments = false;
        for (PortalCourse c : portal.getCourses()) {
            if (c.hasAssignments()) {
                hasAssignments = true;
                break;
            }
        }
        if (hasAssignments) {
            view = inflater.inflate(com.slensky.focussis.R.layout.fragment_portal_assignments_tab, container, false);
            recyclerView = (RecyclerView) view.findViewById(com.slensky.focussis.R.id.assignments_course_recycler_view);
            layoutManager = new LinearLayoutManager(view.getContext());
            recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
            recyclerView.setLayoutManager(layoutManager);
            List<PortalCourse> sortedCourses = portal.getCourses();
            Collections.sort(sortedCourses);

            Map<String, List<CourseAssignment>> savedAssignments = null;
            try {
                savedAssignments = CourseAssignmentFileHandler.getSavedAssignments(getContext());
            } catch (IOException e) {
                Log.e(TAG, "IOException while retrieving saved assignments");
                e.printStackTrace();
            }

            // replace existing assignments with edited versions where applicable
            if (savedAssignments != null) {
                for (PortalCourse course : sortedCourses) {
                    if (!savedAssignments.containsKey(course.getId())) {
                        continue;
                    }
                    for (int i = 0; i < course.getAssignments().size(); i++) {
                        PortalAssignment assignment = course.getAssignments().get(i);
                        for (CourseAssignment savedAssignment : savedAssignments.get(course.getId())) {
                            if (savedAssignment.getName().equals(assignment.getName()) && savedAssignment.getDue().equals(assignment.getDue())) {
                                Log.d(TAG, "Overriding assignment " + assignment.getName());
                                PortalAssignment newAssignment = courseAssignmentToPortalAssignment(course, savedAssignment);
                                replacedAssignments.add(course.getAssignments().remove(i));
                                course.getAssignments().add(i, newAssignment);
                            }
                        }
                    }
                }
            }

            if (savedAssignments != null && showCustomAssignments) {
                for (PortalCourse course : sortedCourses) {
                    if (!savedAssignments.containsKey(course.getId())) {
                        continue;
                    }
                    for (CourseAssignment assignment : savedAssignments.get(course.getId())) {
                        if (shouldShowAssignment(assignment)) {
                            PortalAssignment newAssignment = courseAssignmentToPortalAssignment(course, assignment);
                            course.getAssignments().add(newAssignment);
                        }
                    }
                    Collections.sort(course.getAssignments());
                }
            }

            adapter = new PortalAssignmentCourseAdapter(sortedCourses);
            recyclerView.setAdapter(adapter);

            recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (portalFragment != null) {
                        portalFragment.onItemSelected(adapter, false, position);
                    }
                }

                @Override
                public void onLongClick(View view, int position) {
                    //Select item on long click
                    if (portalFragment != null) {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        portalFragment.onItemSelected(adapter, true, position);
                    }
                }
            }));
        }

        return view;
    }

    private boolean shouldShowAssignment(CourseAssignment assignment) {
        return assignment.getMarkingPeriodId().equals(portal.getCurrentMarkingPeriod().getId())
                && assignment.isCustomAssignment()
                && assignment.getDue().isAfterNow();
    }

    private PortalAssignment courseAssignmentToPortalAssignment(PortalCourse course, CourseAssignment assignment) {
        PortalAssignment newAssignment = new PortalAssignment(assignment.getName(), assignment.getDue(), course.getName(), course.getPeriod(), course.isAdvisory(), course.getTeacher(), course.getTeacherEmail());
        newAssignment.setCustomAssignment(true);
        if (assignment.hasDescription()) {
            newAssignment.setDescription(assignment.getDescription());
        }
        return newAssignment;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public PortalAssignmentCourseAdapter getAdapter() {
        return adapter;
    }

    /* callbacks for when custom assignments are modified on the course page */
    public void onAssignmentCreated(String courseId, CourseAssignment assignment) {
        if (showCustomAssignments && shouldShowAssignment(assignment)) {
            Log.d(TAG, "Updating assignments tab for newly created assignment " + assignment.getName());
            for (PortalCourse course : portal.getCourses()) {
                if (course.getId().equals(courseId)) {
                    course.getAssignments().add(courseAssignmentToPortalAssignment(course, assignment));
                    Collections.sort(course.getAssignments());
                    adapter.setCourses(portal.getCourses());
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public void onAssignmentDeleted(String courseId, CourseAssignment assignment) {
        if (showCustomAssignments && shouldShowAssignment(assignment)) {
            Log.d(TAG, "Updating assignments tab for deleted assignment " + assignment.getName());
            for (PortalCourse course : portal.getCourses()) {
                if (course.getId().equals(courseId)) {
                    PortalAssignment portalAssignment = courseAssignmentToPortalAssignment(course, assignment);
                    for (int i = 0; i < course.getAssignments().size(); i++) {
                        if (course.getAssignments().get(i).equals(portalAssignment)) {
                            course.getAssignments().remove(i);
                            adapter.setCourses(portal.getCourses());
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        }
    }

    public void onAssignmentModified(String courseId, CourseAssignment from, CourseAssignment to) {
        if (showCustomAssignments && from.isCustomAssignment() && to.isCustomAssignment()) {
            Log.i(TAG, "Updating assignments tab for updated assignment " + to.getName());
            for (PortalCourse course : portal.getCourses()) {
                if (course.getId().equals(courseId)) {
                    PortalAssignment portalAssignment = courseAssignmentToPortalAssignment(course, from);
                    for (int i = 0; i < course.getAssignments().size(); i++) {
                        if (course.getAssignments().get(i).equals(portalAssignment)) {
                            course.getAssignments().remove(i);
                            course.getAssignments().add(courseAssignmentToPortalAssignment(course, to));
                            Collections.sort(course.getAssignments());
                            adapter.setCourses(portal.getCourses());
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        }

        // override assignment if necessary
        if (to.isEditedAssignment()) {
            for (PortalCourse course : portal.getCourses()) {
                if (course.getId().equals(courseId)) {
                    for (int i = 0; i < course.getAssignments().size(); i++) {
                        PortalAssignment assignment = course.getAssignments().get(i);
                        if (to.getName().equals(assignment.getName()) && to.getDue().equals(assignment.getDue())) {
                            replacedAssignments.add(course.getAssignments().remove(i));
                            course.getAssignments().add(courseAssignmentToPortalAssignment(course, to));
                            Collections.sort(course.getAssignments());
                            adapter.setCourses(portal.getCourses());
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        }

        // restores original assignment
        if (!to.isCustomAssignment() && !to.isEditedAssignment()) {
            for (PortalCourse course : portal.getCourses()) {
                if (course.getId().equals(courseId)) {
                    for (int i = 0; i < course.getAssignments().size(); i++) {
                        PortalAssignment assignment = course.getAssignments().get(i);
                        if (to.getName().equals(assignment.getName()) && to.getDue().equals(assignment.getDue())) {
                            course.getAssignments().remove(i);
                            for (int j = 0; j < replacedAssignments.size(); j++) {
                                PortalAssignment replacedAssignment = replacedAssignments.get(j);
                                if (to.getName().equals(replacedAssignment.getName()) && to.getDue().equals(replacedAssignment.getDue())) {
                                    course.getAssignments().add(replacedAssignments.remove(j));
                                    Collections.sort(course.getAssignments());
                                    adapter.setCourses(portal.getCourses());
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

}