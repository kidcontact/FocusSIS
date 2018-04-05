package org.kidcontact.focussis.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.data.FinalGrade;
import org.kidcontact.focussis.data.FinalGrades;
import org.kidcontact.focussis.network.FocusApi;
import org.kidcontact.focussis.network.FocusApiSingleton;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesFragment extends NetworkTabAwareFragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "FinalGradesFragment";

    private FinalGrades finalGrades;
    private FocusApi.FinalGradesType selectedType = FocusApi.FinalGradesType.COURSE_HISTORY;
    private ArrayAdapter<String> spinnerAdapter;
    private View loadingView;
    private TableLayout table;

    private AlertDialog commentCodesDialog;
    private AlertDialog gradeDialog;

    // used for truncation hack on name column
    List<String> originalNameColumnText = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();
        title = getString(R.string.final_grades_label);
        refresh();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_finalgrades, container, false);
        Spinner spinner = view.findViewById(R.id.spinner);
        spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        loadingView = view.findViewById(R.id.layout_loading);
        table = view.findViewById(R.id.table_finalgrades);

        // Configure comment code dialog link
        final TextView tv = view.findViewById(R.id.text_comment_codes_link);
        final SpannableString notClickedString = new SpannableString(tv.getText());
        notClickedString.setSpan(new URLSpan(""), 0, notClickedString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
        final SpannableString clickedString = new SpannableString(notClickedString);
        clickedString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, notClickedString.length(),
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
                .setPositiveButton(R.string.finalgrades_grade_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();

        return view;
    }

    @Override
    protected void onSuccess(JSONObject response) {
        finalGrades = new FinalGrades(response);
        View view = getView();
        if (view != null) {
            TextView header = view.findViewById(R.id.text_finalgrades_type);
            switch (selectedType) {
                case COURSE_HISTORY:
                    header.setText(getString(R.string.finalgrades_final_grades));
                    break;
                case CURRENT_SEMESTER:
                    header.setText(String.format(getString(R.string.finalgrades_current_sem_final_grades), finalGrades.getCurrentSemesterName()));
                    break;
                case CURRENT_SEMESTER_EXAMS:
                    header.setText(String.format(getString(R.string.finalgrades_current_sem_final_exam_grades), finalGrades.getCurrentSemesterName()));
                    break;
                case ALL_SEMESTERS:
                    header.setText(getString(R.string.finalgrades_all_sem_final_grades));
                    break;
                case ALL_SEMESTERS_EXAMS:
                    header.setText(getString(R.string.finalgrades_all_sem_final_exam_grades));
                    break;
            }

            TextView stats = view.findViewById(R.id.text_finalgrades_stats);
            String html = getString(R.string.finalgrades_gpa, finalGrades.getGpa()) + "<br>" +
                    getString(R.string.finalgrades_weighted_gpa, finalGrades.getWeightedGpa()) + "<br>" +
                    getString(R.string.finalgrades_credits_earned, finalGrades.getTotalCreditsEarned());
            stats.setText(Html.fromHtml(html));

            commentCodesDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.finalgrades_comment_codes_dialog_title)
                    .setMessage(finalGrades.getCommentCodes())
                    .setPositiveButton(R.string.finalgrades_comment_codes_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();

            if (spinnerAdapter.getCount() == 0) {
                spinnerAdapter.add(finalGradesTypeToString(FocusApi.FinalGradesType.COURSE_HISTORY));
                spinnerAdapter.add(finalGradesTypeToString(FocusApi.FinalGradesType.CURRENT_SEMESTER));
                spinnerAdapter.add(finalGradesTypeToString(FocusApi.FinalGradesType.CURRENT_SEMESTER_EXAMS));
                spinnerAdapter.add(finalGradesTypeToString(FocusApi.FinalGradesType.ALL_SEMESTERS));
                spinnerAdapter.add(finalGradesTypeToString(FocusApi.FinalGradesType.ALL_SEMESTERS_EXAMS));
                spinnerAdapter.notifyDataSetChanged();
            }

            LayoutInflater inflater = LayoutInflater.from(getContext());

            table.removeAllViews();
            TableRow headerRow = (TableRow) inflater.inflate(R.layout.view_finalgrades_header, table, false);
            if (!(selectedType == FocusApi.FinalGradesType.ALL_SEMESTERS || selectedType == FocusApi.FinalGradesType.ALL_SEMESTERS_EXAMS)) {
                headerRow.findViewById(R.id.text_finalgrade_mp).setVisibility(View.GONE);
            }

            final TextView nameHeader = headerRow.findViewById(R.id.text_finalgrade_name);
            table.addView(headerRow);

            table.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = nameHeader.getWidth();
                    int height = nameHeader.getHeight();
                    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                    originalNameColumnText.clear();
                    for (int i = 1; i < table.getChildCount(); i++) {
                        TextView name = table.getChildAt(i).findViewById(R.id.text_finalgrade_name);
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

            for (final FinalGrade fg : finalGrades.getFinalGrades()) {
                if (!(fg.hasPercentGrade() || fg.hasLetterGrade())) {
                    continue;
                }

                TableRow gradeRow = (TableRow) inflater.inflate(R.layout.view_finalgrades_row, table, false);

                TextView year = gradeRow.findViewById(R.id.text_finalgrade_year);
                year.setText(fg.getYearTitle());

                TextView mp = gradeRow.findViewById(R.id.text_finalgrade_mp);
                mp.setText(mpTitleToAcronym(fg.getMpTitle()));
                if (!(selectedType == FocusApi.FinalGradesType.ALL_SEMESTERS || selectedType == FocusApi.FinalGradesType.ALL_SEMESTERS_EXAMS)) {
                    mp.setVisibility(View.GONE);
                }

                TextView name = gradeRow.findViewById(R.id.text_finalgrade_name);
                name.setText(fg.getName());

                TextView grade = gradeRow.findViewById(R.id.text_finalgrade_grade);
                grade.setText(formatLetterAndPercentGrade(fg));

                gradeRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialogForGrade(fg);
                    }
                });
                table.addView(gradeRow);
            }

            loadingView.setVisibility(View.GONE);
            table.setVisibility(View.VISIBLE);
        }

        requestFinished = true;
    }

    private void showDialogForGrade(FinalGrade fg) {
        gradeDialog.setTitle(fg.getName());
        String html = "<b>Year: </b>" + fg.getYearTitle() + "<br>" +
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

        float dpi = getContext().getResources().getDisplayMetrics().density;
        TextView messageView = new TextView(getContext());
        messageView.setText(Html.fromHtml(html));
        messageView.setTextIsSelectable(true);
        messageView.setTextColor(getResources().getColor(R.color.textPrimary));
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.subheadingText));
        messageView.setPadding(16, 0, 16, 0);

        gradeDialog.setView(messageView, (int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi) );
        gradeDialog.show();
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
            return getString(R.string.yes);
        }
        return getString(R.string.no);
    }

    private String finalGradesTypeToString(FocusApi.FinalGradesType type) {
        switch (type) {
            case COURSE_HISTORY:
                return getString(R.string.finalgrades_course_history);
            case CURRENT_SEMESTER:
                return finalGrades.getCurrentSemesterName();
            case CURRENT_SEMESTER_EXAMS:
                return finalGrades.getCurrentSemesterExamsName();
            case ALL_SEMESTERS:
                return getString(R.string.finalgrades_all_semesters);
            case ALL_SEMESTERS_EXAMS:
                return getString(R.string.finalgrades_all_semesters_exams);
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
    public void refresh() {
        requestFinished = false;
        networkFailed = false;
        api.getFinalGrades(selectedType, new Response.Listener<JSONObject>() {
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        FocusApi.FinalGradesType oldSelection = selectedType;
        switch (i) {
            case 0:
                selectedType = FocusApi.FinalGradesType.COURSE_HISTORY;
                break;
            case 1:
                selectedType = FocusApi.FinalGradesType.CURRENT_SEMESTER;
                break;
            case 2:
                selectedType = FocusApi.FinalGradesType.CURRENT_SEMESTER_EXAMS;
                break;
            case 3:
                selectedType = FocusApi.FinalGradesType.ALL_SEMESTERS;
                break;
            case 4:
                selectedType = FocusApi.FinalGradesType.ALL_SEMESTERS_EXAMS;
                break;
        }
        if (selectedType != oldSelection) {
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

        final TextView nameHeader = table.getChildAt(0).findViewById(R.id.text_finalgrade_name);
        table.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = nameHeader.getWidth();
                int height = nameHeader.getHeight();
                int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                for (int i = 1; i < table.getChildCount(); i++) {
                    TextView name = table.getChildAt(i).findViewById(R.id.text_finalgrade_name);
                    String originalText = originalNameColumnText.get(i - 1);
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
