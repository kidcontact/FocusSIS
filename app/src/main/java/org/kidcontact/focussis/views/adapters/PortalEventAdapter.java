package org.kidcontact.focussis.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.kidcontact.focussis.R;
import org.kidcontact.focussis.util.DateUtil;
import org.kidcontact.focussis.data.PortalEvent;

import java.util.List;

/**
 * Created by slensky on 3/23/17.
 */

public class PortalEventAdapter extends RecyclerView.Adapter<PortalEventAdapter.ViewHolder> {
    private List<PortalEvent> events;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView eventName;
        public TextView eventDate;

        public ViewHolder (View v) {
            super(v);
            eventName = (TextView) v.findViewById(R.id.text_portal_event_name);
            eventDate = (TextView) v.findViewById(R.id.text_portal_event_date);
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
                .inflate(R.layout.view_portal_event, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PortalEvent event = events.get(position);
        holder.eventName.setText(event.getDescription());
        holder.eventDate.setText(DateUtil.dateTimeToShortString(event.getDate()));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

}
