package com.slensky.focussis.fragments;

/**
 * Created by slensky on 4/2/17.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.slensky.focussis.activities.MainActivity;
import com.slensky.focussis.util.GsonSingleton;
import com.slensky.focussis.util.ItemClickSupport;
import com.slensky.focussis.views.DividerItemDecoration;
import com.slensky.focussis.views.adapters.PortalCourseAdapter;

import com.slensky.focussis.data.Portal;
import com.slensky.focussis.data.PortalCourse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PortalCoursesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Portal portal;

    public PortalCoursesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = GsonSingleton.getInstance();
        portal = gson.fromJson(getArguments().getString(getString(com.slensky.focussis.R.string.EXTRA_PORTAL)), Portal.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_portal_courses, container, false);
        recyclerView = (RecyclerView) view.findViewById(com.slensky.focussis.R.id.course_recycler_view);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(layoutManager);
        List<PortalCourse> sortedCourses = new ArrayList<>(portal.getCourses());
        Collections.sort(sortedCourses);
        // courses with negative period numbers have assignments but are not real courses.
        for (int i = sortedCourses.size() - 1; i >= 0; i--) {
            if (sortedCourses.get(i).getPeriod().startsWith("-")) {
                sortedCourses.remove(i);
            }
        }
        adapter = new PortalCourseAdapter(sortedCourses);
        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (getActivity() != null && getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getCurrentFragment() instanceof PortalFragment) {
                    ((PortalFragment) ((MainActivity) getActivity()).getCurrentFragment()).destroyActionMode();
                }

                CourseFragment courseFragment = new CourseFragment();
                Bundle args = new Bundle();
                args.putString(getString(com.slensky.focussis.R.string.EXTRA_COURSE_ID), ((PortalCourseAdapter.ViewHolder) recyclerView.getChildViewHolder(v)).courseId);
                courseFragment.setArguments(args);

                FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
                transaction.setCustomAnimations(com.slensky.focussis.R.anim.enter_from_right, com.slensky.focussis.R.anim.exit_to_left, com.slensky.focussis.R.anim.enter_from_left, com.slensky.focussis.R.anim.exit_to_right);
                transaction.addToBackStack(null);
                transaction.replace(com.slensky.focussis.R.id.fragment_container, courseFragment);
                transaction.commit();
            }
        });

        return view;
    }

}