package com.slensky.focussis.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.R;
import com.slensky.focussis.activities.MainActivity;
import com.slensky.focussis.data.Student;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.util.CardViewAnimationController;
import com.slensky.focussis.util.SchoolSingleton;
import com.slensky.focussis.views.IconWithTextView;

import org.apache.commons.lang.math.NumberUtils;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_demographic, container, false);
        return view;
    }

    protected void onSuccess(Demographic demographic) {
        View view = getView();
        if (view != null) {
            IconWithTextView name = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_name);
            name.setText(demographic.getName());
            IconWithTextView dob = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_dob);
            if (demographic.getStudent().getBirthdate() != null) {
                dob.setText(DateUtil.dateTimeToLongString(demographic.getStudent().getBirthdate()));
            }
            else {
                dob.setVisibility(View.GONE);
            }

            // attempt to find email in custom fields
            String emailStr = null;
            for (String title : demographic.getCustomFields().keySet()) {
                if (title.toLowerCase().equals("email") || title.toLowerCase().equals("e-mail")) {
                    emailStr = demographic.getCustomFields().get(title);
                    demographic.getCustomFields().remove(title);
                    break;
                }
            }
            // fall back to known email pattern if email cannot be found
            if (emailStr == null) {
                emailStr = ((MainActivity) getActivity()).getUsername() + SchoolSingleton.getInstance().getSchool().getDomainName();
            }
            IconWithTextView email = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_email);
            email.setText(emailStr);
            final String finalEmailStr = emailStr;
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{finalEmailStr});
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    if (getContext() != null && intent.resolveActivity(getContext().getPackageManager()) != null) {
                        Log.i(TAG, "Emailing " + finalEmailStr);
                        startActivity(intent);
                    }
                }
            });

            // attempt to find the "level" custom field if it's present
            int level = 0;
            for (String title : demographic.getCustomFields().keySet()) {
                if (title.toLowerCase().equals("level (year)") && NumberUtils.isDigits(demographic.getCustomFields().get(title))) {
                    level = Integer.parseInt(demographic.getCustomFields().get(title));
                    demographic.getCustomFields().remove(title);
                    break;
                }
            }

            IconWithTextView grade = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_grade);
            if (level != 0) {
                grade.setText(ordinal(demographic.getStudent().getGrade()) + ", " + ordinal(level) + " year");
            }
            else {
                grade.setText(ordinal(demographic.getStudent().getGrade()));
            }

            IconWithTextView studentID = (IconWithTextView) view.findViewById(com.slensky.focussis.R.id.view_student_id);
            studentID.setText(demographic.getStudent().getId());


            LinearLayout llDetailed = view.findViewById(R.id.ll_detailed);
            llDetailed.removeAllViews();
            for (String title : demographic.getCustomFields().keySet()) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.view_icon_with_text, llDetailed, false);
                ImageView ic = v.findViewById(R.id.row_icon);
                TextView hint = v.findViewById(R.id.text_hint);
                TextView main = v.findViewById(R.id.text_main);
                hint.setText(title);
                main.setText(demographic.getCustomFields().get(title));
                String t = title.toLowerCase();
                int drawableId;
                // custom fields can be anything, but we can try to set a nice icon based on their title
                if (t.contains("locker")) {
                    drawableId = R.drawable.ic_locker_multiple_black_24px;
                } else if (t.contains("bus")) {
                    drawableId = R.drawable.ic_directions_bus_black_24px;
                } else if (t.contains("name")) {
                    drawableId = R.drawable.ic_person_black_24px;
                } else if (t.contains("medical") || t.contains("medicine")) {
                    drawableId = R.drawable.ic_medical_bag_black_24px;
                } else if (t.contains("file") || t.contains("form") || t.contains("document") || t.contains("documentation")) {
                    drawableId = R.drawable.ic_clipboard_text_black_24px;
                } else if (t.contains("birth")) {
                    drawableId = R.drawable.ic_cake_variant_black_24px;
                } else if (t.contains("picture") || t.contains("photo")) {
                    drawableId = R.drawable.ic_camera_black_24px;
                } else if (t.contains("gender") || t.contains("sex")) {
                    drawableId = R.drawable.ic_gender_male_female_black_24px;
                } else if (t.contains("phone") || t.contains("mobile") /* to catch "Student Mobile" field */) {
                    drawableId = R.drawable.ic_phone_black_24px;
                } else if (t.contains("account")) {
                    drawableId = R.drawable.ic_account_card_details_black_24px;
                } else if (t.contains("password")) {
                    drawableId = R.drawable.ic_key_24px;
                } else {
                    drawableId = R.drawable.ic_information_outline_black_24px;
                }
                Drawable d = getResources().getDrawable(drawableId);
                ic.setImageDrawable(d);

                llDetailed.addView(v);
            }

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
        api.getDemographic(new FocusApi.Listener<Demographic>() {
            @Override
            public void onResponse(Demographic response) {
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
