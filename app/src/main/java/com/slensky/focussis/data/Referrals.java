package com.slensky.focussis.data;

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

    public Referrals(List<MarkingPeriod> markingPeriods, List<Integer> markingPeriodYears, List<Referral> referrals) {
        super(markingPeriods, markingPeriodYears);
        this.referrals = referrals;
    }

    public List<Referral> getReferrals() {
        return referrals;
    }

}
