package com.slensky.focussis.parser;

import android.util.Log;

import com.slensky.focussis.data.PasswordResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by slensky on 4/5/18.
 */

public class PasswordResponseParser extends FocusPageParser {
    private final static String TAG = "PasswordResponseParser";

    @Override
    public PasswordResponse parse(String html) {
        Document response = Jsoup.parse(html);
        Element table = response.selectFirst(".scroll_contents").child(0);

        boolean success;
        PasswordResponse.Error error;

        switch (table.text()) {
            case "Error: Your current password was incorrect.":
                success = false;
                error = PasswordResponse.Error.CURRENT_PASSWORD_INCORRECT;
                break;
            case "Error: Your new passwords did not match.":
                success = false;
                error = PasswordResponse.Error.PASSWORDS_DONT_MATCH;
                break;
            case "Note: Your new password was saved.":
                success = true;
                error = null;
                break;
            default:
                Log.w(TAG, "Unrecognized result string: " + table.text());
                // if the text is green then the password change was likely successful
                if (table.selectFirst("span") != null && table.selectFirst("span").attr("style").contains("color:#00CC00")) {
                    success = true;
                    error = null;
                }
                else {
                    success = false;
                    error = PasswordResponse.Error.OTHER;
                }
                break;
        }

        return new PasswordResponse(success, error);
    }

}
