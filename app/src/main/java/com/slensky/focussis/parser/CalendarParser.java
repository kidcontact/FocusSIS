package com.slensky.focussis.parser;

import android.util.Pair;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.slensky.focussis.data.Calendar;
import com.slensky.focussis.data.CalendarEvent;
import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.util.DateUtil;
import com.slensky.focussis.util.JSONUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by slensky on 3/18/18.
 */

public class CalendarParser extends FocusPageParser {
    private static final String TAG = "CalendarParser";

    @Override
    public Calendar parse(String html) {
        Document calendar = Jsoup.parse(html);

        int month = Integer.parseInt(calendar.selectFirst("#monthSelect1 > option[selected]").attr("value"));
        int year = Integer.parseInt(calendar.selectFirst("#yearSelect1 > option[selected]").attr("value"));

        List<CalendarEvent> events = new ArrayList<>();
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
            for (Element e : data.get(1).getElementsByTag("a")) {
                String name = e.text();
                String onclick = e.attr("onclick");

                String id = onclick.substring(onclick.indexOf("_id=") + 4, onclick.indexOf("&year"));
                CalendarEvent.EventType type = onclick.contains("assignment") ? CalendarEvent.EventType.ASSIGNMENT : CalendarEvent.EventType.OCCASION;
                events.add(new CalendarEvent(new DateTime(year, month, day, 0, 0),  id, name, type));
            }

        }

        Pair<List<MarkingPeriod>, List<Integer>> mp = getMarkingPeriods(html);
        return new Calendar(mp.first, mp.second, year, month, events);
    }

}
