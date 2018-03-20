package org.kidcontact.focussis.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
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
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.data.Calendar;
import org.kidcontact.focussis.data.CalendarEvent;
import org.kidcontact.focussis.data.CalendarEventDetails;
import org.kidcontact.focussis.data.ScheduleCourse;
import org.kidcontact.focussis.network.ApiBuilder;
import org.kidcontact.focussis.network.FocusApiSingleton;
import org.kidcontact.focussis.network.RequestSingleton;
import org.kidcontact.focussis.util.DateUtil;
import org.kidcontact.focussis.util.TermUtil;
import org.kidcontact.focussis.views.CalendarDayDecorator;
import org.kidcontact.focussis.views.CalendarDayDisableAllDecorator;
import org.kidcontact.focussis.views.CalendarDayEnableAllDecorator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by slensky on 5/8/17.
 */

public class CalendarFragment extends NetworkTabAwareFragment {

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        java.util.Calendar today = java.util.Calendar.getInstance();
        year = today.get(java.util.Calendar.YEAR);
        month = today.get(java.util.Calendar.MONTH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        loading = (ProgressBar) view.findViewById(R.id.progress_calendar);
        eventHint = (TextView) view.findViewById(R.id.text_calendar_hint);
        dateHeader = (TextView) view.findViewById(R.id.text_calendar_date_header);
        assignmentHeader = (TextView) view.findViewById(R.id.text_calendar_assignments_header);
        assignmentHeader.setVisibility(View.GONE);
        eventHeader = (TextView) view.findViewById(R.id.text_calendar_events_header);
        eventHeader.setVisibility(View.GONE);
        assignmentLayout = (LinearLayout) view.findViewById(R.id.ll_calendar_assignments);
        assignmentLayout.removeAllViews();
        eventLayout = (LinearLayout) view.findViewById(R.id.ll_calendar_events);
        eventLayout.removeAllViews();
        calendarView = (MaterialCalendarView) view.findViewById(R.id.calendarView);
        int selectionColor = ContextCompat.getColor(getContext(), R.color.calendarSelectBackground);
        calendarView.setSelectionColor(selectionColor);
        calendarView.setShowOtherDates(MaterialCalendarView.SHOW_NONE);
        disableDecorator = new CalendarDayDisableAllDecorator(calendarView.getCurrentDate().getMonth());
        calendarView.addDecorator(disableDecorator);
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
                RequestSingleton.getInstance(getContext()).getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
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

        api = FocusApiSingleton.getApi();
        title = getString(R.string.calendar_label);
        refresh();

        return view;
    }

