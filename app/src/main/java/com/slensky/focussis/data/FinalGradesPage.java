package com.slensky.focussis.data;

import com.slensky.focussis.data.domains.SchoolDomain;

import java.util.List;

/**
 * Created by slensky on 4/1/18.
 */

public class FinalGradesPage extends MarkingPeriodPage {

    private final String studentId;
    private final String hmacSecret;
    private final SchoolDomain schoolDomain;
    private final String currentSemesterName;
    private final String currentSemesterTargetMarkingPeriod;

    // these fields seem to be optional, 6th grade accounts or new student accounts may not have them
    private final String currentSemesterExamsName;
    private final String currentSemesterExamsTargetMarkingPeriod;

    private final String commentCodes;
    private final String gpa;
    private final String weightedGpa;
    private final String creditsEarned;

    public FinalGradesPage(List<MarkingPeriod> markingPeriods, List<Integer> markingPeriodYears, String studentId, String hmacSecret, SchoolDomain schoolDomain, String currentSemesterName, String currentSemesterTargetMarkingPeriod, String currentSemesterExamsName, String currentSemesterExamsTargetMarkingPeriod, String commentCodes, String gpa, String weightedGpa, String creditsEarned) {
        super(markingPeriods, markingPeriodYears);
        this.studentId = studentId;
        this.hmacSecret = hmacSecret;
        this.schoolDomain = schoolDomain;
        this.currentSemesterName = currentSemesterName;
        this.currentSemesterTargetMarkingPeriod = currentSemesterTargetMarkingPeriod;
        this.currentSemesterExamsName = currentSemesterExamsName;
        this.currentSemesterExamsTargetMarkingPeriod = currentSemesterExamsTargetMarkingPeriod;
        this.commentCodes = commentCodes;
        this.gpa = gpa;
        this.weightedGpa = weightedGpa;
        this.creditsEarned = creditsEarned;
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

    public boolean hasCurrentSemesterExams() {
        return currentSemesterExamsName != null;
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

    public String getGpa() {
        return gpa;
    }

    public String getWeightedGpa() {
        return weightedGpa;
    }

    public String getCreditsEarned() {
        return creditsEarned;
    }

    public SchoolDomain getSchoolDomain() {
        return schoolDomain;
    }

}
