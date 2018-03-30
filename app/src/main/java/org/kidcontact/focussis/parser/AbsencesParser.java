package org.kidcontact.focussis.parser;

import android.util.Log;

import com.joestelmach.natty.DateGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kidcontact.focussis.util.DateUtil;
import org.kidcontact.focussis.util.JSONUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slensky on 3/29/18.
 */

public class AbsencesParser extends PageParser {
    private static final String TAG = "AbsencesParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        JSONObject json = new JSONObject();
        Document absences = Jsoup.parse(html);

        Element table = absences.selectFirst("td.WhiteDrawHeader");
        Pattern r = Pattern.compile("Absent: ([0-9]+) periods \\(during ([0-9]+) days\\) A Absent: ([0-9]+) periods " +
                "E Excused Absence: ([0-9]+) periods -- ([0-9]+) days Other Marks: ([0-9]+) periods \\(during ([0-9]+) days\\) " +
                "L Late: ([0-9]+) periods T Tardy: ([0-9]+) periods M Misc. Activity: ([0-9]+) periods O Off Site\\/Field Trip: ([0-9]+) periods");
        Matcher m = r.matcher(table.text());
        if (m.find()) {
            json.put("periods_absent", Integer.parseInt(m.group(1)));
            json.put("days_partially_absent", Integer.parseInt(m.group(2)));
            json.put("periods_absent_unexcused", Integer.parseInt(m.group(3)));
            json.put("periods_absent_excused", Integer.parseInt(m.group(4)));
            json.put("days_absent_excused", Integer.parseInt(m.group(5)));
            json.put("periods_other_marks", Integer.parseInt(m.group(6)));
            json.put("days_other_marks", Integer.parseInt(m.group(7)));
            json.put("periods_late", Integer.parseInt(m.group(8)));
            json.put("periods_tardy", Integer.parseInt(m.group(9)));
            json.put("periods_misc", Integer.parseInt(m.group(10)));
            json.put("periods_offsite", Integer.parseInt(m.group(11)));
        }
        else {
            Log.e(TAG, "Regex failed to match table text: " + table.text());
        }

        int start, end;
        String key1 = "Total Full Days Possible: ";
        String key2 = "Total Full Days Attended: ";
        String key3 = "Total Full Days Absent: ";
        String key4 = "Enrollment Dates: ";

        start = absences.text().indexOf(key1) + key1.length();
        end = start + absences.text().substring(start).indexOf(key2);
        json.put("days_possible", Float.parseFloat(absences.text().substring(start, end)));

        start = absences.text().indexOf(key2) + key2.length();
        end = start + absences.text().substring(start).indexOf(key3);
        String attended = absences.text().substring(start, end);
        json.put("days_attended", Float.parseFloat(attended.substring(0, attended.indexOf(" "))));
        json.put("days_attended_percent", Math.round(Float.parseFloat(attended.substring(attended.indexOf("(") + 1, attended.indexOf("%"))) * 100) / 100.0);

        start = absences.text().indexOf(key3) + key3.length();
        end = start + absences.text().substring(start).indexOf(key4);
        String absent = absences.text().substring(start, end);
        json.put("days_absent", Float.parseFloat(absent.substring(0, absent.indexOf(" "))));
        json.put("days_absent_percent", Math.round(Float.parseFloat(absent.substring(absent.indexOf("(") + 1, absent.indexOf("%"))) * 100) / 100.0);

        Elements headers = absences.select("td.LO_header");
        List<String> periodNames = new ArrayList<>();
        for (int i = 2; i < headers.size(); i++) {
            if (headers.get(i).parent().parent().tagName().equals("thead")) {
                periodNames.add(headers.get(i).text().toLowerCase());
            }
            else {
                break;
            }
        }

        JSONObject missed = new JSONObject();
        int count = 1;
        Element tr = absences.getElementById("LOy_row" + Integer.toString(count));
        while (tr != null) {
            Elements fields = tr.select("td.LO_field");

            if (!fields.isEmpty()) {
                JSONObject a = new JSONObject();
                List<DateGroup> groups = DateUtil.nattyDateParser.parse(fields.get(0).text());
                String date = DateUtil.ISO_DATE_FORMATTER.format(groups.get(0).getDates().get(0));
                a.put("date", date);
                a.put("status", fields.get(1).text().toLowerCase().split(" ")[0]);

                JSONObject periods = new JSONObject();
                for (int i = 0; i < periodNames.size(); i++) {
                    Element p = fields.get(i + 2);
                    String n = periodNames.get(i);
                    JSONObject c = new JSONObject();

                    try {
                        c.put("period", Integer.parseInt(n));
                    } catch (NumberFormatException e) {
                        c.put("period", n);
                    }

                    Element tooltip = p.selectFirst("div");
                    if (tooltip != null) {
                        String[] data = tooltip.attr("data-tooltip").split("<BR>");
                        String[] courseInfo = data[0].split(" - ");
                        c.put("name", courseInfo[0]);
                        c.put("days", courseInfo[2]);

                        // focus doesn't account for middle names properly in this page, so teachers without
                        // middle names have two spaces in between their first and last!
                        c.put("teacher", courseInfo[courseInfo.length - 1].replace("  ", " "));

                        groups = DateUtil.nattyDateParser.parse(data[1].substring("Last Modified: ".length()));
                        String lastUpdated = DateUtil.ISO_DATE_FORMATTER.format(groups.get(0).getDates().get(0));
                        c.put("last_updated", lastUpdated);
                        String[] name = data[2].trim().split(", ");
                        c.put("last_updated_by", name[1] + ' ' + name[0]);
                    }

                    String s = p.text().trim().toLowerCase();
                    if (s.isEmpty()) {
                        continue; //c.put("status", "unset");
                    }
                    else if (s.equals("-")) {
                        continue; // possibly inserted for periods that don't happen that day?
                    }
                    else if (s.equals("a")) {
                        c.put("status", "absent");
                    }
                    else if (s.equals("e")) {
                        c.put("status", "excused");
                    }
                    else if (s.equals("l")) {
                        c.put("status", "late");
                    }
                    else if (s.equals("t")) {
                        c.put("status", "tardy");
                    }
                    else if (s.equals("o")) {
                        c.put("status", "offsite");
                    }
                    else {
                        c.put("status", "misc");
                    }

                    periods.put(n, c);
                }
                a.put("periods", periods);
                missed.put(a.getString("date"), a);
            }

            count += 1;
            tr = absences.getElementById("LOy_row" + Integer.toString(count));
        }
        json.put("absences", missed);

        return JSONUtil.concatJson(json, this.getMarkingPeriods(html));
    }

}
