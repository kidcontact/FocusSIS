package org.kidcontact.focussis.parser;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by slensky on 4/5/18.
 */

public class PasswordResponseParser extends PageParser {
    private final static String TAG = "PasswordResponseParser";

    @Override
    public JSONObject parse(String html) throws JSONException {
        JSONObject json = new JSONObject();
        Document response = Jsoup.parse(html);
        Element table = response.selectFirst(".scroll_contents").child(0);

        switch (table.text()) {
            case "Error: Your current password was incorrect.":
                json.put("success", false);
                json.put("error", "current_password_incorrect");
                break;
            case "Error: Your new passwords did not match.":
                json.put("success", false);
                json.put("error", "passwords_dont_match");
                break;
            case "Note: Your new password was saved.":
                json.put("success", true);
                break;
            default:
                Log.w(TAG, "Unrecognized result string: " + table.text());
                // if the text is green then the password change was likely successful
                if (table.selectFirst("span") != null && table.selectFirst("span").attr("style").contains("color:#00CC00")) {
                    json.put("success", true);
                }
                else {
                    json.put("success", false);
                    json.put("error", "other");
                }
                break;
        }

        return json;
    }

}
