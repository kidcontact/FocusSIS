package com.slensky.focussis.parser;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.slensky.focussis.util.JSONUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesPageParser extends FocusPageParser {
    private static final String TAG = "FinalGradesPageParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        JSONObject json = new JSONObject();
        Document finalGrades = Jsoup.parse(html);

        Pattern r = Pattern.compile("Focus\\.API\\.allowAccess\\(\\s+'(.+?)',\\s+'(.+?)',\\s+'(.+?)',\\s+null\\s+\\|\\|\\s+\"(.+?)\"\\s+\\);");
        Matcher m = r.matcher(html);
        if (m.find()) {
            json.put("student_id", m.group(2));
            json.put("hmac_secret", m.group(3));
        }

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
        json.put("current_sem_name", currentSemesterName);
        json.put("current_sem_target_mp", currentSemesterTargetMarkingPeriod);
        json.put("current_sem_exams_name", currentSemesterExamsName);
        json.put("current_sem_exams_target_mp", currentSemesterExamsTargetMarkingPeriod);

        Element div = finalGrades.getElementById("comment_codes");
        div.select("br").append("\\n");
        String commentCodes = div.text().replace(" \\n", "\\n").replace("\\n", "\n").trim();
        json.put("comment_codes", commentCodes);

        Element content = finalGrades.getElementById("content_-1");
        Elements statsHeaderFields = content.select(".stats_header_field");
        json.put("gpa", statsHeaderFields.get(0).text());
        json.put("weighted_gpa", statsHeaderFields.get(1).text());
        json.put("credits_earned", statsHeaderFields.get(2).text());

        return JSONUtil.concatJson(json, this.getMarkingPeriods(html));
    }

}
