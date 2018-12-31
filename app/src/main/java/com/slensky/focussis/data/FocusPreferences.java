package com.slensky.focussis.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by slensky on 4/5/18.
 */

public class FocusPreferences {
    private static final String TAG = "FocusPreferences";

    private boolean englishLanguage;

    public FocusPreferences(boolean englishLanguage) {
        this.englishLanguage = englishLanguage;
    }

    public boolean isEnglishLanguage() {
        return englishLanguage;
    }

    public void setEnglishLanguage(boolean englishLanguage) {
        this.englishLanguage = englishLanguage;
    }

}
