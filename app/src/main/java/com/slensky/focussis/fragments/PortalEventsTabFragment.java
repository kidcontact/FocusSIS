package com.slensky.focussis.fragments;

/**
 * Created by slensky on 4/2/17.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import com.slensky.focussis.activities.MainActivity;
import com.slensky.focussis.util.GsonSingleton;
import com.slensky.focussis.data.Portal;
import com.slensky.focussis.util.RecyclerClickListener;
import com.slensky.focussis.util.RecyclerTouchListener;
import com.slensky.focussis.views.DividerItemDecoration;
import com.slensky.focussis.views.adapters.PortalEventAdapter;
import com.slensky.focussis.R;

public class PortalEventsTabFragment extends Fragment {
    private static final String TAG = "PortalEventsTabFragment";

    private PortalFragment portalFragment;
    private RecyclerView recyclerView;
    private PortalEventAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Portal portal;

    public PortalEventsTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = GsonSingleton.getInstance();
        portal = gson.fromJson(getArguments().getString(getString(R.string.EXTRA_PORTAL)), Portal.class);
        if (getActivity() != null && getActivity() instanceof MainActivity && ((MainActivity) getActivity()).getCurrentFragment() instanceof PortalFragment) {
            portalFragment = (PortalFragment) ((MainActivity) getActivity()).getCurrentFragment();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_portal_empty, container, false);
        if (portal.hasEvents()) {
            view = inflater.inflate(R.layout.fragment_portal_events_tab, container, false);
            recyclerView = (RecyclerView) view.findViewById(R.id.event_recycler_view);
            layoutManager = new LinearLayoutManager(view.getContext());
            recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
            recyclerView.setLayoutManager(layoutManager);
            adapter = new PortalEventAdapter(portal.getEvents());
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

    public PortalEventAdapter getAdapter() {
        return adapter;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

}