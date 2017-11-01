package org.kidcontact.focussis.util;

import org.joda.time.DateTime;
import org.kidcontact.focussis.data.Course;

import java.util.List;

/**
 * Created by slensky on 4/3/17.
 */

public class DateUtil {

    public static String dateTimeToShortString(DateTime dt) {
        return dt.dayOfWeek().getAsShortText() + ". " + dt.monthOfYear().getAsText() + " " + dt.dayOfMonth().getAsText();
    }

    public static String dateTimeToLongString(DateTime dt) {
        return dt.monthOfYear().getAsText() + " " + dt.dayOfMonth().getAsText() + ", " + dt.year().getAsText();
    }

}
