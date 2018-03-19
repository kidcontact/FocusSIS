package org.kidcontact.focussis.fragments;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.kidcontact.focussis.activities.MainActivity;
import org.kidcontact.focussis.data.Course;
import org.kidcontact.focussis.data.CourseAssignment;
import org.kidcontact.focussis.data.CourseCategory;
import org.kidcontact.focussis.network.ApiBuilder;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.network.FocusApiSingleton;
import org.kidcontact.focussis.network.RequestSingleton;
import org.kidcontact.focussis.util.DateUtil;

import java.util.List;

/**
 * Created by slensky on 4/3/17.
 */

public class CourseFragment extends NetworkFragment {
    private static final String TAG = "CourseFragment";

    private LinearLayout progressLayout;
    private ConstraintLayout courseLayout;
    private Course course;
    private String id;

    public CourseFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = FocusApiSingleton.getApi();//ApiBuilder.getCourseUrl();
        id = getArguments().getString(getString(R.string.EXTRA_COURSE_ID));

        if (course == null) {
            refresh();
        }
    }

    protected void onSuccess(JSONObject response) {
        course = new Course(response);
        redraw(getView());
        requestFinished = true;
    }

    @Override
    protected void onError(VolleyError error) {
        super.onError(error);
        View view = getView();

        progressLayout = (LinearLayout) view.findViewById(R.id.layout_loading);
        progressLayout.setVisibility(View.GONE);

        if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
            courseLayout.setVisibility(View.GONE);
            ((MainActivity) getActivity()).reauthenticate();
        }
        else {
            View networkFailureLayout = view.findViewById(R.id.layout_network_failure);
            networkFailureLayout.setVisibility(View.VISIBLE);
        }
    }

    private void redraw(View view) {
        if (view != null) {
            progressLayout.setVisibility(View.GONE);
            View networkFailureLayout = view.findViewById(R.id.layout_network_failure);
            networkFailureLayout.setVisibility(View.GONE);
            courseLayout.setVisibility(View.VISIBLE);

            TextView courseName = (TextView) view.findViewById(R.id.text_course_name);
            TextView courseTeacher = (TextView) view.findViewById(R.id.text_course_teacher);
            TableLayout courseCategories = (TableLayout) view.findViewById(R.id.tablelayout_course_categories);
            courseCategories.removeAllViews();

            if (course.getPeriod().equals("advisory")) {
                courseName.setText("Advisory - " + course.getName());
            }
            else {
                courseName.setText("P" + course.getPeriod() + " - " + course.getName());
            }
            courseTeacher.setText(course.getTeacher());
            TextView overallGrade = (TextView) view.findViewById(R.id.text_course_grade);
            if (course.getLetterGrade() != null) {
                overallGrade.setText(Html.fromHtml("<b>Overall grade:</b> " + Integer.toString(course.getPercentGrade()) + "% " + course.getLetterGrade()));
            }
            else {
                overallGrade.setText(Html.fromHtml("<b>Overall grade:</b> " + "NG"));
            }


            if (course.hasCategories()) {
                TableRow cnames = new TableRow(getContext());
                TableRow cweights = new TableRow(getContext());
                TableRow cgrades = new TableRow(getContext());

                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                cnames.setLayoutParams(rowParams);
                cnames.setBaselineAligned(false);
                cweights.setLayoutParams(rowParams);
                cgrades.setLayoutParams(rowParams);


                // column 1
                Space spacer = new Space(getContext());
                cnames.addView(spacer);
                TextView percentWeightRowHeader = new TextView(getContext());
                percentWeightRowHeader.setText("Percent of Grade");
                setTextViewParams(percentWeightRowHeader);
                percentWeightRowHeader.setPadding(0, dpToPixels(2), dpToPixels(8), dpToPixels(2));
                percentWeightRowHeader.setTypeface(null, Typeface.BOLD);
                cweights.addView(percentWeightRowHeader);
                TextView percentGradeRowHeader = new TextView(getContext());
                percentGradeRowHeader.setText("Your Score");
                setTextViewParams(percentGradeRowHeader);
                percentGradeRowHeader.setPadding(0, dpToPixels(2), dpToPixels(8), dpToPixels(2));
                percentGradeRowHeader.setTypeface(null, Typeface.BOLD);
                cgrades.addView(percentGradeRowHeader);

                // category columns
                List<CourseCategory> categories = course.getCategories();

                // the longest category string needs to wrap, the rest will match_parent
                float maxLength = 0;
                String longestName = null;
                Paint paint = new Paint();
                for (int i = 0; i < categories.size(); i++) {
                    String name = categories.get(i).getName();
                    float width = paint.measureText(name);
                    if (width > maxLength) {
                        maxLength = width;
                        longestName = name;
                    }
                }
                if (paint.measureText("Weighted Grade") > maxLength) {
                    longestName = "Weighted Grade";
                }

                for (int i = 0; i < categories.size(); i++) {
                    CourseCategory category = categories.get(i);

                    TextView name = new TextView(getContext());
                    name.setText(category.getName());
                    setTextViewParams(name);
                    name.setTypeface(null, Typeface.BOLD);
                    cnames.addView(name);
                    ViewGroup.LayoutParams lp = name.getLayoutParams();

                    if (paint.measureText(name.getText().toString()) * 2.5 > view.getResources().getDisplayMetrics().widthPixels / 3) {
                        lp.width = view.getResources().getDisplayMetrics().widthPixels / 3;
                    }
                    if (name.getText().equals(longestName)) {
                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        name.setGravity(Gravity.RIGHT);
                    }
                    else {
                        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        name.setGravity(Gravity.BOTTOM|Gravity.RIGHT);
                    }
                    name.setLayoutParams(lp);

                    TextView weight = new TextView(getContext());
                    weight.setText(Integer.toString(category.getPercentWeight()) + "%");
                    weight.setGravity(Gravity.RIGHT);
                    setTextViewParams(weight);
                    cweights.addView(weight);

                    TextView score = new TextView(getContext());
                    if (category.isGraded()) {
                        score.setText(Integer.toString(category.getPercentGrade()) + "% " + category.getLetterGrade());
                    }
                    else {
                        score.setText("NG");
                    }
                    score.setGravity(Gravity.RIGHT);
                    setTextViewParams(score);
                    cgrades.addView(score);
                }

                // final column
                TextView overallGradeColHeader = new TextView(getContext());
                overallGradeColHeader.setText("Weighted Grade");
                setTextViewParams(overallGradeColHeader);
                overallGradeColHeader.setGravity(Gravity.BOTTOM|Gravity.RIGHT);
                overallGradeColHeader.setTypeface(null, Typeface.BOLD);
                cnames.addView(overallGradeColHeader);
                ViewGroup.LayoutParams gradeParams = overallGradeColHeader.getLayoutParams();
                if (longestName.equals("Weighted Grade")) {
                    gradeParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                else {
                    gradeParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                overallGradeColHeader.setLayoutParams(gradeParams);
                Space gradeSpacer = new Space(getContext());
                cweights.addView(gradeSpacer);
                TextView weightedGrade = new TextView(getContext());
                if (course.getLetterGrade() != null) {
                    weightedGrade.setText(Integer.toString(course.getPercentGrade()) + "% " + course.getLetterGrade());
                }
                else {
                    weightedGrade.setText("NG");
                }
                weightedGrade.setGravity(Gravity.RIGHT);
                setTextViewParams(weightedGrade);
                cgrades.addView(weightedGrade);

                courseCategories.addView(cnames);
                courseCategories.addView(cweights);
                courseCategories.addView(cgrades);

                ViewGroup.LayoutParams lp = cnames.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                cnames.setLayoutParams(lp);
            }

            LayoutInflater inflater = LayoutInflater.from(getContext());
            TableLayout assignmentTable = (TableLayout) view.findViewById(R.id.table_assignments);
            assignmentTable.removeAllViews();
            List<CourseAssignment> assignments = course.getAssignments();

            TableRow headerRow = (TableRow) inflater.inflate(R.layout.view_course_assignment_header, assignmentTable, false);
            assignmentTable.addView(headerRow);

            if (course.hasAssignments()) {
                for (int i = 0; i < assignments.size(); i++) {
                    TableRow row = (TableRow) inflater.inflate(R.layout.view_course_assignment, assignmentTable, false);
                    final CourseAssignment assignment = assignments.get(i);
                    final TextView name = (TextView) row.findViewById(R.id.text_assignment_name);
                    name.setText(assignment.getShortName(course.getAppropriateAssignmentNameLength()));
                    final TextView points = (TextView) row.findViewById(R.id.text_assignment_points);
                    final TextView grade = (TextView) row.findViewById(R.id.text_assignment_grade);
                    CourseAssignment.Status status = assignment.getStatus();
                    points.setGravity(Gravity.CENTER_HORIZONTAL);
                    switch (status) {
                        case PASS:
                            points.setText("\u2713");
                            grade.setText("Pass");
                            break;
                        case FAIL:
                            points.setText("\u2717");
                            grade.setText("Fail");
                            break;
                        case EXCLUDED:
                            points.setText("* / " + Integer.toString(assignment.getMaxGrade()));
                            grade.setText("Excluded");
                            break;
                        case NOT_GRADED:
                            points.setText("NG / " + Integer.toString(assignment.getMaxGrade()));
                            grade.setText("Not Graded");
                            break;
                        case MISSING:
                            points.setText("M / " + Integer.toString(assignment.getMaxGrade()));
                            grade.setText("Missing");
                            break;
                        case EXTRA_CREDIT:
                            points.setText(Integer.toString((int) assignment.getStudentGrade()) + " / " + Integer.toString(assignment.getMaxGrade()));
                            grade.setText("Extra Credit");
                            break;
                        default:
                            points.setText(Integer.toString((int) assignment.getStudentGrade()) + " / " + Integer.toString(assignment.getMaxGrade()));
                            grade.setText(Integer.toString(assignment.getPercentGrade()) + "% " + assignment.getLetterGrade());
                            break;
                    }

                    final TextView assigned =  (TextView) row.findViewById(R.id.text_assignment_assigned);
                    assigned.setText(DateUtil.dateTimeToShortString(assignment.getAssigned()));

                    final TextView due = (TextView) row.findViewById(R.id.text_assignment_due);
                    due.setText(DateUtil.dateTimeToShortString(assignment.getDue()));

                    if (assignment.hasCategory()) {
                        TextView category = (TextView) row.findViewById(R.id.text_assignment_category);
                        category.setText(assignment.getCategory());
                    }

                    assignmentTable.addView(row);
                    row.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                            alertDialog.setTitle(assignment.getName());
                            String html = "<b>Points: </b>" + points.getText() + "<br>" +
                                    "<b>Grade: </b>" + grade.getText() + "<br>" +
                                    "<b>Assigned: </b>" + assigned.getText() + "<br>" +
                                    "<b>Due: </b>" + due.getText();
                            if (assignment.hasDescription()) {
                                html += "<br><b>Description: </b>" + assignment.getDescription();
                            }
                            alertDialog.setMessage(Html.fromHtml(html));
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    });
                }
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        courseLayout = (ConstraintLayout) view.findViewById(R.id.course_fragment_course_layout);
        progressLayout = (LinearLayout) view.findViewById(R.id.layout_loading);

        if (course == null) {
            courseLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
        }
        else {
            redraw(view);
        }

        return view;
    }

    private void setTextViewParams(TextView textView) {
        textView.setTextColor(getResources().getColor(R.color.textPrimary));
        int padding = dpToPixels(2);
        int padding_sides = dpToPixels(8);
        textView.setPadding(padding_sides, padding, padding_sides, padding);
    }

    private int dpToPixels(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void onClickRetry(View v) {
        View view = getView();
        View networkFailureLayout = view.findViewById(R.id.layout_network_failure);
        networkFailureLayout.setVisibility(View.GONE);
        if (networkError.networkResponse.statusCode == 403) {
            ((MainActivity) getActivity()).reauthenticate();
        }
        else {
            refresh();
        }
    }

    @Override
    public void refresh() {
        if (progressLayout != null && progressLayout.getVisibility() != View.VISIBLE) {
            progressLayout.setVisibility(View.VISIBLE);
        }
        requestFinished = false;
        networkFailed = false;

        api.getCourse(this.id, new Response.Listener<JSONObject>() {
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

}
