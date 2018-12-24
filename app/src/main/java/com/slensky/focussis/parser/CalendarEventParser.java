package com.slensky.focussis.parser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.slensky.focussis.util.DateUtil;

import java.util.Date;

/**
 * Created by slensky on 3/18/18.
 */

public class CalendarEventParser extends FocusPageParser {
    private static final String TAG = "CalendarEventParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        Document calendarEvent = Jsoup.parse(html);
        JSONObject json = new JSONObject();

        Elements tr = calendarEvent.selectFirst("div.scroll_contents").getElementsByTag("tr");

        // if the event exists
        if (!tr.get(0).getElementsByTag("td").get(1).text().replace('\u00A0', ' ').equals("-")) {
            int start = 0;
            Date date = DateUtil.nattyDateParser.parse(tr.get(0).getElementsByTag("td").get(1).text()).get(0).getDates().get(0);
            json.put("date", DateUtil.ISO_DATE_FORMATTER.format(date));
            json.put("title", tr.get(1).getElementsByTag("td").get(1).text());

            // event is assignment
            if (tr.get(2).getElementsByTag("td").get(0).text().equals("Teacher")) {
                json.put("type", "assignment");
                JSONObject course = new JSONObject();

                course.put("name", tr.get(3).getElementsByTag("td").get(1).text());
                String[] data = tr.get(4).getElementsByTag("td").get(1).text().split(" - ");
                if (data[0].startsWith("Period ")) {
                    course.put("period", data[0].substring("Period ".length()));
                }
                else {
                    course.put("section", data[0]);
                }

                if (data.length == 4) {
                    course.put("days", data[1]);
                    course.put("teacher", data[3]);
                }
                else {
                    course.put("term", data[1].toLowerCase());
                    course.put("days", data[2]);
                    course.put("teacher", data[4]);
                }
                json.put("course", course);
                start = 5;
            }
            else {
                json.put("type", "occasion");
                start = 2;
            }

            json.put("school", tr.get(start).getElementsByTag("td").get(1).text().replace('\u00A0', ' ').trim());
            String notes = tr.get(start + 1).getElementsByTag("td").get(1).text().replace('\u00A0', ' ').trim();
            if (!notes.equals("-")) {
                json.put("notes", notes);
            }
        }

        return json;
    }

}
