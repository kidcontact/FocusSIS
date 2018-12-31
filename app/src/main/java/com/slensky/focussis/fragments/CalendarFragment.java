package com.slensky.focussis.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDRootLayout;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.R;
import com.slensky.focussis.activities.MainActivity;
import com.slensky.focussis.data.GoogleCalendarEvent;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.network.FocusApiSingleton;
import com.slensky.focussis.util.LayoutUtil;
import com.slensky.focussis.util.Syncable;
import com.slensky.focussis.util.TermUtil;
import com.slensky.focussis.views.CalendarDayDisableAllDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONObject;

import com.slensky.focussis.data.Calendar;
import com.slensky.focussis.data.CalendarEvent;
import com.slensky.focussis.data.CalendarEventDetails;
import com.slensky.focussis.data.ScheduleCourse;
import com.slensky.focussis.network.RequestSingleton;
import com.slensky.focussis.util.DateUtil;
import com.slensky.focussis.views.CalendarDayDecorator;
import com.slensky.focussis.views.CalendarDayEnableAllDecorator;

import java.text.DateFormatSymbols;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slensky on 5/8/17.
 */

public class CalendarFragment extends NetworkTabAwareFragment implements Syncable {

    private static final String TAG = "CalendarFragment";
    private int year;
    private int month;
    private MaterialCalendarView calendarView;
    private ProgressBar loading;
    private List<DayViewDecorator> eventsDecorator = new ArrayList<>();
    private DayViewDecorator enableDecorator;
    private DayViewDecorator disableDecorator;
    private boolean firstRun = true;
    private TextView eventHint;
    private TextView dateHeader;
    private TextView assignmentHeader;
    private TextView eventHeader;
    private LinearLayout assignmentLayout;
    private LinearLayout eventLayout;
    Calendar calendar;

    private CalendarEventDetails eventDetailsForCurrentDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        java.util.Calendar today = java.util.Calendar.getInstance();
        year = today.get(java.util.Calendar.YEAR);
        month = today.get(java.util.Calendar.MONTH);
        api = FocusApiSingleton.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        View view = inflater.inflate(com.slensky.focussis.R.layout.fragment_calendar, container, false);

