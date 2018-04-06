package com.slensky.focussis.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slensky.focussis.R;
import com.slensky.focussis.util.DateUtil;
import com.slensky.focussis.data.PortalAssignment;

import java.util.List;

/**
 * Created by slensky on 3/23/17.
 */

public class PortalAssignmentAdapter extends RecyclerView.Adapter<PortalAssignmentAdapter.ViewHolder> {
    private List<PortalAssignment> assignments;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView assignmentName;
        public TextView assignmentDate;

        public ViewHolder (View v) {
            super(v);
            assignmentName = (TextView) v.findViewById(R.id.text_assignment_name);
            assignmentDate = (TextView) v.findViewById(R.id.text_assignment_date);
        }
    }

    public PortalAssignmentAdapter(List<PortalAssignment> assignments) {
        this.assignments = assignments;
    }

    @Override
    public PortalAssignmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_portal_assignment, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PortalAssignment assignment = assignments.get(position);
        holder.assignmentName.setText(assignment.getName());
        holder.assignmentDate.setText(DateUtil.dateTimeToShortString(assignment.getDue()));
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

}
