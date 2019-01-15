package com.slensky.focussis.data.focus;

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
