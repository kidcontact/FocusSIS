package com.slensky.focussis.views;

import com.slensky.focussis.data.CalendarEvent;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotsSpan;

import java.util.ArrayDeque;

/**
 * Created by slensky on 5/8/17.
 */

public class CalendarDayDecorator implements DayViewDecorator {

    private int day;
    private int month;
    private int year;
    private int[] colors;
    private boolean firstRun;

    public CalendarDayDecorator(int day, int month, int year, ArrayDeque<CalendarEvent> events, int occasionColor, int assignmentColor, boolean firstRun) {
        this.day = day;
        this.month = month;
        this.year = year;
        colors = new int[events.size()];
        int i = 0;
        for (CalendarEvent e : events) {
            if (e.getType() == CalendarEvent.EventType.OCCASION) {
                colors[i] = occasionColor;
            }
            else {
                colors[i] = assignmentColor;
            }
            i += 1;
        }
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return this.day == day.getDay() && month == day.getMonth() && year == day.getYear();
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (!firstRun) {
            view.setDaysDisabled(true);
        }
        view.addSpan(new DotsSpan(5, 3, colors));
    }

}
