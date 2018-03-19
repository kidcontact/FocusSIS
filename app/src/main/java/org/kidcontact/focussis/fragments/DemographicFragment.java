package org.kidcontact.focussis.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.data.Demographic;
import org.kidcontact.focussis.network.ApiBuilder;
import org.kidcontact.focussis.network.FocusApiSingleton;
import org.kidcontact.focussis.util.DateUtil;
import org.kidcontact.focussis.views.IconWithTextView;

import java.util.List;

/**
 * Created by slensky on 5/14/17.
 */

public class DemographicFragment extends NetworkTabAwareFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(R.string.demographic_label);
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_demographic, container, false);
        return view;
    }

    @Override
    protected void onSuccess(JSONObject response) {
        Demographic demographic = new Demographic(response);
        View view = getView();
        if (view != null) {
            IconWithTextView name = (IconWithTextView) view.findViewById(R.id.view_name);
            name.setText(demographic.getName());
            IconWithTextView dob = (IconWithTextView) view.findViewById(R.id.view_dob);
            dob.setText(DateUtil.dateTimeToLongString(demographic.getBirthday()));
            //IconWithTextView email = (IconWithTextView) view.findViewById(R.id.view_email);
            //email.setText(demographic.getEmail());
            IconWithTextView gender = (IconWithTextView) view.findViewById(R.id.view_gender);
            gender.setText(demographic.getGender());
            IconWithTextView grade = (IconWithTextView) view.findViewById(R.id.view_grade);
            grade.setText(ordinal(demographic.getGrade()) + ", " + ordinal(demographic.getLevel()) + " year");

            IconWithTextView bus = (IconWithTextView) view.findViewById(R.id.view_bus);
            if (demographic.getArrivalBus() == demographic.getDismissalBus()) {
                bus.setHint(getString(R.string.demographic_bus_single_hint));
                if (demographic.getArrivalBus() == 0) {
                    bus.setText(getString(R.string.demographic_bus_not_assigned));
                }
                else {
                    bus.setText(Integer.toString(demographic.getArrivalBus()));
                }
            }
            else {
                bus.setHint(getString(R.string.demographic_bus_multiple_hint));
                bus.setText(demographic.getArrivalBus() + "/" + demographic.getDismissalBus());
            }
            bus.setText(demographic.getArrivalBus() + "/" + demographic.getDismissalBus());
            IconWithTextView locker = (IconWithTextView) view.findViewById(R.id.view_locker);
            locker.setText(Integer.toString(demographic.getLocker()));
            IconWithTextView medical = (IconWithTextView) view.findViewById(R.id.view_medical_record_status);
            medical.setText(demographic.getMedicalRecordStatus());
            IconWithTextView photoAuth = (IconWithTextView) view.findViewById(R.id.view_photo_auth);
            photoAuth.setText(boolToYesNo(demographic.isPhotoAuthorized()));
            IconWithTextView cumulative = (IconWithTextView) view.findViewById(R.id.view_cumulative_file);
            cumulative.setText(boolToYesNo(demographic.isCumulativeFile()));
            IconWithTextView studentID = (IconWithTextView) view.findViewById(R.id.view_student_id);
            studentID.setText(Integer.toString(demographic.getId()));
        }

        requestFinished = true;
    }

    @Override
    public void refresh() {
        requestFinished = false;
        networkFailed = false;
        // TODO: implement api call
//        api.getPortal(new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                onSuccess(response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                onError(error);
//            }
//        });
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

    private static String boolToYesNo(boolean b) {
        if (b) {
            return "Yes";
        }
        return "No";
    }

}
