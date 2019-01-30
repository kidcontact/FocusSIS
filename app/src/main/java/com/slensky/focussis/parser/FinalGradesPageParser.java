package com.slensky.focussis.parser;

import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.slensky.focussis.data.FinalGradesPage;
import com.slensky.focussis.data.MarkingPeriod;
import com.slensky.focussis.data.domains.SchoolDomain;
import com.slensky.focussis.util.GsonSingleton;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesPageParser extends FocusPageParser {
    private static final String TAG = "FinalGradesPageParser";

    @Override
    public FinalGradesPage parse(String html) {
        Document finalGrades = Jsoup.parse(html);

        Pattern r = Pattern.compile("Focus\\.API\\.allowAccess\\(\\s+'(.+?)',\\s+'(.+?)',\\s+'(.+?)',\\s+null\\s+\\|\\|\\s+\"(.+?)\"\\s+\\);");
        Matcher m = r.matcher(html);
        if (!m.find()) {
            throw new FocusParseException("Could not match student id/hmac secret in html");
        }

        String studentId = m.group(2);
        String hmacSecret = m.group(3);

        // pattern does not compile without "redundant" escape
        //noinspection RegExpRedundantEscape
        r = Pattern.compile("school_list\\s*=\\s*null\\s*\\|\\|\\s*(\\{.+?\\})\\s*,");
        m = r.matcher(html);
        if (!m.find()) {
            throw new FocusParseException("Could not match school_list in html");
        }

        String schoolListStr = m.group(1);
        Map<String, SchoolDomain.School> schoolDomainMembers = new HashMap<>();
        try {
            JSONObject schoolListJson = new JSONObject(schoolListStr);
            Iterator<String> keys = schoolListJson.keys();

            while(keys.hasNext()) {
                String key = keys.next();
                schoolDomainMembers.put(key, new SchoolDomain.School(key, schoolListJson.getString(key)));
            }
        } catch (JSONException e) {
            throw new FocusParseException("JSONException while parsing school list from html " + schoolListStr, e);
        }
        SchoolDomain schoolDomain = new SchoolDomain(schoolDomainMembers);

        Element ul = finalGrades.getElementById("fg_tabs_ul");
        String currentSemesterName = null;
        String currentSemesterTargetMarkingPeriod = null;
        String currentSemesterExamsName = null;
        String currentSemesterExamsTargetMarkingPeriod = null;
        for (Element li : ul.children()) {
            if ((li.text().contains("Semester") || li.text().contains("Quarter")) && !li.text().contains("All")) {
                if (li.text().contains("Exams")) {
                    currentSemesterExamsName = li.text().trim();
                    currentSemesterExamsTargetMarkingPeriod = li.attr("data-target-mp");
                }
                else {
                    currentSemesterName = li.text().trim();
                    currentSemesterTargetMarkingPeriod = li.attr("data-target-mp");
                }
            }
        }
        if (currentSemesterName == null
                || currentSemesterTargetMarkingPeriod == null
                || currentSemesterExamsName == null
                || currentSemesterExamsTargetMarkingPeriod == null) {
            Log.w(TAG, String.format("Not all title/raw marking periods found! (current exams not being found is ok)" +
                    "\n  currentSemesterName: %s" +
                    "\n  currentSemesterTargetMarkingPeriod: %s" +
                    "\n  currentSemesterExamsName: %s" +
                    "\n  currentSemesterExamsTargetMarkingPerioid: %s",
                    currentSemesterName == null ? "null" : currentSemesterName,
                    currentSemesterTargetMarkingPeriod == null ? "null" : currentSemesterTargetMarkingPeriod,
                    currentSemesterExamsName == null ? "null" : currentSemesterExamsName,
                    currentSemesterExamsTargetMarkingPeriod == null ? "null" : currentSemesterExamsTargetMarkingPeriod));
        }

        Element div = finalGrades.getElementById("comment_codes");
        div.select("br").append("\\n");
        String commentCodes = div.text().replace(" \\n", "\\n").replace("\\n", "\n").trim();

        Element content = finalGrades.getElementById("content_-1");
        Elements statsHeaderFields = content.select(".stats_header_field");
        String gpa = statsHeaderFields.get(0).text();
        String weightedGpa = statsHeaderFields.get(1).text();
        String creditsEarned = statsHeaderFields.get(2).text();

        Pair<List<MarkingPeriod>, List<Integer>> mp = getMarkingPeriods(html);
        return new FinalGradesPage(mp.first, mp.second, studentId, hmacSecret, schoolDomain, currentSemesterName, currentSemesterTargetMarkingPeriod, currentSemesterExamsName, currentSemesterExamsTargetMarkingPeriod, commentCodes, gpa, weightedGpa, creditsEarned);
    }

}
