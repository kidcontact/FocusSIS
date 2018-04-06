package com.slensky.focussis.fragments;

/**
 * Created by slensky on 4/2/17.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.slensky.focussis.data.Portal;
import com.slensky.focussis.util.GsonSingleton;

public class PortalCoursesTabFragment extends Fragment {

    private Portal portal;

    public PortalCoursesTabFragment() {
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
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_portal_courses_tab, container, false);
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Fragment portalCoursesFragment = new PortalCoursesFragment();
        Bundle args = new Bundle();
        Gson gson = GsonSingleton.getInstance();
        args.putString(getString(com.slensky.focussis.R.string.EXTRA_PORTAL), gson.toJson(portal));
        portalCoursesFragment.setArguments(args);

        transaction.addToBackStack(null);
        transaction.replace(com.slensky.focussis.R.id.fragment_container, portalCoursesFragment);
        transaction.commit();

        return view;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

}