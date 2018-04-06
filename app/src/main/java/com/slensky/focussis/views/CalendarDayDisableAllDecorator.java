package com.slensky.focussis.views;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

/**
 * Created by slensky on 5/8/17.
 */

public class CalendarDayDisableAllDecorator implements DayViewDecorator {

    int month;

    public CalendarDayDisableAllDecorator(int month) {
        this.month = month;
    }

    @Override
    public boolean shouldDecorate(CalendarDay calendarDay) {
        return calendarDay.getMonth() != month;
    }

    @Override
    public void decorate(DayViewFacade dayViewFacade) {
        dayViewFacade.setDaysDisabled(true);
    }
}
