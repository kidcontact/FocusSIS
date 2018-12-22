package com.slensky.focussis.util;

import com.slensky.focussis.school.ASD;
import com.slensky.focussis.school.School;

/**
 * Created by slensky on 4/25/18.
 */

public class SchoolSingleton {
    private static final SchoolSingleton ourInstance = new SchoolSingleton();
    private School school = new ASD();

    public static SchoolSingleton getInstance() {
        return ourInstance;
    }

    private SchoolSingleton() {

    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }
}
