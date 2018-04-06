package com.slensky.focussis.data;

/**
 * Created by slensky on 4/16/17.
 */

public class CourseCategory {

    private final String name;
    private final String letterGrade;
    private final int percentGrade;
    private final int percentWeight;

    public CourseCategory(String name, String letterGrade, int percentGrade, int percentWeight) {
        this.name = name;
        this.letterGrade = letterGrade;
        this.percentGrade = percentGrade;
        this.percentWeight = percentWeight;
    }

    public String getName() {
        return name;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public int getPercentGrade() {
        return percentGrade;
    }

    public int getPercentWeight() {
        return percentWeight;
    }

    public boolean isGraded() {
        return letterGrade != null && percentGrade >= 0;
    }

}
