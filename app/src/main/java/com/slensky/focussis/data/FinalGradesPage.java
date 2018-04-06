package com.slensky.focussis.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesPage extends MarkingPeriodPage {

    private final String studentId;
    private final String hmacSecret;
    private final String currentSemesterName;
    private final String currentSemesterExamsName;
    private final String currentSemesterTargetMarkingPeriod;
    private final String currentSemesterExamsTargetMarkingPeriod;
    private final String commentCodes;
    private final String gpa;
    private final String weightedGpa;
    private final String creditsEarned;
    private final JSONObject json;

    public FinalGradesPage(JSONObject finalGradesPage) throws JSONException {
        super(finalGradesPage);
        this.json = finalGradesPage;
        this.studentId = finalGradesPage.getString("student_id");
        this.hmacSecret = finalGradesPage.getString("hmac_secret");
        this.currentSemesterName = finalGradesPage.getString("current_sem_name");
        this.currentSemesterExamsName = finalGradesPage.getString("current_sem_exams_name");
        this.currentSemesterTargetMarkingPeriod = finalGradesPage.getString("current_sem_target_mp");
        this.currentSemesterExamsTargetMarkingPeriod = finalGradesPage.getString("current_sem_exams_target_mp");
        this.commentCodes = finalGradesPage.getString("comment_codes");
        this.gpa = finalGradesPage.getString("gpa");
        this.weightedGpa = finalGradesPage.getString("weighted_gpa");
        this.creditsEarned = finalGradesPage.getString("credits_earned");
    }

    public String getStudentId() {
        return studentId;
    }

    public String getHmacSecret() {
        return hmacSecret;
    }

    public String getCurrentSemesterName() {
        return currentSemesterName;
    }

    public String getCurrentSemesterExamsName() {
        return currentSemesterExamsName;
    }

    public String getCurrentSemesterTargetMarkingPeriod() {
        return currentSemesterTargetMarkingPeriod;
    }

    public String getCurrentSemesterExamsTargetMarkingPeriod() {
        return currentSemesterExamsTargetMarkingPeriod;
    }

    public String getCommentCodes() {
        return commentCodes;
    }

    public JSONObject getJson() {
        return json;
    }

}
