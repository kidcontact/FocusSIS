package org.kidcontact.focussis.data;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 5/23/17.
 */

public class Referrals extends MarkingPeriodPage {

    private static final String TAG = "Referrals";
    private final List<Referral> referrals;

    public Referrals(JSONObject referralsJSON) {
        super(referralsJSON);

        referrals = new ArrayList<>();
        try {
            referralsJSON = referralsJSON.getJSONObject("referrals");
            Iterator<?> keys = referralsJSON.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject referralJSON = referralsJSON.getJSONObject(key);

                try {
                    DateTime creationDate = new DateTime(referralJSON.getString("creation_date"));
                    DateTime entryDate = new DateTime(referralJSON.getString("entry_date"));
                    DateTime lastUpdated = new DateTime(referralJSON.getString("last_updated"));
                    boolean display = referralJSON.getBoolean("display");
                    int grade = referralJSON.getInt("grade");
                    String id = referralJSON.getString("id");
                    String name = referralJSON.getString("name");
                    boolean notificationSent = referralJSON.getBoolean("notification_sent");
                    boolean processed = referralJSON.getBoolean("processed");
                    String school = referralJSON.getString("school");
                    int schoolYear = referralJSON.getInt("school_year");
                    String teacher = referralJSON.getString("teacher");
                    String violation = null;
                    if (referralJSON.has("violation")) {
                        violation = referralJSON.getString("violation");
                    }
                    String otherViolation = null;
                    if (referralJSON.has("other_violation")) {
                        otherViolation = referralJSON.getString("other_violation");
                    }
                    referrals.add(new Referral(creationDate, entryDate, lastUpdated, display, grade, id, name, notificationSent, processed, school, schoolYear, teacher, violation, otherViolation));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "error parsing referral\n" + referralJSON.toString());
                }

            }
        } catch (JSONException e) {
            Log.e(TAG, "referrals not found in JSON");
        }

    }

    public List<Referral> getReferrals() {
        return referrals;
    }

}
