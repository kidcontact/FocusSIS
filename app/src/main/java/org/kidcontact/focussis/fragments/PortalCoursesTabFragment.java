package org.kidcontact.focussis.fragments;

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
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.google.gson.Gson;

import org.kidcontact.focussis.util.GsonSingleton;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.data.Portal;

public class PortalCoursesTabFragment extends Fragment {

    private Portal portal;

    public PortalCoursesTabFragment() {
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
        View view = inflater.inflate(R.layout.fragment_portal_courses_tab, container, false);
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Fragment portalCoursesFragment = new PortalCoursesFragment();
        Bundle args = new Bundle();
        Gson gson = GsonSingleton.getInstance();
        args.putString(getString(R.string.EXTRA_PORTAL), gson.toJson(portal));
        portalCoursesFragment.setArguments(args);

        transaction.addToBackStack(null);
        transaction.replace(R.id.fragment_container, portalCoursesFragment);
        transaction.commit();

        return view;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

}