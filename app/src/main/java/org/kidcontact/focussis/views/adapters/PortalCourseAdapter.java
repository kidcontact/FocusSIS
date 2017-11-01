package org.kidcontact.focussis.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.data.PortalCourse;

import java.util.List;

/**
 * Created by slensky on 3/23/17.
 */

public class PortalCourseAdapter extends RecyclerView.Adapter<PortalCourseAdapter.ViewHolder> {
    private List<PortalCourse> courses;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView courseName;
        public TextView courseDetails;
        public TextView courseGrade;
        public String courseId;

        public ViewHolder (View v) {
            super(v);
            courseName = (TextView) v.findViewById(R.id.text_portal_course_name);
            courseDetails = (TextView) v.findViewById(R.id.text_portal_course_details);
            courseGrade = (TextView) v.findViewById(R.id.text_portal_course_grade);
        }
    }

    public PortalCourseAdapter(List<PortalCourse> courses) {
        this.courses = courses;
    }

    @Override
    public PortalCourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_portal_course, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PortalCourse course = courses.get(position);
        holder.courseName.setText(course.getName());
        holder.courseDetails.setText(course.getName() + " - " + "Period " + Integer.toString(course.getPeriod()));
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
