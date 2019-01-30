package com.slensky.focussis.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.R;
import com.slensky.focussis.data.FinalGrade;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.network.FocusApiSingleton;

import org.json.JSONObject;

import com.slensky.focussis.data.FinalGrades;
import com.slensky.focussis.util.TableRowAnimationController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesFragment extends NetworkTabAwareFragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "FinalGradesFragment";

    private FinalGrades finalGrades;
    private Map<Integer, FocusApi.FinalGradesType> gradeTypeSpinnerPositions = new HashMap<>();
    private FocusApi.FinalGradesType selectedType = FocusApi.FinalGradesType.COURSE_HISTORY;
    private ArrayAdapter<String> spinnerAdapter;
    private View loadingView;
    private TableLayout table;

    private AlertDialog commentCodesDialog;
    private AlertDialog gradeDialog;
    private TextView messageView;

    // used for truncation hack on name column
    List<String> originalNameColumnText = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(com.slensky.focussis.R.string.final_grades_label);
        refresh();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_finalgrades, container, false);
        Spinner spinner = view.findViewById(com.slensky.focussis.R.id.spinner);
        spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        loadingView = view.findViewById(com.slensky.focussis.R.id.layout_loading);
        table = view.findViewById(com.slensky.focussis.R.id.table_finalgrades);

        // Configure comment code dialog link
        final TextView tv = view.findViewById(com.slensky.focussis.R.id.text_comment_codes_link);
        final SpannableString notClickedString = new SpannableString(tv.getText());
        notClickedString.setSpan(new URLSpan(""), 0, notClickedString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
        final SpannableString clickedString = new SpannableString(notClickedString);
        clickedString.setSpan(new ForegroundColorSpan(getResources().getColor(com.slensky.focussis.R.color.colorAccent)), 0, notClickedString.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tv.setText(clickedString);
                        break;
                    case MotionEvent.ACTION_UP:
                        tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
                        v.performClick();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
                        break;
                }
                return true;
            }
        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(clickedString);
                Thread changeColorBack = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
                            }
                        });
                    }
                });
                changeColorBack.start();
                commentCodesDialog.show();
            }
        });

        gradeDialog = new AlertDialog.Builder(getContext())
                .setPositiveButton(com.slensky.focussis.R.string.finalgrades_grade_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        messageView = new TextView(getContext());
        messageView.setTextIsSelectable(true);
        messageView.setTextColor(getResources().getColor(com.slensky.focussis.R.color.textPrimary));
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(com.slensky.focussis.R.dimen.subheadingText));
        messageView.setPadding(16, 0, 16, 0);
        float dpi = getContext().getResources().getDisplayMetrics().density;
        gradeDialog.setView(messageView, (int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi) );

        return view;
    }

    protected void onSuccess(FinalGrades response) {
        finalGrades = response;
        View view = getView();
        if (view != null) {
            final ScrollView scrollView = view.findViewById(R.id.scrollview_finalgrades);
            TextView header = view.findViewById(com.slensky.focussis.R.id.text_finalgrades_type);
            switch (selectedType) {
                case COURSE_HISTORY:
                    header.setText(getString(com.slensky.focussis.R.string.finalgrades_final_grades));
                    break;
                case CURRENT_SEMESTER:
                    header.setText(String.format(getString(com.slensky.focussis.R.string.finalgrades_current_sem_final_grades), finalGrades.getFinalGradesPage().getCurrentSemesterName()));
                    break;
                case CURRENT_SEMESTER_EXAMS:
                    header.setText(String.format(getString(com.slensky.focussis.R.string.finalgrades_current_sem_final_exam_grades), finalGrades.getFinalGradesPage().getCurrentSemesterName()));
                    break;
                case ALL_SEMESTERS:
                    header.setText(getString(com.slensky.focussis.R.string.finalgrades_all_sem_final_grades));
                    break;
                case ALL_SEMESTERS_EXAMS:
                    header.setText(getString(com.slensky.focussis.R.string.finalgrades_all_sem_final_exam_grades));
                    break;
            }

            TextView stats = view.findViewById(com.slensky.focussis.R.id.text_finalgrades_stats);
            String html = getString(com.slensky.focussis.R.string.finalgrades_gpa, finalGrades.getFinalGradesPage().getGpa()) + "<br>" +
                    getString(com.slensky.focussis.R.string.finalgrades_weighted_gpa, finalGrades.getFinalGradesPage().getWeightedGpa()) + "<br>" +
                    getString(com.slensky.focussis.R.string.finalgrades_credits_earned, finalGrades.getFinalGradesPage().getCreditsEarned());
            stats.setText(Html.fromHtml(html));

            commentCodesDialog = new AlertDialog.Builder(getContext())
                    .setTitle(com.slensky.focussis.R.string.finalgrades_comment_codes_dialog_title)
                    .setMessage(finalGrades.getFinalGradesPage().getCommentCodes())
                    .setPositiveButton(com.slensky.focussis.R.string.finalgrades_comment_codes_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();

            if (spinnerAdapter.getCount() == 0) {
                int i = 0;
                for (FocusApi.FinalGradesType t : FocusApi.FinalGradesType.values()) {
                    // not all final grades pages have current semester exams (6th graders/new students don't?)
                    if (!t.equals(FocusApi.FinalGradesType.CURRENT_SEMESTER_EXAMS) || finalGrades.getFinalGradesPage().hasCurrentSemesterExams()) {
                        spinnerAdapter.add(finalGradesTypeToString(t));
                        gradeTypeSpinnerPositions.put(i, t);
                        i++;
                    }
                }
                spinnerAdapter.notifyDataSetChanged();
            }

            LayoutInflater inflater = LayoutInflater.from(getContext());

            table.removeAllViews();
            TableRow headerRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_finalgrades_header, table, false);
            if (!(selectedType == FocusApi.FinalGradesType.ALL_SEMESTERS || selectedType == FocusApi.FinalGradesType.ALL_SEMESTERS_EXAMS)) {
                headerRow.findViewById(com.slensky.focussis.R.id.text_finalgrade_mp).setVisibility(View.GONE);
            }

            final TextView nameHeader = headerRow.findViewById(com.slensky.focussis.R.id.text_finalgrade_name);
            table.addView(headerRow);

            table.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = nameHeader.getWidth();
                    int height = nameHeader.getHeight();
                    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                    originalNameColumnText.clear();
                    for (int i = 2; i < table.getChildCount(); i++) {
                        TextView name = table.getChildAt(i).findViewById(com.slensky.focussis.R.id.text_finalgrade_name);
                        if (name == null) { // divider row
                            continue;
                        }
                        name.measure(widthMeasureSpec, heightMeasureSpec);
                        String originalText = name.getText().toString();
                        originalNameColumnText.add(originalText);
                        while (name.getMeasuredWidth() > width) {
                            originalText = originalText.substring(0, originalText.length() - 1);
                            name.setText(originalText + "...");
                            name.measure(widthMeasureSpec, heightMeasureSpec);
                        }
                    }
                    table.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });

            TableRowAnimationController animationController = new TableRowAnimationController(getContext());
            for (final FinalGrade fg : finalGrades.getFinalGrades()) {
                if (!(fg.hasPercentGrade() || fg.hasLetterGrade())) {
                    continue;
                }

                final TableRow gradeRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_finalgrades_row, table, false);

                TextView year = gradeRow.findViewById(com.slensky.focussis.R.id.text_finalgrade_year);
                year.setText(getYearTitle(fg.getSyear()));

                TextView mp = gradeRow.findViewById(com.slensky.focussis.R.id.text_finalgrade_mp);
                mp.setText(mpTitleToAcronym(fg.getMpTitle()));
                if (!(selectedType == FocusApi.FinalGradesType.ALL_SEMESTERS || selectedType == FocusApi.FinalGradesType.ALL_SEMESTERS_EXAMS)) {
                    mp.setVisibility(View.GONE);
                }

                TextView name = gradeRow.findViewById(com.slensky.focussis.R.id.text_finalgrade_name);
                name.setText(fg.getName());

                TextView grade = gradeRow.findViewById(com.slensky.focussis.R.id.text_finalgrade_grade);
                grade.setText(formatLetterAndPercentGrade(fg));

                gradeRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialogForGrade(fg);
                    }
                });

                final View divider = inflater.inflate(R.layout.view_divider, table, false);

                final Animation animation = animationController.nextAnimation();
                //gradeRow.setAnimation(animation);
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

                gradeRow.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect scrollBounds = new Rect();
                        scrollView.getHitRect(scrollBounds);
                        if (gradeRow.getLocalVisibleRect(scrollBounds)) {
                            gradeRow.setAnimation(animation);
                        }
                        gradeRow.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

                divider.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                gradeRow.setLayerType(View.LAYER_TYPE_HARDWARE, null);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (divider.getLayerType() != View.LAYER_TYPE_NONE) {
                            divider.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                        if (gradeRow.getLayerType() != View.LAYER_TYPE_NONE) {
                            gradeRow.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                table.addView(divider);
                table.addView(gradeRow);
            }

            if (finalGrades.getFinalGrades().size() == 0) {
                table.addView(inflater.inflate(R.layout.view_no_records_row, table, false));
            }

            loadingView.setVisibility(View.GONE);
            table.setVisibility(View.VISIBLE);
        }

        requestFinished = true;
    }

    private void showDialogForGrade(FinalGrade fg) {
        gradeDialog.setTitle(fg.getName());
        String html = "<b>Year: </b>" + getYearTitle(fg.getSyear()) + "<br>" +
                "<b>Marking Period: </b>" + fg.getMpTitle() + "<br>" +
                "<b>Type: </b>" + finalGradesTypeToStringForDialog(selectedType) + "<br>" +
                "<b>Course Number: </b>" + fg.getCourseNum() + "<br>" +
                "<b>Grade: </b>" + formatLetterAndPercentGrade(fg) + "<br>" +
                "<b>Grade Scale: </b>" + fg.getGradeScaleTitle() + "<br>" +
                "<b>Affects GPA: </b>" + boolToYesNo(fg.affectsGpa()) + "<br>";

        if (fg.affectsGpa()) {
            html += "<b>GPA Points: </b>" + fg.getGpaPoints() + "<br>" +
                    "<b>Weighted GPA: </b>" + fg.getWeightedGpaPoints() + "<br>";
        }

        if (fg.hasCredits()) {
            html += "<b>Credits attempted: </b>" + fg.getCredits() + "<br>" +
                    "<b>Credits earned: </b>" + fg.getCreditsEarned() + "<br>";
        }

        html += "<b>Teacher: </b>" + fg.getTeacher() + "<br>" +
                "<b>School: </b>" + fg.getLocation();

        if (fg.hasGradSubject()) {
            html += "<br><b>Grad Subject: </b>" + fg.getGradSubject();
        }

        if (fg.hasComment()) {
            html += "<br><b>Comments: </b>" + fg.getComment();
        }

        messageView.setText(Html.fromHtml(html));
        gradeDialog.show();
    }

    private String getYearTitle(String syearStr) {
        String yearTitle = syearStr;
        try {
            int syear = Integer.parseInt(syearStr);
            yearTitle = String.format("%d-%d", syear, syear + 1);
        } catch (NumberFormatException e) {
            Log.w(TAG, "syear" + syearStr + " is not int");
        }
        return yearTitle;
    }

    private String finalGradesTypeToStringForDialog(FocusApi.FinalGradesType type) {
        switch (type) {
            case COURSE_HISTORY:
                return "Final grade";
            case CURRENT_SEMESTER:
                return "Semester grade";
            case CURRENT_SEMESTER_EXAMS:
                return "Term exam";
            case ALL_SEMESTERS:
                return "Semester grade";
            case ALL_SEMESTERS_EXAMS:
                return "Term exam";
        }
        return null;
    }

    private String formatLetterAndPercentGrade(FinalGrade fg) {
        StringBuilder gradeBuilder = new StringBuilder();
        if (fg.hasPercentGrade()) {
            gradeBuilder.append(fg.getPercentGrade());
            gradeBuilder.append("%");
            if (fg.hasLetterGrade()) {
                gradeBuilder.append(" ");
            }
        }
        if (fg.hasLetterGrade()) {
            gradeBuilder.append(fg.getLetterGrade());
        }
        return gradeBuilder.toString();
    }

    private String boolToYesNo(boolean b) {
        if (b) {
            return getString(com.slensky.focussis.R.string.yes);
        }
        return getString(com.slensky.focussis.R.string.no);
    }

    private String finalGradesTypeToString(FocusApi.FinalGradesType type) {
        switch (type) {
            case COURSE_HISTORY:
                return getString(com.slensky.focussis.R.string.finalgrades_course_history);
            case CURRENT_SEMESTER:
                return finalGrades.getFinalGradesPage().getCurrentSemesterName();
            case CURRENT_SEMESTER_EXAMS:
                return finalGrades.getFinalGradesPage().getCurrentSemesterExamsName();
            case ALL_SEMESTERS:
                return getString(com.slensky.focussis.R.string.finalgrades_all_semesters);
            case ALL_SEMESTERS_EXAMS:
                return getString(com.slensky.focussis.R.string.finalgrades_all_semesters_exams);
        }
        return null;
    }

    private String mpTitleToAcronym(String mpTitle) {
        // return numeric marking period titles as-is
        boolean onlyNumeric = true;
        for (int i = 0; i < mpTitle.length(); i++) {
            if (!Character.isDigit(mpTitle.charAt(i))) {
                onlyNumeric = false;
                break;
            }
        }
        if (onlyNumeric) {
            return mpTitle;
        }

        String[] mp = mpTitle.split(" ");
        // 1 word marking period title
        if (mp.length == 1) {
            // handle marking periods like "Sem1Gradebook"
            for (int i = 1; i < mpTitle.length(); i++) {
                if (Character.isDigit(mpTitle.charAt(i))) {
                    return Character.toString(mpTitle.charAt(0)).toUpperCase() + mpTitle.charAt(i);
                }
            }
            // otherwise the format is unknown, just return
            return mpTitle;
        }
        else {
            // return marking periods like "S2 Progress" as "S2"
            if (mp[0].length() == 2 && !Character.isDigit(mp[0].charAt(0)) && Character.isDigit(mp[0].charAt(1))) {
                return mp[0];
            }

            // otherwise make an acronym normally using first letter of each word
            StringBuilder acronym = new StringBuilder();
            for (String word : mp) {
                acronym.append(Character.toUpperCase(word.charAt(0)));
            }
            return acronym.toString();
        }
    }

    @Override
    protected void makeRequest() {
        api.getFinalGrades(selectedType, new FocusApi.Listener<FinalGrades>() {
            @Override
            public void onResponse(FinalGrades response) {
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        FocusApi.FinalGradesType oldSelection = selectedType;
        selectedType = gradeTypeSpinnerPositions.get(i);
        if (selectedType != oldSelection) { // only reload page if the selection has changed
            table.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            refresh();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (table != null) {
            final TextView nameHeader = table.getChildAt(0).findViewById(com.slensky.focussis.R.id.text_finalgrade_name);
            table.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = nameHeader.getWidth();
                    int height = nameHeader.getHeight();
                    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                    for (int i = 2; i < table.getChildCount(); i++) {
                        TextView name = table.getChildAt(i).findViewById(com.slensky.focussis.R.id.text_finalgrade_name);
                        if (name == null) { // divider row
                            continue;
                        }
                        String originalText = originalNameColumnText.get((i - 1) / 2); // divison to account for divider rows
                        name.setText(originalText);
                        name.measure(widthMeasureSpec, heightMeasureSpec);
                        while (name.getMeasuredWidth() > width) {
                            originalText = originalText.substring(0, originalText.length() - 1);
                            name.setText(originalText + "...");
                            name.measure(widthMeasureSpec, heightMeasureSpec);
                        }
                    }
                    table.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

    }
}
