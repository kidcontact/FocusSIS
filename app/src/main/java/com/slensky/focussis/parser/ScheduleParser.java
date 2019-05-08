package com.slensky.focussis.parser;

import android.util.Pair;

import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.data.Schedule;
import com.slensky.focussis.data.ScheduleCourse;
import com.slensky.focussis.util.JSONUtil;
import com.slensky.focussis.util.TermUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 3/18/18.
 */

public class ScheduleParser extends FocusPageParser {
    private static final String TAG = "ScheduleParser";

    @Override
    public Schedule parse(String html) {
        Document schedule = Jsoup.parse(html);

        List<ScheduleCourse> courses = new ArrayList<>();
        int count = 1;
        Element trLeft = schedule.getElementById("LOy_fixed_left_row" + Integer.toString(count));
        Element tr = schedule.getElementById("LOy_row" + Integer.toString(count));
        while (tr != null) {
            Elements tdLeft = trLeft.select("td.LO_field");
            Elements td = tr.select("td.LO_field");
            JSONObject course = new JSONObject();

            String name = tdLeft.get(0).text().trim();
            HyphenatedCourseInformation courseInformation = parseHyphenatedCourseInformation(tdLeft.get(1).text(), false, false);
            String days = td.get(2).text();
            String room = td.get(3).text();

            String termStr = td.get(4).text().trim().toLowerCase();
            if (termStr.equals("full year") || termStr.isEmpty()) {
                termStr = "year";
            }
            else {
                String tmpTerm = Character.toString(termStr.charAt(0));
                if (termStr.length() > 1) {
                    for (int i = 1; i < termStr.length(); i++) {
                        if (Character.isDigit(termStr.charAt(i))) {
                            tmpTerm += termStr.charAt(i);
                            break;
                        }
                    }
                }
                termStr = tmpTerm;
            }
            ScheduleCourse.Term term = TermUtil.stringToTerm(termStr);

            courses.add(new ScheduleCourse(days, name, courseInformation.getPeriod(), room, courseInformation.getTeacher(), term));
            count += 1;
            trLeft = schedule.getElementById("LOy_fixed_left_row" + Integer.toString(count));
            tr = schedule.getElementById("LOy_row" + Integer.toString(count));
        }

        Pair<List<MarkingPeriod>, List<Integer>> mp = getMarkingPeriods(html);
        return new Schedule(mp.first, mp.second, courses);
    }

}
