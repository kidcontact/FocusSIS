package com.slensky.focussis.util;

/**
 * Created by slensky on 4/25/18.
 */

public class SchoolSingleton {
    private static final SchoolSingleton ourInstance = new SchoolSingleton();

    public static SchoolSingleton getInstance() {
        return ourInstance;
    }

    private SchoolSingleton() {
    }
}
