package com.slensky.focussis.views.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slensky.focussis.util.DateUtil;
import com.slensky.focussis.data.PortalEvent;

import java.util.List;

/**
 * Created by slensky on 3/23/17.
 */

public class PortalEventAdapter extends SelectableItemAdapter<PortalEventAdapter.ViewHolder> {
    private List<PortalEvent> events;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView eventName;
        public TextView eventDate;

        public ViewHolder (View v) {
            super(v);
            eventName = (TextView) v.findViewById(com.slensky.focussis.R.id.text_portal_event_name);
            eventDate = (TextView) v.findViewById(com.slensky.focussis.R.id.text_portal_event_date);
        }
    }

    public PortalEventAdapter(List<PortalEvent> events) {
        this.events = events;
    }

    @Override
    public PortalEventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(com.slensky.focussis.R.layout.view_portal_event, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        PortalEvent event = events.get(position);
        holder.eventName.setText(event.getDescription());
        holder.eventDate.setText(DateUtil.dateTimeToShortString(event.getDate()));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public List<PortalEvent> getEvents() {
        return events;
    }

}
