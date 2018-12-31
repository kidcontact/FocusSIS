package com.slensky.focussis.parser;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.slensky.focussis.data.CalendarEvent;
import com.slensky.focussis.data.CalendarEventDetails;
import com.slensky.focussis.util.DateUtil;

import java.util.Date;

/**
 * Created by slensky on 3/18/18.
 */

public class CalendarEventParser extends FocusPageParser {
    private static final String TAG = "CalendarEventParser";

    private String id;
    private CalendarEvent.EventType type;

    @Override
    public CalendarEventDetails parse(String html) {
        Document calendarEvent = Jsoup.parse(html);

        Elements tr = calendarEvent.selectFirst("div.scroll_contents").getElementsByTag("tr");

        // if the event exists
        if (!tr.get(0).getElementsByTag("td").get(1).text().replace('\u00A0', ' ').equals("-")) {
            String dateStr = tr.get(0).getElementsByTag("td").get(1).text();
            DateTime date = new DateTime(DateUtil.nattyDateParser.parse(dateStr).get(0).getDates().get(0));
            String title = tr.get(1).getElementsByTag("td").get(1).text();

            int start = type.equals(CalendarEvent.EventType.ASSIGNMENT) ? 5 : 2;
            String school = tr.get(start).getElementsByTag("td").get(1).text().replace('\u00A0', ' ').trim();
            String notes = tr.get(start + 1).getElementsByTag("td").get(1).text().replace('\u00A0', ' ').trim();
            if (notes.trim().equals("-")) {
                notes = null;
            }

            // legacy code to detect if event is assignment
            // if (tr.get(2).getElementsByTag("td").get(0).text().equals("Teacher")) {
            if (type.equals(CalendarEvent.EventType.ASSIGNMENT)) {
                String courseName = tr.get(3).getElementsByTag("td").get(1).text();
                String courseInformationStr = tr.get(4).getElementsByTag("td").get(1).text();
                HyphenatedCourseInformation courseInformation = parseHyphenatedCourseInformation(courseInformationStr, false, true, true);
                return new CalendarEventDetails(date, id, school, title, type, notes, courseInformation.getMeetingDays(), courseName, courseInformation.getPeriod(), courseInformation.getSection(), courseInformation.getTeacher(), courseInformation.getTerm());
            }

            // event is occasion
            return new CalendarEventDetails(date, id, school, title, type, notes, null, null, null, null, null, null);

        }

        throw new FocusParseException("Calendar event could not be found " + tr.get(0).text());
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(CalendarEvent.EventType type) {
        this.type = type;
    }
}
