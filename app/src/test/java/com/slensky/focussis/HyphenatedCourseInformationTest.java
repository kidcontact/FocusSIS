package com.slensky.focussis;

import com.slensky.focussis.data.ScheduleCourse;
import com.slensky.focussis.parser.FocusPageParser;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test parsing of various kinds of hyphenated course information found on Focus
 */
public class HyphenatedCourseInformationTest {

    @Test
    public void hyphenatedCourseInformationParsing_isCorrect() throws Exception {
        String courseString1 = "Physical Education - Period 1 - H - Red - Clive Alston";
        FocusPageParser.HyphenatedCourseInformation parsedCourse1 = FocusPageParser.parseHyphenatedCourseInformation(courseString1, true, true);
        FocusPageParser.HyphenatedCourseInformation expectedCourse1 =
                new FocusPageParser.HyphenatedCourseInformation("Physical Education", "1", true, ScheduleCourse.Term.YEAR,"H", "Red", "Clive Alston");

        String courseString2 = "Language Arts - - MWF - Red - Ella Barron";
        FocusPageParser.HyphenatedCourseInformation parsedCourse2 = FocusPageParser.parseHyphenatedCourseInformation(courseString2, true, true);
        FocusPageParser.HyphenatedCourseInformation expectedCourse2 =
                new FocusPageParser.HyphenatedCourseInformation("Language Arts", null, false, ScheduleCourse.Term.YEAR, "MWF", "Red", "Ella Barron");

        String courseString3 = "- MWF - Red - Ella Barron";
        FocusPageParser.HyphenatedCourseInformation parsedCourse3 = FocusPageParser.parseHyphenatedCourseInformation(courseString3, false, true);
        FocusPageParser.HyphenatedCourseInformation expectedCourse3 =
                new FocusPageParser.HyphenatedCourseInformation(null, null, false, ScheduleCourse.Term.YEAR, "MWF", "Red", "Ella Barron");

        // same thing but with some oddities in teacher name
        String courseString4 = "- MWF - Red - Ella  van-Barron";
        FocusPageParser.HyphenatedCourseInformation parsedCourse4 = FocusPageParser.parseHyphenatedCourseInformation(courseString4, false, true);
        FocusPageParser.HyphenatedCourseInformation expectedCourse4 =
                new FocusPageParser.HyphenatedCourseInformation(null, null, false, ScheduleCourse.Term.YEAR, "MWF", "Red", "Ella van-Barron");

        String courseString5 = "Advisory - M - Red - Ella Barron";
        FocusPageParser.HyphenatedCourseInformation parsedCourse5 = FocusPageParser.parseHyphenatedCourseInformation(courseString5, false, true);
        FocusPageParser.HyphenatedCourseInformation expectedCourse5 =
                new FocusPageParser.HyphenatedCourseInformation(null, "Advisory", false, ScheduleCourse.Term.YEAR, "M", "Red", "Ella Barron");

        String courseString6 = "Senior Project - Advisory - W - 001 - Amarah Travis";
        FocusPageParser.HyphenatedCourseInformation parsedCourse6 = FocusPageParser.parseHyphenatedCourseInformation(courseString6, true, true);
        FocusPageParser.HyphenatedCourseInformation expectedCourse6 =
                new FocusPageParser.HyphenatedCourseInformation("Senior Project", "Advisory", false, ScheduleCourse.Term.YEAR, "W", "001", "Amarah Travis");

        String courseString7 = "Advisory - 12 - Shayna Hollis";
        FocusPageParser.HyphenatedCourseInformation parsedCourse7 = FocusPageParser.parseHyphenatedCourseInformation(courseString7, false, false);
        FocusPageParser.HyphenatedCourseInformation expectedCourse7 =
                new FocusPageParser.HyphenatedCourseInformation(null, "Advisory", false, ScheduleCourse.Term.YEAR, null, "12", "Shayna Hollis");

        assertEquals(parsedCourse1, expectedCourse1);
        assertEquals(parsedCourse2, expectedCourse2);
        assertEquals(parsedCourse3, expectedCourse3);
        assertEquals(parsedCourse4, expectedCourse4);
        assertEquals(parsedCourse5, expectedCourse5);
        assertEquals(parsedCourse6, expectedCourse6);
        assertEquals(parsedCourse7, expectedCourse7);

        System.out.println(parsedCourse1);
        System.out.println(parsedCourse2);
        System.out.println(parsedCourse3);
        System.out.println(parsedCourse4);
        System.out.println(parsedCourse5);
        System.out.println(parsedCourse6);
        System.out.println(parsedCourse7);

    }

}