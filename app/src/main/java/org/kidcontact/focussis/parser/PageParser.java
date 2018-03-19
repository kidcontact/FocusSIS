package org.kidcontact.focussis.parser;

import com.joestelmach.natty.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;

/**
 * Created by slensky on 3/12/18.
 */

public abstract class PageParser {

    public abstract JSONObject parse(String html) throws JSONException;

    protected JSONObject getMarkingPeriods(String html) throws JSONException {
        Document page = Jsoup.parse(html);
        JSONObject json = new JSONObject();

        Elements years = page.getElementsByAttributeValue("name", "side_syear").first().children();
        JSONArray availableYears = new JSONArray();
        int selectedYear = -1;
        for (int i = 0; i < years.size(); i++) {
            if (years.get(i).hasAttr("selected")) {
                selectedYear = Integer.parseInt(years.attr("value"));
            }
            availableYears.put(Integer.parseInt(years.attr("value")));
        }

        Elements mps = page.getElementsByAttributeValue("name", "side_mp").first().children();
        JSONObject markingPeriods = new JSONObject();
        for (Element mp : mps) {
            JSONObject mpd = new JSONObject();
            mpd.put("id", mp.attr("value"));
            mpd.put("name", mp.text());
            mpd.put("year", selectedYear);
            if (mp.hasAttr("selected")) {
                mpd.put("selected", true);
            }
            markingPeriods.put(mp.attr("value"), mpd);
        }

        json.put("mps", markingPeriods);
        json.put("mp_years", availableYears);

        return json;
    }

    protected JSONObject concatJson(JSONObject json1, JSONObject json2) throws JSONException {
        Iterator it = json2.keys();
        while (it.hasNext()) {
            String key = (String)it.next();
            json1.put(key, json2.get(key));
        }
        return json1;
    }

}
