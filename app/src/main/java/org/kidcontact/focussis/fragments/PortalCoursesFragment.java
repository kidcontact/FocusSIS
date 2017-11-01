package org.kidcontact.focussis.fragments;

/**
 * Created by slensky on 4/2/17.
 */
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.google.gson.Gson;

import org.kidcontact.focussis.data.Portal;
import org.kidcontact.focussis.data.PortalCourse;
import org.kidcontact.focussis.util.GsonSingleton;
import org.kidcontact.focussis.util.ItemClickSupport;
import org.kidcontact.focussis.views.DividerItemDecoration;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.views.adapters.PortalCourseAdapter;

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
        portal = gson.fromJson(getArguments().getString(getString(R.string.EXTRA_PORTAL)), Portal.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portal_courses, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.course_recycler_view);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(layoutManager);
        List<PortalCourse> sortedCourses = portal.getCourses();
        Collections.sort(sortedCourses);
        adapter = new PortalCourseAdapter(sortedCourses);
        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                CourseFragment courseFragment = new CourseFragment();
                Bundle args = new Bundle();
                args.putString(getString(R.string.EXTRA_COURSE_ID), ((PortalCourseAdapter.ViewHolder) recyclerView.getChildViewHolder(v)).courseId);
                courseFragment.setArguments(args);

                FragmentTransaction transaction = getParentFragment().getChildFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
                transaction.addToBackStack(null);
                transaction.replace(R.id.fragment_container, courseFragment);
                transaction.commit();
            }
        });

        return view;
    }

}