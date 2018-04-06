package com.slensky.focussis.parser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by slensky on 4/5/18.
 */

public class PreferencesParser extends PageParser {
    private final static String TAG = "PreferencesParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        JSONObject json = new JSONObject();
        Document preferences = Jsoup.parse(html);
        Element englishLanguageInput = preferences.selectFirst("input[name=values[Preferences][LANGUAGE]][value=en_US]");
        json.put("english_language", englishLanguageInput.hasAttr("checked"));
        return json;
    }

}