    @Override
    protected void onSuccess(JSONObject response) {
        final Calendar calendar = new Calendar(response);
        final View view = getView();
        if (view != null) {
            calendarView.removeDecorator(enableDecorator);
            final DayViewDecorator tempDisableDecorator = new CalendarDayDisableAllDecorator(calendarView.getCurrentDate().getMonth());
            calendarView.addDecorator(tempDisableDecorator);
            Thread decoratorThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final int occasionColor = ContextCompat.getColor(getContext(), R.color.calendarOccasionDecorator);
                    final int assignmentColor = ContextCompat.getColor(getContext(), R.color.calendarAssignmentDecorator);

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
                                            final TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.view_calendar_event, assignmentLayout, false);
                                            final CharSequence text = Html.fromHtml("&#8226; ") + e.getName() + "\n";
                                            final SpannableString notClickedString = new SpannableString(text);
                                            notClickedString.setSpan(new URLSpan(""), 2, notClickedString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
                                            final SpannableString clickedString = new SpannableString(notClickedString);
                                            clickedString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 2, notClickedString.length(),
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
                                                    final Dialog dialog = createEventDetailDialog(e);
                                                    dialog.show();
                                                    api.getCalendarEvent(e.getId(), e.getType(), new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
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
                                            final TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.view_calendar_event, eventLayout, false);
                                            final CharSequence text = Html.fromHtml("&#8226; ") + e.getName() + "\n";
                                            final SpannableString notClickedString = new SpannableString(text);
                                            notClickedString.setSpan(new URLSpan(""), 2, notClickedString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            tv.setText(notClickedString, TextView.BufferType.SPANNABLE);
                                            final SpannableString clickedString = new SpannableString(notClickedString);
                                            clickedString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 2, notClickedString.length(),
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

                                                    final Dialog dialog = createEventDetailDialog(e);
                                                    dialog.show();
                                                    api.getCalendarEvent(e.getId(), e.getType(), new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
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

    @Override
    public void refresh() {
        requestFinished = false;
        networkFailed = false;
        api.getCalendar(year, month + 1, new Response.Listener<JSONObject>() {  // month is 0 indexed in java calendar
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

    private Dialog createEventDetailDialog(CalendarEvent e) {
        Log.d(TAG, "Creating event details dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(e.getName());
        RelativeLayout dialogLayout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_event_dialog, null, false);
        if (e.getType() == CalendarEvent.EventType.OCCASION) {
            LinearLayout details = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_occasion_details, null, false);
            details.setVisibility(View.INVISIBLE);
            dialogLayout.addView(details);
        }
        else {
            LinearLayout details = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_assignment_event_details, null, false);
            details.setVisibility(View.INVISIBLE);
            dialogLayout.addView(details);
        }
        builder.setView(dialogLayout)
                .setPositiveButton(getString(R.string.calendar_event_dialog_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        return builder.create();
    }

    private void onEventRequestSuccess(JSONObject response, Dialog dialog) {
        Log.i(TAG, "Calendar event request success");
        CalendarEventDetails eventDetails = new CalendarEventDetails(response);
        TextView date = (TextView) dialog.findViewById(R.id.text_event_date);
        date.setText(Html.fromHtml("<b>" + getString(R.string.calendar_event_date) + ": </b>"
                + DateUtil.dateTimeToShortString(eventDetails.getDate())));
        TextView school = (TextView) dialog.findViewById(R.id.text_event_school);
        school.setText(Html.fromHtml("<b>" + getString(R.string.calendar_event_school) + ": </b>"
                + eventDetails.getSchool()));

        if (eventDetails.getType() == CalendarEvent.EventType.ASSIGNMENT) {
            TextView course = (TextView) dialog.findViewById(R.id.text_event_course);
            course.setText(Html.fromHtml("<b>" + getString(R.string.calendar_event_course_name) + ": </b>"
                    + eventDetails.getCourseName()));
            TextView teacher = (TextView) dialog.findViewById(R.id.text_event_teacher);
            String[] teacherNames = eventDetails.getCourseTeacher().split(" ");
            if (teacherNames.length == 1) {
                teacher.setText(Html.fromHtml("<b>" + getString(R.string.calendar_event_teacher) + ": </b>"
                        + eventDetails.getCourseTeacher()));
            }
            else {
                teacherNames[1] = teacherNames[teacherNames.length - 1];
                teacher.setText(Html.fromHtml("<b>" + getString(R.string.calendar_event_teacher) + ": </b>"
                        + teacherNames[1] + ", " + teacherNames[0]));
            }
            TextView section = (TextView) dialog.findViewById(R.id.text_event_section);
            String sectionStr = " - ";
            if (eventDetails.hasPeriod()) {
                sectionStr = "Period " + eventDetails.getCoursePeriod() + sectionStr;
            }
            else {
                sectionStr = eventDetails.getCourseSection() + sectionStr;
            }
            if (eventDetails.getCourseTerm() != ScheduleCourse.Term.YEAR) {
                sectionStr += TermUtil.termToStringAbbr(eventDetails.getCourseTerm()) + " - ";
            }
            sectionStr += eventDetails.getCourseDays() + " - " + eventDetails.getCourseTeacher();
            section.setText(Html.fromHtml("<b>" + getString(R.string.calendar_event_section) + ": </b>"
                    + sectionStr));
            TextView notes = (TextView) dialog.findViewById(R.id.text_event_notes);
            String notesStr;
            if (eventDetails.hasNotes()) {
                notesStr = eventDetails.getNotes();
            }
            else {
                notesStr = "-";
            }
            notes.setText(Html.fromHtml("<b>" + getString(R.string.calendar_event_notes) + ": </b>"
                    + notesStr));
        }

        ProgressBar loading = (ProgressBar) dialog.findViewById(R.id.progress_event_details);
        loading.setVisibility(View.GONE);
        LinearLayout eventDetailsLayout = (LinearLayout) dialog.findViewById(R.id.ll_event_details);
        eventDetailsLayout.setVisibility(View.VISIBLE);
    }

    private void onEventRequestError(VolleyError error, Dialog dialog) {
        // TODO
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

}
