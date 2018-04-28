package com.slensky.focussis.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slensky.focussis.data.PortalCourse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 3/23/17.
 */

public class PortalCourseAdapter extends RecyclerView.Adapter<PortalCourseAdapter.ViewHolder> {
    private List<PortalCourse> courses = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView courseName;
        public TextView courseDetails;
        public TextView courseGrade;
        public String courseId;

        public ViewHolder (View v) {
            super(v);
            courseName = (TextView) v.findViewById(com.slensky.focussis.R.id.text_portal_course_name);
            courseDetails = (TextView) v.findViewById(com.slensky.focussis.R.id.text_portal_course_details);
            courseGrade = (TextView) v.findViewById(com.slensky.focussis.R.id.text_portal_course_grade);
        }
    }

    public PortalCourseAdapter(List<PortalCourse> courses) {
        for (PortalCourse c : courses) {
            if (!c.isAdvisory()) {
                this.courses.add(c);
            }
        }
    }

    @Override
    public PortalCourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(com.slensky.focussis.R.layout.view_portal_course, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PortalCourse course = courses.get(position);
        holder.courseName.setText(course.getName());
        if (course.isAdvisory()) {
            holder.courseDetails.setText(course.getTeacher() + " - " + "Advisory");
        }
        else {
            holder.courseDetails.setText(course.getTeacher() + " - " + "Period " + course.getPeriod());
        }

        if (course.isGraded()) {
            holder.courseGrade.setText(Integer.toString(course.getPercentGrade()) + "% " + course.getLetterGrade());
        }
        else {
            holder.courseGrade.setText("NG");
        }
        holder.courseId = course.getId();
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

}
