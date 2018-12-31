package com.slensky.focussis.fragments;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.slensky.focussis.R;
import com.slensky.focussis.data.Schedule;
import com.slensky.focussis.data.ScheduleCourse;
import com.slensky.focussis.util.GsonSingleton;
import com.slensky.focussis.util.TableRowAnimationController;
import com.slensky.focussis.util.TermUtil;

import java.util.List;

/**
 * Created by slensky on 5/9/18.
 */

public class ScheduleCoursesTabFragment extends Fragment {
    private static final String TAG = "ScheduleCoursesTabFragment";
    private Schedule schedule;

    public ScheduleCoursesTabFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = GsonSingleton.getInstance();
        schedule = gson.fromJson(getArguments().getString(getString(R.string.EXTRA_SCHEDULE)), Schedule.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_courses_tab, container, false);
        TextView header = (TextView) view.findViewById(com.slensky.focussis.R.id.text_schedule_header);
        header.setText(schedule.getCurrentMarkingPeriod().getName());

        TableLayout table = (TableLayout) view.findViewById(com.slensky.focussis.R.id.table_courses);
        table.removeAllViews();
        TableRow headerRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_schedule_course_header, table, false);
        table.addView(headerRow);

        List<ScheduleCourse> courses = schedule.getCourses();
        TableRowAnimationController animationController = new TableRowAnimationController(getContext());
        for (ScheduleCourse c : courses) {
            final TableRow courseRow = (TableRow) inflater.inflate(com.slensky.focussis.R.layout.view_schedule_course, table, false);
            TextView name = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_name);
            name.setText(c.getName());
            TextView period = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_period);
            String periodStr;
            if (c.getPeriod() == null) {
                periodStr = "-";
            }
            else if (c.isAdvisory()) {
                periodStr = "Advisory";
            }
            else {
                periodStr = "Period " + c.getPeriod();
            }
            period.setText(periodStr);
            TextView teacher = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_teacher);
            teacher.setText(c.getTeacher());
            TextView days = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_days);
            days.setText(c.getDays());
            TextView room = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_room);
            room.setText(c.getRoom().split(" ")[0]); // changes jr/sr area into just jr/sr for brevity
            TextView term = (TextView) courseRow.findViewById(com.slensky.focussis.R.id.text_course_term);
            term.setText(TermUtil.termToString(c.getTerm()));

            final View divider = inflater.inflate(R.layout.view_divider, table, false);

            Animation animation = animationController.nextAnimation();
            courseRow.setAnimation(animation);
            divider.setAnimation(animation);

            divider.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            courseRow.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (divider.getLayerType() != View.LAYER_TYPE_NONE) {
                        divider.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                    if (courseRow.getLayerType() != View.LAYER_TYPE_NONE) {
                        courseRow.setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            table.addView(divider);
            table.addView(courseRow);
        }

        if (schedule.getCourses().size() == 0) {
            table.addView(inflater.inflate(R.layout.view_no_records_row, table, false));
        }

        return view;
    }
}
