package com.slensky.focussis.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.R;
import com.slensky.focussis.util.CardViewAnimationController;
import com.slensky.focussis.views.IconWithTextView;

import org.json.JSONObject;

import com.slensky.focussis.data.Demographic;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.util.DateUtil;

import java.util.List;

/**
 * Created by slensky on 5/14/17.
 */

public class DemographicFragment extends NetworkTabAwareFragment {
    private static final String TAG = "DemographicFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(com.slensky.focussis.R.string.demographic_label);
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_demographic, container, false);
        return view;
    }

    @Override
    protected void onSuccess(JSONObject response) {
        final Demographic demographic = new Demographic(response);
        demographic.setPicture(api.getStudent().getPicture());
        View view = getView();
        if (view != null) {
            IconWithTextView name = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_name);
            name.setText(demographic.getName());
            IconWithTextView dob = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_dob);
            dob.setText(DateUtil.dateTimeToLongString(demographic.getBirthdate()));
            IconWithTextView email = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_email);
            email.setText(demographic.getEmail());
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{demographic.getEmail()});
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    //startActivity(intent);
                    if (getContext() != null && intent.resolveActivity(getContext().getPackageManager()) != null) {
                        Log.i(TAG, "Emailing " + demographic.getEmail());
                        startActivity(intent);
                    }
                }
            });
            IconWithTextView gender = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_gender);
            gender.setText(demographic.getGender());
            IconWithTextView grade = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_grade);
            grade.setText(ordinal(demographic.getGrade()) + ", " + ordinal(demographic.getLevel()) + " year");

            IconWithTextView bus = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_bus);
            if (demographic.getArrivalBus() == null || demographic.getArrivalBus().equals(demographic.getDismissalBus())) {
                bus.setHint(getString(com.slensky.focussis.R.string.demographic_bus_single_hint));
                if (demographic.getArrivalBus() == null) {
                    bus.setText(getString(com.slensky.focussis.R.string.demographic_unassigned));
                }
                else {
                    bus.setText(demographic.getArrivalBus());
                }
            }
            else {
                bus.setHint(getString(com.slensky.focussis.R.string.demographic_bus_multiple_hint));
                bus.setText(demographic.getArrivalBus() + "/" + demographic.getDismissalBus());
            }
            bus.setText(demographic.getArrivalBus() + "/" + demographic.getDismissalBus());
            IconWithTextView locker = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_locker);
            if (demographic.getLocker() != null) {
                locker.setText(demographic.getLocker());
            }
            else {
                locker.setText(getString(com.slensky.focussis.R.string.demographic_unassigned));
            }
            IconWithTextView medical = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_medical_record_status);
            medical.setText(demographic.getMedicalRecordStatus());
            IconWithTextView photoAuth = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_photo_auth);
            photoAuth.setText(boolToYesNo(demographic.isPhotoAuthorized()));
            IconWithTextView cumulative = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_cumulative_file);
            cumulative.setText(demographic.getCumulativeFile());
            IconWithTextView studentID = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_student_id);
            studentID.setText(Integer.toString(demographic.getId()));

            CardViewAnimationController animationController = new CardViewAnimationController(getContext());
            CardView basic = view.findViewById(R.id.card_basic);
            CardView detailed = view.findViewById(R.id.card_detailed);
            basic.setAnimation(animationController.nextAnimation());
            detailed.setAnimation(animationController.nextAnimation());

        }

        requestFinished = true;
    }

    @Override
    protected void makeRequest() {
        api.getDemographic(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onError(error);
            }
        });
    }

    @Override
    public boolean hasTabs() {
        return false;
    }

    @Override
    public List<String> getTabNames() {
        return null;
    }

    // for formatting the student's grade/level
    private static String ordinal(int i) {
        String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }

    private String boolToYesNo(boolean b) {
        if (b) {
            return getString(com.slensky.focussis.R.string.yes);
        }
        return getString(com.slensky.focussis.R.string.no);
    }

}