        loading = (ProgressBar) view.findViewById(com.slensky.focussis.R.id.progress_calendar);
        eventHint = (TextView) view.findViewById(com.slensky.focussis.R.id.text_calendar_hint);
        dateHeader = (TextView) view.findViewById(com.slensky.focussis.R.id.text_calendar_date_header);
        dateHeader.setVisibility(View.GONE);
        assignmentHeader = (TextView) view.findViewById(com.slensky.focussis.R.id.text_calendar_assignments_header);
        assignmentHeader.setVisibility(View.GONE);
        eventHeader = (TextView) view.findViewById(com.slensky.focussis.R.id.text_calendar_events_header);
        eventHeader.setVisibility(View.GONE);
        assignmentLayout = (LinearLayout) view.findViewById(com.slensky.focussis.R.id.ll_calendar_assignments);
        assignmentLayout.removeAllViews();
        eventLayout = (LinearLayout) view.findViewById(com.slensky.focussis.R.id.ll_calendar_events);
        eventLayout.removeAllViews();
        calendarView = (MaterialCalendarView) view.findViewById(com.slensky.focussis.R.id.calendarView);
        int selectionColor = ContextCompat.getColor(getContext(), com.slensky.focussis.R.color.calendarSelectBackground);
        calendarView.setSelectionColor(selectionColor);
        calendarView.setShowOtherDates(MaterialCalendarView.SHOW_NONE);
        disableDecorator = new CalendarDayDisableAllDecorator(calendarView.getCurrentDate().getMonth());
        calendarView.addDecorator(disableDecorator);
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
                api.cancelAll(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return true;
                    }
                });
                eventHint.setVisibility(View.VISIBLE);
                dateHeader.setVisibility(View.GONE);
                assignmentLayout.removeAllViews();
                eventLayout.removeAllViews();
                assignmentHeader.setVisibility(View.GONE);
                eventHeader.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                year = materialCalendarView.getCurrentDate().getYear();
                month = materialCalendarView.getCurrentDate().getMonth();
                refresh();
                //materialCalendarView.addDecorator(new CalendarDayEnableAllDecorator(materialCalendarView.getCurrentDate().getMonth()));
                //materialCalendarView.addDecorator(new CalendarDayDisableAllDecorator(materialCalendarView.getCurrentDate().getMonth()));
            }
        });
        sizeCalendar();

        title = getString(com.slensky.focussis.R.string.calendar_label);
        refresh();

        return view;
    }

    protected void onSuccess(Calendar response) {
        calendar = response;
        final View view = getView();
        if (view != null) {
            calendarView.removeDecorator(enableDecorator);
            final DayViewDecorator tempDisableDecorator = new CalendarDayDisableAllDecorator(calendarView.getCurrentDate().getMonth());
            calendarView.addDecorator(tempDisableDecorator);
            Thread decoratorThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (calendar == null) {
                        return;
                    }
                    final int occasionColor = ContextCompat.getColor(getContext(), com.slensky.focussis.R.color.calendarOccasionDecorator);
                    final int assignmentColor = ContextCompat.getColor(getContext(), com.slensky.focussis.R.color.calendarAssignmentDecorator);

                    final SparseArray<ArrayDeque<CalendarEvent>> eventsByDay = new SparseArray<>();
                    if (calendar.getEvents() != null) {
                        for (CalendarEvent e : calendar.getEvents()) {
                            int d = e.getDate().dayOfMonth().get();
                            ArrayDeque<CalendarEvent> a = eventsByDay.get(d);
                            if (a == null) {
                                a = new ArrayDeque<>();
                                eventsByDay.append(d, a);
                            }
                            a.add(e);
                        }

                        final int[] count = {0};
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (DayViewDecorator d : eventsDecorator) {
                                    calendarView.removeDecorator(d);
                                    count[0] += 1;
                                }
                                calendarView.addDecorator(tempDisableDecorator);
                            }
                        });
                        while (count[0] < eventsDecorator.size()) {
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        eventsDecorator.clear();

                        count[0] = 0;
                        for (int i = 0; i < eventsByDay.size(); i++) {
                            final int k = eventsByDay.keyAt(i);
                            final ArrayDeque<CalendarEvent> a = eventsByDay.get(k);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    eventsDecorator.add(new CalendarDayDecorator(k, calendarView.getCurrentDate().getMonth(), calendarView.getCurrentDate().getYear(), a, occasionColor, assignmentColor, firstRun));
                                    calendarView.addDecorator(eventsDecorator.get(eventsDecorator.size() - 1));
                                    count[0] += 1;
                                }
                            });
                        }

                        while (count[0] < eventsByDay.size()) {
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                            @Override
                            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
                                eventHint.setVisibility(View.GONE);
                                dateHeader.setVisibility(View.VISIBLE);
                                DateTime selected = new DateTime(calendarDay.getYear(), calendarDay.getMonth() + 1, calendarDay.getDay(), 0, 0);
                                dateHeader.setText(selected.monthOfYear().getAsText() + " " + selected.dayOfMonth().getAsText() + ", " + selected.year().getAsText());
                                ArrayDeque<CalendarEvent> events = eventsByDay.get(calendarDay.getDay());
                                assignmentLayout.removeAllViews();
                                eventLayout.removeAllViews();
                                if (events != null) {
                                    ArrayDeque<CalendarEvent> occasions = new ArrayDeque<CalendarEvent>();
                                    ArrayDeque<CalendarEvent> assignments = new ArrayDeque<CalendarEvent>();
                                    for (CalendarEvent e : events) {
                                        if (e.getType() == CalendarEvent.EventType.OCCASION) {
                                            occasions.add(e);
                                        }
                                        else {
                                            assignments.add(e);
                                        }
                                    }
                                    if (!assignments.isEmpty()) {
                                        assignmentHeader.setVisibility(View.VISIBLE);
                                        for (final CalendarEvent e : assignments) {
                                            final TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(com.slensky.focussis.R.layout.view_calendar_event, assignmentLayout, false);
                                            final CharSequence text = Html.fromHtml("&#8226; ") + e.getName() + "\n";
                                            final SpannableString notClickedString = new SpannableString(text);
                                            notClickedString.setSpan(new URLSpan(""), 2, notClickedString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
                                            final SpannableString clickedString = new SpannableString(notClickedString);
                                            clickedString.setSpan(new ForegroundColorSpan(getResources().getColor(com.slensky.focussis.R.color.colorAccent)), 2, notClickedString.length(),
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
                                                    final MaterialDialog dialog = createEventDetailDialog(e);
                                                    dialog.show();
                                                    api.getCalendarEvent(e.getId(), e.getType(), new FocusApi.Listener<CalendarEventDetails>() {
                                                        @Override
                                                        public void onResponse(CalendarEventDetails response) {
                                                            onEventRequestSuccess(response, dialog);
                                                        }
                                                    }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            onEventRequestError(error, dialog);
                                                        }
                                                    });
                                                }
                                            });
                                            assignmentLayout.addView(tv);
                                        }
                                    }
                                    else {
                                        assignmentHeader.setVisibility(View.GONE);
                                    }
                                    if (!occasions.isEmpty()) {
                                        eventHeader.setVisibility(View.VISIBLE);
                                        for (final CalendarEvent e : occasions) {
                                            final TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(com.slensky.focussis.R.layout.view_calendar_event, eventLayout, false);
                                            final CharSequence text = Html.fromHtml("&#8226; ") + e.getName() + "\n";
                                            final SpannableString notClickedString = new SpannableString(text);
                                            notClickedString.setSpan(new URLSpan(""), 2, notClickedString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
                                            final SpannableString clickedString = new SpannableString(notClickedString);
                                            clickedString.setSpan(new ForegroundColorSpan(getResources().getColor(com.slensky.focussis.R.color.colorAccent)), 2, notClickedString.length(),
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

                                                    final MaterialDialog dialog = createEventDetailDialog(e);
                                                    dialog.show();
                                                    api.getCalendarEvent(e.getId(), e.getType(), new FocusApi.Listener<CalendarEventDetails>() {
                                                        @Override
                                                        public void onResponse(CalendarEventDetails response) {
                                                            onEventRequestSuccess(response, dialog);
                                                        }
                                                    }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            onEventRequestError(error, dialog);
                                                        }
                                                    });

                                                }
                                            });
                                            eventLayout.addView(tv);
                                        }
                                    }
                                    else {
                                        eventHeader.setVisibility(View.GONE);
                                    }
                                }
                                else {
                                    assignmentHeader.setVisibility(View.GONE);
                                    eventHeader.setVisibility(View.GONE);
                                }
                            }
                        });
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            enableDecorator = new CalendarDayEnableAllDecorator(calendarView.getCurrentDate().getMonth());
                            calendarView.addDecorator(enableDecorator);
                            loading.setVisibility(View.GONE);
                            calendarView.removeDecorator(disableDecorator);
                            disableDecorator = tempDisableDecorator;
                        }
                    });

                    firstRun = false;
                    requestFinished = true;
                }
            });
            decoratorThread.start();
        }

    }

    private void sizeCalendar() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.i(TAG, "dpWidth: " + dpWidth);
        if(dpWidth > 450){
            Log.d(TAG, "Screen has large width, setting calendarView to hardcoded dp width");
            ViewGroup.LayoutParams params = calendarView.getLayoutParams();
            params.width = LayoutUtil.dpToPixels(getContext(), 450);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sizeCalendar();
    }

    @Override
    protected void makeRequest() {
        calendar = null;
        api.getCalendar(year, month + 1, new FocusApi.Listener<Calendar>() {  // month is 0 indexed in java calendar
            @Override
            public void onResponse(Calendar response) {
                onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onError(error);
            }
        });
    }

    private MaterialDialog createEventDetailDialog(CalendarEvent e) {
        Log.d(TAG, "Creating event details dialog");
        eventDetailsForCurrentDialog = null;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title(e.getName());
        RelativeLayout dialogLayout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(com.slensky.focussis.R.layout.view_event_dialog, null, false);
        if (e.getType() == CalendarEvent.EventType.OCCASION) {
            LinearLayout details = (LinearLayout) LayoutInflater.from(getContext()).inflate(com.slensky.focussis.R.layout.view_occasion_details, null, false);
            details.setVisibility(View.INVISIBLE);
            dialogLayout.addView(details);
        }
        else {
            LinearLayout details = (LinearLayout) LayoutInflater.from(getContext()).inflate(com.slensky.focussis.R.layout.view_assignment_event_details, null, false);
            details.setVisibility(View.INVISIBLE);
            dialogLayout.addView(details);
        }
        builder.customView(dialogLayout, true)
                .positiveText(getString(com.slensky.focussis.R.string.calendar_event_dialog_button))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .negativeText(R.string.calendar_event_dialog_export)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (eventDetailsForCurrentDialog == null) {
                            Toast.makeText(getContext(), R.string.calendar_event_dialog_export_wait, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (!(getActivity() instanceof MainActivity) || getContext() == null) {
                                Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
                                return;
                            }
                            List<GoogleCalendarEvent> events = new ArrayList<>();
                            events.add(eventDetailsForCurrentDialog);
                            ((MainActivity) getActivity()).exportEventsToCalendar(events, false, null);
                            dialog.dismiss();
                        }
                    }
                }).autoDismiss(false);

        return builder.build();
    }

    private void onEventRequestSuccess(CalendarEventDetails eventDetails, MaterialDialog dialog) {
        Log.d(TAG, "Calendar event request success");
        eventDetailsForCurrentDialog = eventDetails;
        TextView date = (TextView) dialog.findViewById(com.slensky.focussis.R.id.text_event_date);
        date.setText(Html.fromHtml("<b>" + getString(com.slensky.focussis.R.string.calendar_event_date) + ": </b>"
                + DateUtil.dateTimeToShortString(eventDetails.getDate())));
        TextView school = (TextView) dialog.findViewById(com.slensky.focussis.R.id.text_event_school);
        school.setText(Html.fromHtml("<b>" + getString(com.slensky.focussis.R.string.calendar_event_school) + ": </b>"
                + eventDetails.getSchool()));

        if (eventDetails.getType() == CalendarEvent.EventType.ASSIGNMENT) {
            TextView course = (TextView) dialog.findViewById(com.slensky.focussis.R.id.text_event_course);
            course.setText(Html.fromHtml("<b>" + getString(com.slensky.focussis.R.string.calendar_event_course_name) + ": </b>"
                    + eventDetails.getCourseName()));
            TextView teacher = (TextView) dialog.findViewById(com.slensky.focussis.R.id.text_event_teacher);
            String[] teacherNames = eventDetails.getCourseTeacher().split(" ");
            if (teacherNames.length == 1) {
                teacher.setText(Html.fromHtml("<b>" + getString(com.slensky.focussis.R.string.calendar_event_teacher) + ": </b>"
                        + eventDetails.getCourseTeacher()));
            }
            else {
                teacherNames[1] = teacherNames[teacherNames.length - 1];
                teacher.setText(Html.fromHtml("<b>" + getString(com.slensky.focussis.R.string.calendar_event_teacher) + ": </b>"
                        + teacherNames[1] + ", " + teacherNames[0]));
            }
            TextView section = (TextView) dialog.findViewById(com.slensky.focussis.R.id.text_event_section);
            String sectionStr = "";
            if (eventDetails.hasPeriod()) {
                sectionStr = "Period " + eventDetails.getCoursePeriod() + " - ";
            } else {
                sectionStr = eventDetails.getCourseSection() + " - ";
            }
            if (eventDetails.getCourseTerm() != ScheduleCourse.Term.YEAR) {
                sectionStr += TermUtil.termToStringAbbr(eventDetails.getCourseTerm()) + " - ";
            }
            sectionStr += eventDetails.getCourseDays() + " - " + eventDetails.getCourseTeacher();
            section.setText(Html.fromHtml("<b>" + getString(com.slensky.focussis.R.string.calendar_event_section) + ": </b>"
                    + sectionStr));
            TextView notes = (TextView) dialog.findViewById(com.slensky.focussis.R.id.text_event_notes);
            String notesStr;
            if (eventDetails.hasNotes()) {
                notesStr = eventDetails.getNotes();
            }
            else {
                notesStr = "-";
            }
            notes.setText(Html.fromHtml("<b>" + getString(com.slensky.focussis.R.string.calendar_event_notes) + ": </b>"
                    + notesStr));
        }

        ProgressBar loading = (ProgressBar) dialog.findViewById(com.slensky.focussis.R.id.progress_event_details);
        loading.setVisibility(View.GONE);
        LinearLayout eventDetailsLayout = (LinearLayout) dialog.findViewById(com.slensky.focussis.R.id.ll_event_details);
        eventDetailsLayout.setVisibility(View.VISIBLE);

    }

    private void onEventRequestError(VolleyError error, Dialog dialog) {
        dialog.cancel();
        Toast.makeText(getContext(), getString(com.slensky.focussis.R.string.network_error_timeout), Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error getting calendar event details");
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
    public void sync() {
        if (!(getActivity() instanceof MainActivity) || getContext() == null) {
            Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
            return;
        }

        View view = View.inflate(getContext(), R.layout.view_calendar_sync_dialog, null);
        final Spinner spinnerFromMonth = view.findViewById(R.id.spinner_from_month);
        final Spinner spinnerFromYear = view.findViewById(R.id.spinner_from_year);
        final Spinner spinnerToMonth = view.findViewById(R.id.spinner_to_month);
        final Spinner spinnerToYear = view.findViewById(R.id.spinner_to_year);
        final LinearLayout eventsLayout = view.findViewById(R.id.ll_checkbox_events);
        final LinearLayout assignmentsLayout = view.findViewById(R.id.ll_checkbox_assignments);
        final CheckBox eventsCheckbox = view.findViewById(R.id.checkbox_events);
        final CheckBox assignmentsCheckbox = view.findViewById(R.id.checkbox_assignments);

        String[] months = new DateFormatSymbols().getMonths();
        Integer[] years = new Integer[5];
        for (int i = 0; i < years.length; i++) {
            years[i] = year + i - 2;
        }

        ArrayAdapter<String> fromMonthAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, months);
        fromMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromMonth.setAdapter(fromMonthAdapter);
        spinnerFromMonth.setSelection(month);
        ArrayAdapter<Integer> fromYearAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, years);
        fromYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromYear.setAdapter(fromYearAdapter);
        spinnerFromYear.setSelection(2);
        ArrayAdapter<String> toMonthAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, months);
        toMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToMonth.setAdapter(toMonthAdapter);
        spinnerToMonth.setSelection(month);
        ArrayAdapter<Integer> toYearAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, years);
        toYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToYear.setAdapter(toYearAdapter);
        spinnerToYear.setSelection(2);

        final Spinner.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int fromMonth = spinnerFromMonth.getSelectedItemPosition();
                int fromYear = (Integer) spinnerFromYear.getSelectedItem();
                int toMonth = spinnerToMonth.getSelectedItemPosition();
                int toYear = (Integer) spinnerToYear.getSelectedItem();
                LocalDate from = new LocalDate(fromYear, fromMonth + 1, 1);
                LocalDate to = new LocalDate(toYear, toMonth + 1, 1);
                if (from.isAfter(to)) {
                    Toast.makeText(view.getContext(), R.string.calendar_sync_dialog_date_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        };

        spinnerFromMonth.post(new Runnable() {
            @Override
            public void run() {
                spinnerFromMonth.setOnItemSelectedListener(onItemSelectedListener);
            }
        });
        spinnerFromYear.post(new Runnable() {
            @Override
            public void run() {
                spinnerFromYear.setOnItemSelectedListener(onItemSelectedListener);
            }
        });
        spinnerToYear.post(new Runnable() {
            @Override
            public void run() {
                spinnerToYear.setOnItemSelectedListener(onItemSelectedListener);
            }
        });
        spinnerToMonth.post(new Runnable() {
            @Override
            public void run() {
                spinnerToMonth.setOnItemSelectedListener(onItemSelectedListener);
            }
        });

        eventsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (eventsCheckbox.isChecked() && !assignmentsCheckbox.isChecked()) {
                    Toast.makeText(getContext(), R.string.sync_dialog_min_select_error, Toast.LENGTH_SHORT).show();
                }
                else {
                    eventsCheckbox.toggle();
                }
            }
        });
        assignmentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (assignmentsCheckbox.isChecked() && !eventsCheckbox.isChecked()) {
                    Toast.makeText(getContext(), R.string.sync_dialog_min_select_error, Toast.LENGTH_SHORT).show();
                }
                else {
                    assignmentsCheckbox.toggle();
                }
            }
        });

        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.sync_to_google_calendar)
                .customView(view, false)
                .autoDismiss(false)
                .positiveText(R.string.sync_to_google_calendar_positive)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        int fromMonth = spinnerFromMonth.getSelectedItemPosition();
                        int fromYear = (Integer) spinnerFromYear.getSelectedItem();
                        int toMonth = spinnerToMonth.getSelectedItemPosition();
                        int toYear = (Integer) spinnerToYear.getSelectedItem();
                        LocalDate from = new LocalDate(fromYear, fromMonth + 1, 1);
                        LocalDate to = new LocalDate(toYear, toMonth + 1, 1);
                        if (from.isAfter(to)) {
                            Toast.makeText(getContext(), R.string.calendar_sync_dialog_date_error, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            boolean exportEvents = eventsCheckbox.isChecked();
                            boolean exportAssignments = assignmentsCheckbox.isChecked();
                            performSync(fromYear, fromMonth, toYear, toMonth, exportEvents, exportAssignments, null, null);
                            dialog.dismiss();
                        }
                    }
                }).negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();

        dialog.show();
    }

    private void performSync(int fromYear, int fromMonth, int toYear, int toMonth, boolean exportEvents, boolean exportAssignments, @Nullable List<Calendar> calendars, @Nullable List<CalendarEventDetails> assignmentDetails) {
        if (!(getActivity() instanceof MainActivity) || getContext() == null) {
            Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
            return;
        }

        if (calendars == null) {
            getCalendarsToExport(fromYear, fromMonth, toYear, toMonth, exportEvents, exportAssignments);
            return;
        }
        if (exportAssignments && assignmentDetails == null) {
            getAssignmentDetailsForCalendarsToExport(exportEvents, calendars);
            return;
        }

        List<GoogleCalendarEvent> events = new ArrayList<>();
        if (exportEvents) {
            for (Calendar c : calendars) {
                for (CalendarEvent e : c.getEvents()) {
                    if (e.getType().equals(CalendarEvent.EventType.OCCASION)) {
                        events.add(e);
                    }
                }
            }
        }
        if (exportAssignments) {
            events.addAll(assignmentDetails);
        }

        if (events.size() > 0) {
            Log.d(TAG, "Exporting " + events.size() + " events to calendar");
            ((MainActivity) getActivity()).exportEventsToCalendar(events, false, null);
        }

//        Log.i(TAG, "CALENDARS LENGTH: " + calendars.size());
//        Log.i(TAG, "ASSIGNMENT DETAILS LENGTH: " + assignmentDetails.size());
    }

    private void getCalendarsToExport(final int fromYear, final int fromMonth, final int toYear, final int toMonth, final boolean exportEvents, final boolean exportAssignments) {
        if (!(getActivity() instanceof MainActivity) || getContext() == null) {
            Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
            return;
        }

        java.util.Calendar current = java.util.Calendar.getInstance();
        current.set(fromYear, fromMonth, 1);
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.set(toYear, toMonth, 2);

        int yearDiff = toYear - fromYear;
        final int monthDiff = yearDiff * 12 + toMonth - fromMonth;
        Log.i(TAG, "monthdiff " + monthDiff);

        final List<Calendar> calendars = new ArrayList<>();
        final List<Request> requests = new ArrayList<>();
        final boolean[] hasError = new boolean[]{false};
        final MaterialDialog progress = new MaterialDialog.Builder(getContext())
                .content(R.string.calendar_sync_calendars_progress)
                .progress(false, monthDiff + 1, true)
                .negativeText(R.string.cancel)
                .canceledOnTouchOutside(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        for (Request request : requests) {
                            Log.i(TAG, "Cancelling request");
                            request.cancel();
                        }
                    }
                })
                .build();
        progress.show();

        for (; current.before(end); current.add(java.util.Calendar.MONTH, 1)) {
            int month = current.get(java.util.Calendar.MONTH);
            int year = current.get(java.util.Calendar.YEAR);
            if (CalendarFragment.this.calendar != null
                    && year == CalendarFragment.this.year && month == CalendarFragment.this.month) {
                calendars.add(CalendarFragment.this.calendar);
            }
            else {
                requests.add(api.getCalendar(year, month + 1, new FocusApi.Listener<Calendar>() {
                    @Override
                    public void onResponse(Calendar calendar) {
                        progress.incrementProgress(1);
                        calendars.add(calendar);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hasError[0] = true;
                    }
                }));
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!progress.isCancelled() && calendars.size() < monthDiff + 1) {
                    if (!(getActivity() instanceof MainActivity) || getContext() == null) {
                        Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
                        return;
                    }

                    if (hasError[0]) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                for (Request request : requests) {
                                    request.cancel();
                                }
                                new MaterialDialog.Builder(getContext())
                                        .content(R.string.export_network_error)
                                        .positiveText(R.string.ok)
                                        .show();
                            }
                        });
                        return;
                    }
                }

                if (!progress.isCancelled()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            performSync(fromYear, fromMonth, toYear, toMonth, exportEvents, exportAssignments, calendars, null);
                        }
                    });
                }
                Log.d(TAG, "Exiting get calendars thread");

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void getAssignmentDetailsForCalendarsToExport(final boolean exportEvents, @NonNull final List<Calendar> calendars) {
        if (!(getActivity() instanceof MainActivity) || getContext() == null) {
            Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
            return;
        }

        final List<CalendarEvent> assignments = new ArrayList<>();
        for (Calendar c : calendars) {
            for (CalendarEvent e : c.getEvents()) {
                if (e.getType().equals(CalendarEvent.EventType.ASSIGNMENT)) {
                    assignments.add(e);
                }
            }
        }

        final List<CalendarEventDetails> assignmentDetails = new ArrayList<>();
        final List<Request> requests = new ArrayList<>();
        final boolean[] hasError = new boolean[]{false};
        final MaterialDialog progress = new MaterialDialog.Builder(getContext())
                .content(R.string.calendar_sync_assignments_progress)
                .progress(false, assignments.size(), true)
                .negativeText(R.string.cancel)
                .canceledOnTouchOutside(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        for (Request request : requests) {
                            Log.i(TAG, "Cancelling request");
                            request.cancel();
                        }
                    }
                })
                .build();
        progress.show();

        for (final CalendarEvent e : assignments) {
            requests.add(api.getCalendarEvent(e.getId(), CalendarEvent.EventType.ASSIGNMENT, new FocusApi.Listener<CalendarEventDetails>() {
                @Override
                public void onResponse(CalendarEventDetails details) {
                    progress.incrementProgress(1);
                    assignmentDetails.add(details);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hasError[0] = true;
                }
            }));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!progress.isCancelled() && assignmentDetails.size() < assignments.size()) {
                    if (!(getActivity() instanceof MainActivity) || getContext() == null) {
                        Log.e(TAG, "Could not export events because activity was null or not MainActivity or context was null");
                        return;
                    }

                    if (hasError[0]) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                for (Request request : requests) {
                                    request.cancel();
                                }
                                new MaterialDialog.Builder(getContext())
                                        .content(R.string.export_network_error)
                                        .positiveText(R.string.ok)
                                        .show();
                            }
                        });
                        return;
                    }
                }

                if (!progress.isCancelled()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            performSync(-1, -1, -1, -1, exportEvents, true, calendars, assignmentDetails);
                        }
                    });
                }
                Log.d(TAG, "Exiting get assignment details thread");

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
