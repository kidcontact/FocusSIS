package com.slensky.focussis.data;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slensky on 4/3/18.
 */

public class FinalGrades {
    private static final String TAG = "FinalGrades";

    private FinalGradesPage finalGradesPage;
    private final List<FinalGrade> finalGrades;

    public FinalGrades(List<FinalGrade> finalGrades) {
        this.finalGrades = finalGrades;
    }

    public FinalGradesPage getFinalGradesPage() {
        return finalGradesPage;
    }

    public void setFinalGradesPage(FinalGradesPage finalGradesPage) {
        this.finalGradesPage = finalGradesPage;
    }

    public List<FinalGrade> getFinalGrades() {
        return finalGrades;
    }

}
