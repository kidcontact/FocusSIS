package com.slensky.focussis.data.network.model;

import com.slensky.focussis.data.focus.FocusPreferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by slensky on 4/5/18.
 */

public class PreferencesParser extends FocusPageParser {
    private final static String TAG = "PreferencesParser";

    @Override
    public FocusPreferences parse(String html) {
        Document preferences = Jsoup.parse(html);
        Element englishLanguageInput = preferences.selectFirst("input[name=values[Preferences][LANGUAGE]][value=en_US]");
        return new FocusPreferences(englishLanguageInput.hasAttr("checked"));
    }

}
