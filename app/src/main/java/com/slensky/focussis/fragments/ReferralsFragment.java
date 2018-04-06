package com.slensky.focussis.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;
import com.slensky.focussis.R;
import com.slensky.focussis.data.Referral;
import com.slensky.focussis.data.Referrals;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.util.DateUtil;

import java.util.Collections;
import java.util.List;

/**
 * Created by slensky on 5/23/17.
 */

public class ReferralsFragment extends NetworkTabAwareFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(R.string.referrals_label);
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_referrals, container, false);
        return view;
    }

    @Override
    public boolean hasTabs() {
        return false;
    }

    @Override
    public List<String> getTabNames() {
        return null;
    }

    @Override
    protected void onSuccess(JSONObject response) {
        Referrals referrals = new Referrals(response);
        List<Referral> refList = referrals.getReferrals();
        Collections.reverse(refList);
        View view = getView();
        if (view != null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            TableLayout table = (TableLayout) view.findViewById(R.id.table_referrals);
            table.removeAllViews();
            TableRow headerRow = (TableRow) inflater.inflate(R.layout.view_referral_header, table, false);
            table.addView(headerRow);

            for (final Referral r : refList) {
                TableRow referralRow = (TableRow) inflater.inflate(R.layout.view_referral, table, false);
                TextView reporter = (TextView) referralRow.findViewById(R.id.text_reporter_name);
                reporter.setText(r.getTeacher().split(" ")[1] + ", " + r.getTeacher().split(" ")[0]);
                TextView entryDate = (TextView) referralRow.findViewById(R.id.text_entry_date);
                entryDate.setText(r.getEntryDate().monthOfYear().get() + "/" + r.getEntryDate().dayOfMonth().getAsText() + '/' +r.getEntryDate().year().getAsShortText().substring(2));
                TextView violation = (TextView) referralRow.findViewById(R.id.text_violation);
                if (r.getViolation() != null) {
                    violation.setText(r.getViolation());
                }
                else {
                    violation.setText(r.getOtherViolation());
                }

                referralRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showReferralDialog(r);
                    }
                });

                table.addView(referralRow);
            }

        }

        requestFinished = true;
    }

    @Override
    public void refresh() {
        requestFinished = false;
        networkFailed = false;
        api.getReferrals(new Response.Listener<JSONObject>() {
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

    public void showReferralDialog(Referral r) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        //alertDialog.setTitle();
        String html = "<b>Reporter: </b>" + r.getTeacher() + "<br>" +
                "<b>Name: </b>" + r.getName() + "<br>" +
                "<b>Grade: </b>" + r.getGrade() + "<br>" +
                "<b>Entry date: </b>" + DateUtil.dateTimeToLongString(r.getEntryDate()) + "<br>" +
                "<b>Last updated: </b>" + DateUtil.dateTimeToLongString(r.getLastUpdated()) + "<br>" +
                "<b>School: </b>" + r.getSchool();

        if (r.getViolation() != null) {
            html += "<br><b>Violation: </b>" + r.getViolation();
        }
        if (r.getOtherViolation() != null) {
            html += "<br><b>Other violation: </b>" + r.getOtherViolation();
        }
        if (r.isProcessed()) {
            html += "<br><b>Processed: </b>Yes";
        }
        else {
            html += "<br><b>Processed: </b>No";
        }

        float dpi = getContext().getResources().getDisplayMetrics().density;
        TextView messageView = new TextView(getContext());
        messageView.setText(Html.fromHtml(html));
        messageView.setTextIsSelectable(true);
        messageView.setTextColor(getResources().getColor(R.color.textPrimary));
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.subheadingText));
        messageView.setPadding(16, 0, 16, 0);

        alertDialog.setView(messageView, (int)(19*dpi), (int)(19*dpi), (int)(14*dpi), (int)(5*dpi) );
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
