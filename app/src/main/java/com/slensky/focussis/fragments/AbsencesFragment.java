package com.slensky.focussis.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.data.Absences;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.views.AbsenceLabelView;

import org.json.JSONObject;

import com.slensky.focussis.data.AbsenceDay;
import com.slensky.focussis.data.AbsencePeriod;
import com.slensky.focussis.util.DateUtil;

import java.util.List;

/**
 * Created by slensky on 5/24/17.
 */

public class AbsencesFragment extends NetworkTabAwareFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(com.slensky.focussis.R.string.absences_label);
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_absences, container, false);
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
        Absences absences = new Absences(response);
        View view = getView();
        if (view != null) {

            TextView summary = (TextView) view.findViewById(com.slensky.focussis.R.id.text_absences_summary);
            String html = "<b>" + getString(com.slensky.focussis.R.string.absences_days_possible) + ": </b>" + absences.getDaysPossible() + "|"
                    + "<br><b>" + getString(com.slensky.focussis.R.string.absences_days_attended) + ": </b>" + absences.getDaysAttended() + "| (" + absences.getDaysAttendedPercent() + "%)"
                    + "<br><b>" + getString(com.slensky.focussis.R.string.absences_days_absent) + ": </b>" + absences.getDaysAbsent() + "| (" + absences.getDaysAbsentPercent() + "%)";
            html = html.replace(".0|", "").replace("|", "");
            summary.setText(Html.fromHtml(html));

            TextView absentHeader = (TextView) view.findViewById(com.slensky.focussis.R.id.text_absent_header);
            absentHeader.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_absent_header, absences.getPeriodsAbsent(), absences.getDaysPartiallyAbsent())));
            TextView absent = (TextView) view.findViewById(com.slensky.focussis.R.id.text_absent);
            absent.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_absent, absences.getPeriodsAbsentUnexcused())));
            TextView absentExcused = (TextView) view.findViewById(com.slensky.focussis.R.id.text_excused_absences);
            absentExcused.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_excused, absences.getPeriodsAbsentExcused(), absences.getDaysAbsentExcused())));
            TextView otherMarksHeader = (TextView) view.findViewById(com.slensky.focussis.R.id.text_other_marks_header);
            otherMarksHeader.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_other_marks, absences.getPeriodsOtherMarks(), absences.getDaysOtherMarks())));
            TextView late = (TextView) view.findViewById(com.slensky.focussis.R.id.text_late);
            late.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_late, absences.getPeriodsLate())));
            TextView tardy = (TextView) view.findViewById(com.slensky.focussis.R.id.text_tardy);
            tardy.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_tardy, absences.getPeriodsTardy())));
            TextView misc = (TextView) view.findViewById(com.slensky.focussis.R.id.text_misc_activity);
            misc.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_misc, absences.getPeriodsMisc())));
            TextView offsite = (TextView) view.findViewById(com.slensky.focussis.R.id.text_offsite);
            offsite.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_offsite, absences.getPeriodsOffsite())));

            LayoutInflater inflater = LayoutInflater.from(getContext());
            TableLayout table = (TableLayout) view.findViewById(com.slensky.focussis.R.id.table_absences);
            table.removeAllViews();
            TableRow headerRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_absences_header, table, false);
            table.addView(headerRow);

            for (final AbsenceDay d : absences.getDays()) {
                if (d.getPeriods().size() == 0) {
                    continue;
                }
                TableRow absenceRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_absences_row, table, false);
                TextView date = (TextView) absenceRow.findViewById(com.slensky.focussis.R.id.text_absence_date);
                date.setText(DateUtil.dateTimeToShortString(d.getDate()));
                TextView daily = (TextView) absenceRow.findViewById(com.slensky.focussis.R.id.text_absence_daily);
                String status = statusToString(d.getStatus());
                daily.setText(status);

                if (d.getStatus() == Absences.Status.ABSENT) {
                    int padding = dpToPixels(4);
                    TableRow.LayoutParams lp = (TableRow.LayoutParams) daily.getLayoutParams();
                    lp.setMargins(lp.leftMargin - padding, lp.topMargin - padding, lp.rightMargin - padding, lp.bottomMargin - padding);
                    absenceRow.removeView(daily);
                    LinearLayout ll = new LinearLayout(getContext());
                    ll.addView(daily);
                    daily.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    daily.setPadding(padding, padding, padding, padding);
                    daily.setTextColor(ContextCompat.getColor(getContext(), com.slensky.focussis.R.color.textPrimaryDark));
                    ll.setBackgroundResource(com.slensky.focussis.R.drawable.absence_label);
                    Drawable background = ll.getBackground();
                    int red = Color.parseColor("#F44336");
                    if (background instanceof ShapeDrawable) {
                        ((ShapeDrawable)background).getPaint().setColor(red);
                    } else if (background instanceof GradientDrawable) {
                        ((GradientDrawable)background).setColor(red);
                    } else if (background instanceof ColorDrawable) {
                        ((ColorDrawable)background).setColor(red);
                    }
                    absenceRow.addView(ll);
                    ll.setLayoutParams(lp);
                }

                absenceRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAbsenceDialog(d);
                    }
                });

                table.addView(absenceRow);
            }

        }
        requestFinished = true;
    }

    @Override
    public void refresh() {
        requestFinished = false;
        networkFailed = false;
        Request request = api.getAbsences(new Response.Listener<JSONObject>() {
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
        // absences takes quite a long time to load for some reason
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void showAbsenceDialog(AbsenceDay d) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        FrameLayout fl = new FrameLayout(getContext());
        TableLayout table = new TableLayout(getContext());
        fl.addView(table);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) table.getLayoutParams();
        lp.setMargins(dpToPixels(16), dpToPixels(16), dpToPixels(16), 0);
        for (AbsencePeriod p : d.getPeriods()) {
            TableRow row = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_absence_dialog_row, table, false);
            TextView period = (TextView) row.findViewById(com.slensky.focussis.R.id.text_period);
            if (p.getPeriod() == 0) {
                period.setText("Advisory: ");
            }
            else {
                period.setText("Period " + p.getPeriod() + ": ");
            }
            AbsenceLabelView label = (AbsenceLabelView) row.findViewById(com.slensky.focussis.R.id.period_absence_label);
            label.setStatus(p.getStatus());
            TextView textLabel = (TextView) row.findViewById(com.slensky.focussis.R.id.text_absence_label);
            String status = statusToString(p.getStatus());
            textLabel.setText(status);
            table.addView(row);
        }
        builder.setView(fl)
                .setTitle(DateUtil.dateTimeToShortString(d.getDate()))
                .setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

//    @Override
//    public void configureRequest(JsonObjectRequest request) {
//        super.configureRequest(request);
//        // for some reason, the absences page on focus takes forever to load. Give some extra time before timing out
//        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//    }

    private int dpToPixels(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private String statusToString(Absences.Status status) {
        switch (status) {
            case UNSET:
                return "Unset";
            case PRESENT:
                return "Present";
            case HALF_DAY:
                return "Half-day";
            case ABSENT:
                return "Absent";
            case EXCUSED:
                return "Excused";
            case LATE:
                return "Late";
            case TARDY:
                return "Tardy";
            case MISC:
                return "Misc.";
            case OFFSITE:
                return "Off Site";
        }
        return null;
    }

}
