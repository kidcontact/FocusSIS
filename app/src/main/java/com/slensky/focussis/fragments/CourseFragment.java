package com.slensky.focussis.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.data.CourseAssignment;

import org.joda.time.DateTime;
import org.json.JSONObject;
import com.slensky.focussis.activities.MainActivity;
import com.slensky.focussis.data.Course;
import com.slensky.focussis.data.CourseCategory;
import com.slensky.focussis.R;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.util.CourseAssignmentFileHandler;
import com.slensky.focussis.util.DateUtil;
import com.slensky.focussis.views.HorizontalScrollViewWithListener;
import com.slensky.focussis.views.ScrollAwareFABBehavior;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by slensky on 4/3/17.
 */

public class CourseFragment extends NetworkFragment {
    private static final String TAG = "CourseFragment";

    private LinearLayout progressLayout;
    private ConstraintLayout courseLayout;
    private NestedScrollView scrollView;
    private FloatingActionButton fab;
    TableLayout assignmentTable;
    private Course course;
    private String id;

    private List<CourseAssignment> replacedAssignments;
    private boolean editingAssignment = false; // used to prevent assignment details from overlaying edit assignment alert
    private CourseAssignment contextMenuAssignment;

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

        if (getView() != null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editAssignment(null, false);
                }
            });
        }

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
            fab.setVisibility(View.GONE);
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

            int initialAnimationStartOffset = 100;
            Animation fabAnimation = new TranslateAnimation(0, 0, 175, 0);
            fabAnimation.setDuration(350);
            fabAnimation.setInterpolator(new DecelerateInterpolator());
            fabAnimation.setStartOffset(initialAnimationStartOffset);
            fab.setAnimation(fabAnimation);
            fab.show();
            fab.setVisibility(View.VISIBLE);

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
            assignmentTable = (TableLayout) view.findViewById(R.id.table_assignments);
            assignmentTable.removeAllViews();
            List<CourseAssignment> assignments = course.getAssignments();

            TableRow headerRow = (TableRow) inflater.inflate(R.layout.view_course_assignment_header, assignmentTable, false);
            assignmentTable.addView(headerRow);

            replacedAssignments = new ArrayList<>();
            try {
                List<CourseAssignment> savedAssignments = CourseAssignmentFileHandler.getSavedAssignments(getContext(), course.getId());
                for (int i = savedAssignments.size() - 1; i >= 0; i--) {
                    if (!savedAssignments.get(i).getMarkingPeriodId().equals(course.getCurrentMarkingPeriod().getId())) {
                        savedAssignments.remove(i);
                    }
                    if (savedAssignments.get(i).isEditedAssignment()) {
                        Log.d(TAG, "Edited assignment found: " + savedAssignments.get(i).getName());
                        for (int j = 0; j < course.getAssignments().size(); j++) {
                            if (savedAssignments.get(i).overrides(course.getAssignments().get(j))) {
                                replacedAssignments.add(course.getAssignments().remove(j));
                                break;
                            }
                        }
                    }
                }
                course.getAssignments().addAll(savedAssignments);
            } catch (IOException e) {
                Log.e(TAG, "IOException while retrieving saved assignments");
                e.printStackTrace();
            }
            Collections.sort(course.getAssignments(), Collections.<CourseAssignment>reverseOrder());

            if (course.hasAssignments()) {
                int animationStartOffset = initialAnimationStartOffset;
                for (int i = 0; i < assignments.size(); i++) {
                    TableRow row = createAssignmentRowFromAssignment(getContext(), inflater, assignments.get(i));
                    View divider = inflater.inflate(R.layout.view_divider, assignmentTable, false);

                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_with_slide);
                    animation.setStartOffset(animationStartOffset);
                    animation.setInterpolator(new AccelerateDecelerateInterpolator());
                    row.setAnimation(animation);
                    divider.setAnimation(animation);
                    animationStartOffset += 46 * Math.pow(0.8, i);

                    assignmentTable.addView(divider);
                    assignmentTable.addView(row);
                }
            }

            courseLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int courseLayoutHeight = courseLayout.getHeight();
                    int scrollViewHeight = scrollView.getHeight();
                    if (courseLayoutHeight <= scrollViewHeight) {
                        courseLayout.setPadding(courseLayout.getPaddingLeft(),
                                courseLayout.getPaddingTop(),
                                courseLayout.getPaddingRight(),
                                dpToPixels(58));
                        Log.d(TAG, "Adding extra padding to make sure FAB does not overlay information");
                    }
                    courseLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });

        }
    }

    private TableRow createAssignmentRowFromAssignment(Context context, LayoutInflater inflater, final CourseAssignment assignment) {
        TableRow row = (TableRow) inflater.inflate(R.layout.view_course_assignment, assignmentTable, false);
        final TextView name = (TextView) row.findViewById(R.id.text_assignment_name);
        if (assignment.isCustomAssignment() || assignment.isEditedAssignment()) {
            final ImageView edited = row.findViewById(R.id.image_edited);
            //edited.setColorFilter(Color.argb(132, 0, 0, 0), PorterDuff.Mode.MULTIPLY);
            edited.setVisibility(View.VISIBLE);
        }
        else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) name.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            name.setLayoutParams(params);
        }
        name.setText(assignment.getShortName(course.getAppropriateAssignmentNameLength()));
        final TextView points = (TextView) row.findViewById(R.id.text_assignment_points);
        final TextView grade = (TextView) row.findViewById(R.id.text_assignment_grade);
        CourseAssignment.Status status = assignment.getStatus();
        points.setGravity(Gravity.CENTER_HORIZONTAL);
        StringBuilder gradeBuilder;
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
                if (assignment.hasMaxGradeString()) {
                    points.setText("* / " + assignment.getMaxGradeString());
                }
                else {
                    points.setText("* / " + Integer.toString(assignment.getMaxGrade()));
                }
                grade.setText("Excluded");
                break;
            case NOT_GRADED:
                if (assignment.hasMaxGradeString()) {
                    points.setText("NG / " + assignment.getMaxGradeString());
                }
                else {
                    points.setText("NG / " + Integer.toString(assignment.getMaxGrade()));
                }
                grade.setText("Not Graded");
                break;
            case MISSING:
                if (assignment.hasMaxGradeString()) {
                    points.setText("M / " + assignment.getMaxGradeString());
                }
                else {
                    points.setText("M / " + Integer.toString(assignment.getMaxGrade()));
                }
                grade.setText("Missing");
                break;
            case EXTRA_CREDIT:
                gradeBuilder = new StringBuilder();
                if (assignment.hasStudentGradeString()) {
                    gradeBuilder.append(assignment.getStudentGradeString());
                }
                else {
                    gradeBuilder.append((int) assignment.getStudentGrade());
                }
                gradeBuilder.append(" / ");
                if (assignment.hasMaxGradeString()) {
                    gradeBuilder.append(assignment.getMaxGradeString());
                }
                else {
                    gradeBuilder.append(assignment.getMaxGrade());
                }
                points.setText(gradeBuilder.toString());
                grade.setText("Extra Credit");
                break;
            default:
                gradeBuilder = new StringBuilder();
                if (assignment.hasStudentGradeString()) {
                    gradeBuilder.append(assignment.getStudentGradeString());
                }
                else {
                    gradeBuilder.append((int) assignment.getStudentGrade());
                }
                gradeBuilder.append(" / ");
                if (assignment.hasMaxGradeString()) {
                    gradeBuilder.append(assignment.getMaxGradeString());
                }
                else {
                    gradeBuilder.append(assignment.getMaxGrade());
                }
                points.setText(gradeBuilder.toString());
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

        row.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (!editingAssignment) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle(assignment.getName());
                    String html = "<b>Points: </b>" + points.getText() + "<br>" +
                            "<b>Grade: </b>" + grade.getText() + "<br>" +
                            "<b>Assigned: </b>" + assigned.getText() + "<br>" +
                            "<b>Due: </b>" + due.getText();

                    if (assignment.hasDescription()) {
                        html += assignment.getDescription().contains("\n") ? "<br><b>Description:</b><br>" : "<br><b>Description: </b>";
                        html += assignment.getDescription().replace("\n", "<br>");
                    }

                    float dpi = getContext().getResources().getDisplayMetrics().density;
                    TextView messageView = new TextView(getContext());
                    messageView.setText(Html.fromHtml(html));
                    messageView.setLinksClickable(true);
                    messageView.setMovementMethod(LinkMovementMethod.getInstance());
                    messageView.setTextIsSelectable(true);
                    messageView.setTextColor(getResources().getColor(R.color.textPrimary));
                    messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.subheadingText));
                    messageView.setPadding(16, 0, 16, 0);

                    Linkify.addLinks(messageView, Linkify.WEB_URLS);

                    alertDialog.setView(messageView, (int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi) );
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    if (assignment.isCustomAssignment() || assignment.isEditedAssignment()) {
                        alertDialog.setIcon(R.drawable.ic_mode_edit);
                    }

                    alertDialog.show();
                }
                else {
                    Log.d(TAG, "Assignment details blocked from appearing with editingAssignment");
                }
            }
        });

        row.setTag(assignment);
        registerForContextMenu(row);

        return row;
    }

    private static final int CONTEXT_MENU_ID_EDIT = 1;
    private static final int CONTEXT_MENU_ID_DELETE = 2;
    private static final int CONTEXT_MENU_ID_RESET = 3;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        contextMenuAssignment = (CourseAssignment) v.getTag();
        if (!contextMenuAssignment.isCustomAssignment() && !contextMenuAssignment.isEditedAssignment()) {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            editAssignment(contextMenuAssignment, true);
        }
        else {
            menu.add(Menu.NONE, CONTEXT_MENU_ID_EDIT, CONTEXT_MENU_ID_EDIT, R.string.assignment_menu_edit);
            if (contextMenuAssignment.isCustomAssignment()) {
                menu.add(Menu.NONE, CONTEXT_MENU_ID_DELETE, CONTEXT_MENU_ID_DELETE, R.string.assignment_menu_delete);
            }
            else if (contextMenuAssignment.isEditedAssignment()) {
                menu.add(Menu.NONE, CONTEXT_MENU_ID_RESET, CONTEXT_MENU_ID_RESET, R.string.assignment_menu_reset);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTEXT_MENU_ID_EDIT:
                editAssignment(contextMenuAssignment, contextMenuAssignment.isEditedAssignment());
                return true;
            case CONTEXT_MENU_ID_DELETE:
                try {
                    CourseAssignmentFileHandler.removeSavedAssignment(getContext(), course.getId(), contextMenuAssignment);
                } catch (IOException e) {
                    Log.e(TAG, "IOException while attempting to remove saved assignment");
                    e.printStackTrace();
                }
                updateAssignment(contextMenuAssignment, null);
                return true;
            case CONTEXT_MENU_ID_RESET:
                try {
                    CourseAssignmentFileHandler.removeSavedAssignment(getContext(), course.getId(), contextMenuAssignment);
                } catch (IOException e) {
                    Log.e(TAG, "IOException while attempting to remove saved assignment");
                    e.printStackTrace();
                }
                for (int i = 0; i < replacedAssignments.size(); i++) {
                    if (contextMenuAssignment.overrides(replacedAssignments.get(i))) {
                        updateAssignment(contextMenuAssignment, replacedAssignments.remove(i));
                        break;
                    }
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void editAssignment(final CourseAssignment assignment, final boolean descOnly) {
        editingAssignment = true;
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        View newAssignmentView = inflater.inflate(R.layout.view_assignment_new, null);
        final TextInputLayout nameWrapper = newAssignmentView.findViewById(R.id.assignment_name_wrapper);
        final EditText nameEditText = newAssignmentView.findViewById(R.id.assignment_name_edittext);
        final TextInputLayout dateWrapper = newAssignmentView.findViewById(R.id.assignment_date_wrapper);
        final EditText dateEditText = newAssignmentView.findViewById(R.id.assignment_date_edittext);
        View categoryLayout = newAssignmentView.findViewById(R.id.category_layout);
        final Spinner spinner = newAssignmentView.findViewById(R.id.spinner);
        final EditText descriptionEditText = newAssignmentView.findViewById(R.id.assignment_description_edittext);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(assignment == null ? R.string.assignment_add_new_title : R.string.assignment_edit_title))
                .setPositiveButton(R.string.assignment_add_new_positive_button, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();

        dateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            private Calendar cal = Calendar.getInstance();

            @Override
            public void onFocusChange(View view, boolean b) {
                Editable editable = ((EditText) view).getText();
                if (editable.toString().isEmpty()) {
                    return;
                }

                String monthStr;
                String dayStr;
                String yearStr;
                if (editable.toString().length() == 6) {
                    if (editable.toString().contains(".")) {
                        dateWrapper.setError(getString(R.string.assignment_add_new_due_invalid));
                        return;
                    }
                    monthStr = editable.toString().substring(0, 2);
                    dayStr = editable.toString().substring(2, 4);
                    yearStr = editable.toString().substring(4, 6);
                }
                else {
                    String[] dates = editable.toString().split("/");
                    if (editable.toString().contains(".") || dates.length != 3) {
                        dateWrapper.setError(getString(R.string.assignment_add_new_due_invalid));
                        return;
                    }
                    monthStr = dates[0];
                    dayStr = dates[1];
                    yearStr = dates[2];
                }

                int month = Integer.parseInt(monthStr);
                int day = Integer.parseInt(dayStr);
                int year = Integer.parseInt(yearStr);

                if (month < 1 || month > 12) {
                    dateWrapper.setError(getString(R.string.assignment_add_new_due_invalid));
                    return;
                }
                cal.set(Calendar.MONTH, month - 1);

                if (year < 0 || year > 99) {
                    dateWrapper.setError(getString(R.string.assignment_add_new_due_invalid));
                    return;
                }
                int century = cal.get(Calendar.YEAR) - (cal.get(Calendar.YEAR) % 100);
                cal.set(Calendar.YEAR, century + year);

                if (day < 1 || day > cal.getActualMaximum(Calendar.DATE)) {
                    dateWrapper.setError(getString(R.string.assignment_add_new_due_invalid));
                    return;
                }
                cal.set(Calendar.DATE, day);

                dateWrapper.setErrorEnabled(false);
                editable.clear();
                editable.append(monthStr.length() == 2 ? monthStr : "0" + monthStr);
                editable.append('/');
                editable.append(dayStr.length() == 2 ? dayStr : "0" + dayStr);
                editable.append('/');
                editable.append(yearStr.length() == 2 ? yearStr : "0" + yearStr);
            }
        });

        if (course.hasCategories()) {
            String[] categories = new String[course.getCategories().size()];
            for (int i = 0; i < categories.length; i++) {
                categories[i] = course.getCategories().get(i).getName();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
        else {
            categoryLayout.setVisibility(View.GONE);
        }

        // has to be done before setting text
        if (descOnly) {
            nameWrapper.setHintAnimationEnabled(false);
        }

        if (assignment != null) {
            nameEditText.setText(assignment.getName());
            dateEditText.setText(DateUtil.mmddyyFormatter.print(assignment.getDue()));
            if (course.hasCategories() && assignment.hasCategory()) {
                for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
                    if (spinner.getAdapter().getItem(i).equals(assignment.getCategory())) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }
            if (assignment.hasDescription()) {
                descriptionEditText.setText(assignment.getDescription());
            }
        }
        else if (course.hasCategories()) {
            Log.i(TAG, getString(R.string.course_prefs_assignment_menu_category, course.getId()));
            String rememberedCategory = sharedPref.getString(getString(R.string.course_prefs_assignment_menu_category, course.getId()), null);
            if (rememberedCategory != null) {
                for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
                    if (spinner.getAdapter().getItem(i).equals(rememberedCategory)) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }
        }

        if (descOnly) {
            nameEditText.setEnabled(false);
            nameEditText.setFocusable(false);
            nameEditText.setFocusableInTouchMode(false);
            nameEditText.setClickable(false);
            dateEditText.setEnabled(false);
            dateEditText.setFocusable(false);
            dateEditText.setFocusableInTouchMode(false);
            dateEditText.setClickable(false);
            spinner.setEnabled(false);
            spinner.setFocusable(false);
            spinner.setFocusableInTouchMode(false);
            spinner.setClickable(false);

            TextView dateLabel = newAssignmentView.findViewById(R.id.assignment_date_label);
            TextView categoryLabel = newAssignmentView.findViewById(R.id.assignment_category_label);
            dateLabel.setEnabled(false);
            categoryLabel.setEnabled(false);
        }

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String selected = (String) adapterView.getAdapter().getItem(i);
                        Log.d(TAG, "Remembering selected category " + selected);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.course_prefs_assignment_menu_category, course.getId()), selected);
                        editor.apply();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        // Don't do anything
                    }
                });
            }
        });

        dialog.setView(newAssignmentView);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                editingAssignment = false;
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                editingAssignment = false;
            }
        });
        dialog.show();
        // set afterwards to stop button from always dismissing dialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validInput = true;

                String name = nameEditText.getText().toString();
                if (name.isEmpty()) {
                    nameWrapper.setError(getString(R.string.assignment_add_new_name_empty));
                    validInput = false;
                }
                else {
                    nameWrapper.setErrorEnabled(false);
                }

                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int yearPrefix = currentYear - (currentYear % 100);
                Date due = null;
                try {
                    String[] dates = dateEditText.getText().toString().split("/");
                    dates[2] = String.valueOf(yearPrefix + Integer.parseInt(dates[2]));
                    due = df.parse(String.format("%s/%s/%s", dates[0], dates[1], dates[2]));
                } catch (ParseException | IndexOutOfBoundsException | NumberFormatException e) {
                    if (dateEditText.getText().toString().isEmpty()) {
                        dateWrapper.setError(getString(R.string.assignment_add_new_due_empty));
                    }
                    else {
                        dateWrapper.setError(getString(R.string.assignment_add_new_due_invalid));
                    }
                    validInput = false;
                }

                String category = null;
                if (course.hasCategories()) {
                    category = course.getCategories().get(spinner.getSelectedItemPosition()).getName();
                }

                String description = descriptionEditText.getText().toString();
                if (description.isEmpty()) {
                    description = null;
                }

                if (validInput) {
                    // no change was actually made to the existing assignment
                    if (assignment != null && descOnly &&
                            ((description == null && assignment.getDescription() == null) || description.equals(assignment.getDescription()))) {
                        dialog.cancel();
                        return;
                    }

                    DateTime now = DateTime.now();
                    DateTime assigned = assignment != null ? assignment.getAssigned() : now;
                    CourseAssignment newAssignment = new CourseAssignment(name, assigned, new DateTime(due), now, category, -1, "*", -1, "*", null, -1, null, description, CourseAssignment.Status.NOT_GRADED, course.getCurrentMarkingPeriod().getId());
                    if (assignment != null && !assignment.isCustomAssignment()) {
                        newAssignment.setEditedAssignment(true);
                        replacedAssignments.add(assignment);
                    }
                    else {
                        newAssignment.setCustomAssignment(true);
                    }
                    try {
                        if (assignment != null) {
                            CourseAssignmentFileHandler.removeSavedAssignment(getContext(), course.getId(), assignment);
                        }
                        CourseAssignmentFileHandler.addSavedAssignment(getContext(), course.getId(), newAssignment);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException while saving changes of editing assignment");
                        e.printStackTrace();
                    }

                    updateAssignment(assignment, newAssignment);
                    dialog.dismiss();
                }
            }
        });
    }

    private void updateAssignment(CourseAssignment from, CourseAssignment to) {
        if (from != null) {
            for (int i = 0; i < assignmentTable.getChildCount(); i++) {
                View row = assignmentTable.getChildAt(i);
                if (row.getTag() != null && row.getTag().equals(from)) {
                    assignmentTable.removeViewAt(i); // remove assignment row
                    assignmentTable.removeViewAt(i - 1); // remove divider
                    course.getAssignments().remove(row.getTag());
                    break;
                }
            }
        }

        if (to != null) {
            course.getAssignments().add(to);
            Collections.sort(course.getAssignments(), Collections.<CourseAssignment>reverseOrder());

            LayoutInflater inflater = LayoutInflater.from(getContext());
            TableRow newRow = createAssignmentRowFromAssignment(getContext(), inflater, to);
            View divider = inflater.inflate(R.layout.view_divider, assignmentTable, false);

            int pos = course.getAssignments().indexOf(to) * 2 + 1;
            assignmentTable.addView(newRow, pos);
            assignmentTable.addView(divider, pos);
        }
    }

    public void resetCourse() {
        try {
            CourseAssignmentFileHandler.clearSavedAssignmentsForCourse(getContext(), id);
        } catch (IOException e) {
            Log.e(TAG, "IOException while resetting course");
            e.printStackTrace();
        }
        if (course != null) {
            for (int i = course.getAssignments().size() - 1; i >= 0; i--) {
                if (course.getAssignments().get(i).isCustomAssignment()) {
                    course.getAssignments().remove(i);
                }
                else if (course.getAssignments().get(i).isEditedAssignment()) {
                    for (int j = 0; j < replacedAssignments.size(); j++) {
                        if (course.getAssignments().get(i).overrides(replacedAssignments.get(j))) {
                            course.getAssignments().remove(i);
                            course.getAssignments().add(i, replacedAssignments.remove(j));
                            break;
                        }
                    }
                }
            }
        }
        if (requestFinished && !networkFailed) {
            redraw(getView());
        }
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        courseLayout = (ConstraintLayout) view.findViewById(R.id.course_fragment_course_layout);
        progressLayout = (LinearLayout) view.findViewById(R.id.layout_loading);
        scrollView = view.findViewById(R.id.nestedscrollview);

        fab = view.findViewById(R.id.fab);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        final ScrollAwareFABBehavior behavior = (ScrollAwareFABBehavior) params.getBehavior();
        if (behavior != null) {
            final HorizontalScrollViewWithListener assignmentsScrollView = view.findViewById(R.id.scrollview_assignments);
            assignmentsScrollView.setHorizontalScrollViewListener(new HorizontalScrollViewWithListener.HorizontalScrollViewListener() {
                @Override
                public void onScrollChanged(HorizontalScrollViewWithListener scrollView, int x, int y, int oldx, int oldy) {
                    behavior.onHorizontalScroll(fab, assignmentsScrollView, x, y, oldx, oldy);
                }

                @Override
                public void onEndScroll() {
                    // unused
                }
            });
        }
        else {
            Log.e(TAG, "FAB has no behavior!");
        }

        if (course == null) {
            courseLayout.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
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
        if (courseLayout != null && courseLayout.getVisibility() == View.VISIBLE) {
            courseLayout.setVisibility(View.GONE);
        }
        if (fab != null) {
            fab.setVisibility(View.GONE);
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
