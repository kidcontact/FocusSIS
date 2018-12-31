package com.slensky.focussis.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.R;
import com.slensky.focussis.data.Absences;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.util.TableRowAnimationController;
import com.slensky.focussis.views.AbsenceLabelView;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONObject;

import com.slensky.focussis.data.AbsenceDay;
import com.slensky.focussis.data.AbsencePeriod;
import com.slensky.focussis.util.DateUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by slensky on 5/24/17.
 */

public class AbsencesFragment extends NetworkTabAwareFragment {
    private static final String TAG = "AbsencesFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(com.slensky.focussis.R.string.absences_label);
        refresh();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_absences, container, false);
    }

    @Override
    public boolean hasTabs() {
        return false;
    }

    @Override
    public List<String> getTabNames() {
        return null;
    }

    protected void onSuccess(Absences absences) {
        View view = getView();
        if (view != null) {
            final ScrollView scrollView = view.findViewById(R.id.scrollview_absences);
            TextView summary = view.findViewById(R.id.text_absences_summary);
            String html = "<b>" + getString(com.slensky.focussis.R.string.absences_days_possible) + ": </b>" + absences.getDaysPossible() + "|"
                    + "<br><b>" + getString(com.slensky.focussis.R.string.absences_days_attended) + ": </b>" + absences.getDaysAttended() + "| (" + absences.getDaysAttendedPercent() + "%)"
                    + "<br><b>" + getString(com.slensky.focussis.R.string.absences_days_absent) + ": </b>" + absences.getDaysAbsent() + "| (" + absences.getDaysAbsentPercent() + "%)";
            html = html.replace(".0|", "").replace("|", "");
            summary.setText(Html.fromHtml(html));

            TextView absentHeader = view.findViewById(R.id.text_absent_header);
            absentHeader.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_absent_header, absences.getPeriodsAbsent(), absences.getDaysPartiallyAbsent())));
            TextView absent = view.findViewById(R.id.text_absent);
            absent.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_absent, absences.getPeriodsAbsentUnexcused())));
            TextView absentExcused = view.findViewById(R.id.text_excused_absences);
            absentExcused.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_excused, absences.getPeriodsAbsentExcused(), absences.getDaysAbsentExcused())));
            TextView otherMarksHeader = view.findViewById(R.id.text_other_marks_header);
            otherMarksHeader.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_other_marks, absences.getPeriodsOtherMarks(), absences.getDaysOtherMarks())));
            TextView late = view.findViewById(com.slensky.focussis.R.id.text_late);
            late.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_late, absences.getPeriodsLate())));
            TextView tardy = view.findViewById(com.slensky.focussis.R.id.text_tardy);
            tardy.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_tardy, absences.getPeriodsTardy())));
            TextView misc = view.findViewById(com.slensky.focussis.R.id.text_misc_activity);
            misc.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_misc, absences.getPeriodsMisc())));
            TextView offsite = view.findViewById(com.slensky.focussis.R.id.text_offsite);
            offsite.setText(Html.fromHtml(getString(com.slensky.focussis.R.string.absences_offsite, absences.getPeriodsOffsite())));

            LayoutInflater inflater = LayoutInflater.from(getContext());
            TableLayout table = view.findViewById(com.slensky.focussis.R.id.table_absences);
            table.removeAllViews();
            TableRow headerRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_absences_header, table, false);
            table.addView(headerRow);

            TableRowAnimationController animationController = new TableRowAnimationController(getContext());
            for (final AbsenceDay d : absences.getDays()) {
                if (d.getPeriods().size() == 0) {
                    continue;
                }
                final TableRow absenceRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_absences_row, table, false);
                TextView date = absenceRow.findViewById(R.id.text_absence_date);
                date.setText(DateUtil.dateTimeToShortString(d.getDate()));
                TextView daily = absenceRow.findViewById(R.id.text_absence_daily);
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

                final View divider = inflater.inflate(R.layout.view_divider, table, false);

                final Animation animation = animationController.nextAnimation();
                //absenceRow.setAnimation(animation);
                //divider.setAnimation(animation);

                divider.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect scrollBounds = new Rect();
                        scrollView.getHitRect(scrollBounds);
                        if (divider.getLocalVisibleRect(scrollBounds)) {
                            divider.setAnimation(animation);
                        }
                        divider.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

                absenceRow.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect scrollBounds = new Rect();
                        scrollView.getHitRect(scrollBounds);
                        if (absenceRow.getLocalVisibleRect(scrollBounds)) {
                            absenceRow.setAnimation(animation);
                        }
                        absenceRow.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

                divider.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                absenceRow.setLayerType(View.LAYER_TYPE_HARDWARE, null);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (divider.getLayerType() != View.LAYER_TYPE_NONE) {
                            divider.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                        if (absenceRow.getLayerType() != View.LAYER_TYPE_NONE) {
                            absenceRow.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                table.addView(divider);
                table.addView(absenceRow);
            }

            if (absences.getDays().size() == 0) {
                table.addView(inflater.inflate(R.layout.view_no_records_row, table, false));
            }

        }
        requestFinished = true;
    }

    @Override
    protected void makeRequest() {
        Request request = api.getAbsences(new FocusApi.Listener<Absences>() {
            @Override
            public void onResponse(Absences response) {
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
            period.setText(unabbreviatePeriod(p.getPeriod()));
            AbsenceLabelView label = (AbsenceLabelView) row.findViewById(com.slensky.focussis.R.id.period_absence_label);
            label.setStatus(p.getStatus());
            TextView textLabel = (TextView) row.findViewById(com.slensky.focussis.R.id.text_absence_label);
            String status = statusToString(p.getStatus());
            textLabel.setText(status);

            table.addView(row);
        }
        builder.setView(fl)
                .setTitle(DateUtil.dateTimeToShortString(d.getDate()))
                .setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

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

    private String unabbreviatePeriod(String period) {
        String p = period.toLowerCase(); // for more robust comparisons

        // if the period is an integer, return "Period i"
        if (NumberUtils.isDigits(p)) {
            return "Period " + p;
        }

        // if the period is in the form "P3", return "Period 3"
        if (p.charAt(0) == 'p' && NumberUtils.isDigits(p.substring(1))) {
            return "Period " + p.substring(1);
        }

        // these abbreviations can be found in the absences page of sixth graders
        if (p.equals("hr")) {
            return "Homeroom";
        }
        if (p.equals("sh")) {
            return "Study Hall";
        }

        // possible abbreviations for advisory
        if (p.equals("adv") || p.equals("adv.")) {
            return "Advisory";
        }

        // no abbreviation/unknown pattern, capitalize it just in case it isn't for some reason
        return WordUtils.capitalize(period);
    }

}
