package org.kidcontact.focussis.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 4/28/17.
 */

public abstract class MarkingPeriodPage {

    private static final String TAG = MarkingPeriodPage.class.getName();
    private final List<MarkingPeriod> markingPeriods;
    private final List<Integer> markingPeriodYears;

    public MarkingPeriodPage(JSONObject page) {
        List<MarkingPeriod> markingPeriods;
        List<Integer> markingPeriodYears;

        try {
            JSONObject mpsJSON = page.getJSONObject("mps");
            Iterator<String> mpIterator = mpsJSON.keys();
            markingPeriods = new ArrayList<>();

            while (mpIterator.hasNext()) {
                JSONObject mpJSON = mpsJSON.getJSONObject(mpIterator.next());
                markingPeriods.add(new MarkingPeriod(mpJSON.getString("id"), mpJSON.getInt("year"), mpJSON.has("selected"), mpJSON.getString("name")));
            }
        } catch (JSONException e) {
            markingPeriods = null;
            Log.e(TAG, "Error parsing marking period JSON");
        }

        try {
            JSONArray yearJSON = page.getJSONArray("mp_years");
            markingPeriodYears = new ArrayList<>();

            for (int i = 0; i < yearJSON.length(); i++) {
                markingPeriodYears.add(yearJSON.getInt(i));
            }
        } catch (JSONException e) {
            markingPeriodYears = null;
            Log.e(TAG, "Error parsing marking period years JSON");
        }

        this.markingPeriods = markingPeriods;
        this.markingPeriodYears = markingPeriodYears;
    }

    public List<MarkingPeriod> getMarkingPeriods() {
        return markingPeriods;
    }

    public List<Integer> getMarkingPeriodYears() {
        return markingPeriodYears;
    }

    public MarkingPeriod getCurrentMarkingPeriod() {
        for (MarkingPeriod mp : markingPeriods) {
            if (mp.isSelected()) {
                return mp;
            }
        }
        return null;
    }

}
