package com.slensky.focussis.parser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.slensky.focussis.util.DateUtil;
import com.slensky.focussis.util.JSONUtil;

import java.util.Calendar;

/**
 * Created by slensky on 3/18/18.
 */

public class CalendarParser extends PageParser {
    private static final String TAG = "CalendarParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        Document calendar = Jsoup.parse(html);
        JSONObject json = new JSONObject();

        int month = Integer.parseInt(calendar.selectFirst("#monthSelect1 > option[selected]").attr("value"));
        int year = Integer.parseInt(calendar.selectFirst("#yearSelect1 > option[selected]").attr("value"));

        JSONObject events = new JSONObject();
        Elements td = calendar.select("div.scroll_contents > table > tbody > tr > td > table > tbody");

        for(int i = 0; i < td.size(); i++) { // skip header cells
            Element d = td.get(i);
            if (d.text().trim().isEmpty()) {
                continue;
            }

            Elements data = d.children();
            if (data.get(1).text().trim().isEmpty()) {
                continue;
            }

            int day = Integer.parseInt(data.get(0).text());
            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, day);
            String date = DateUtil.ISO_DATE_FORMATTER.format(c.getTime());

            for (Element e : data.get(1).getElementsByTag("a")) {
                JSONObject event = new JSONObject();
                event.put("name", e.text());
                String onclick = e.attr("onclick");

                event.put("id", onclick.substring(onclick.indexOf("_id=") + 4, onclick.indexOf("&year")));
                event.put("type", onclick.contains("assignment") ? "assignment" : "occasion");
                event.put("date", date);
                events.put(event.getString("id"), event);

            }

        }

        json.put("month", month);
        json.put("year", year);
        if (events.keys().hasNext()) {
            json.put("events", events);
        }

        return JSONUtil.concatJson(json, this.getMarkingPeriods(html));
    }

}
