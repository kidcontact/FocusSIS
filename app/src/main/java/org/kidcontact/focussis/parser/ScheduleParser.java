package org.kidcontact.focussis.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by slensky on 3/18/18.
 */

public class ScheduleParser extends PageParser {
    private static final String TAG = "ScheduleParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        Document schedule = Jsoup.parse(html);
        JSONObject json = new JSONObject();

        JSONArray courses = new JSONArray();
        int count = 1;
        Element tr = schedule.getElementById("LOy_row" + Integer.toString(count));
        while (tr != null) {
            Elements td = tr.select("td.LO_field");
            JSONObject course = new JSONObject();

            course.put("name", td.get(0).text());
            String[] data = td.get(1).text().split(" - ");
            if (data[0].startsWith("Period")) {
                course.put("period", Integer.parseInt(data[0].substring("Period ".length())));
            }
            course.put("teacher", data[data.length - 1]);
            course.put("days", td.get(2).text());
            course.put("room", td.get(3).text());
            if (td.get(4).text().equals("Full Year")) {
                course.put("term", "year");
            }
            else {
                String[] t = td.get(4).text().split(" ");
                course.put("term", t[0].charAt(0) + t[1]);
            }

            courses.put(course);
            count += 1;
            tr = schedule.getElementById("LOy_row" + Integer.toString(count));
        }

        json.put("courses", courses);
        return this.concatJson(json, this.getMarkingPeriods(html));
    }

}
