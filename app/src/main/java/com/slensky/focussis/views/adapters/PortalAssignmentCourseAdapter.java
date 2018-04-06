package com.slensky.focussis.views.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slensky.focussis.R;
import com.slensky.focussis.data.PortalCourse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 3/23/17.
 */

public class PortalAssignmentCourseAdapter extends RecyclerView.Adapter<PortalAssignmentCourseAdapter.ViewHolder> {
    private List<PortalCourse> courses = new ArrayList<PortalCourse>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView courseView;
        public RecyclerView recyclerView;
        public RecyclerView.Adapter adapter;
        public RecyclerView.LayoutManager layoutManager;

        public ViewHolder (View v) {
            super(v);
            courseView = (TextView) v.findViewById(R.id.text_assignment_course);
            recyclerView = (RecyclerView) v.findViewById(R.id.assignment_recycler_view);
            layoutManager = new LinearLayoutManager(v.getContext());
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    public PortalAssignmentCourseAdapter(List<PortalCourse> courses) {
        for (PortalCourse c : courses) {
            if (c.hasAssignments()) {
                this.courses.add(c);
            }
        }
    }

    @Override
    public PortalAssignmentCourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_portal_assignment_course, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PortalCourse course = courses.get(position);
        holder.courseView.setText(course.getName());
        holder.adapter = new PortalAssignmentAdapter(course.getAssignments());
        holder.recyclerView.setAdapter(holder.adapter);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

}
