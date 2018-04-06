package com.slensky.focussis.util;

import com.joestelmach.natty.Parser;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by slensky on 4/3/17.
 */

public class DateUtil {

    public static final DateFormat ISO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    public static Parser nattyDateParser = new Parser();

    public static String dateTimeToShortString(DateTime dt) {
        return dt.dayOfWeek().getAsShortText() + ". " + dt.monthOfYear().getAsText() + " " + dt.dayOfMonth().getAsText();
    }

    public static String dateTimeToLongString(DateTime dt) {
        return dt.monthOfYear().getAsText() + " " + dt.dayOfMonth().getAsText() + ", " + dt.year().getAsText();
    }

}
