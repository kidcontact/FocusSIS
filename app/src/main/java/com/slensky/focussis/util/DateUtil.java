package com.slensky.focussis.util;

import com.joestelmach.natty.Parser;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by slensky on 4/3/17.
 */

public class DateUtil {

    public static final DateFormat ISO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    public static Parser nattyDateParser = new Parser();

    public static DateTimeFormatter shortStringDateFormatter = DateTimeFormat.forPattern("EEE. MMMM d");
    public static DateTimeFormatter mmddyyFormatter = DateTimeFormat.forPattern("MM/dd/yy");

    public static String dateTimeToShortString(DateTime dt) {
        return shortStringDateFormatter.print(dt);
    }

    public static String dateTimeToLongString(DateTime dt) {
        return dt.monthOfYear().getAsText() + " " + dt.dayOfMonth().getAsText() + ", " + dt.year().getAsText();
    }

}
