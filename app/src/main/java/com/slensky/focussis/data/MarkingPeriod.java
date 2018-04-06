package com.slensky.focussis.data;

/**
 * Created by slensky on 4/28/17.
 */

public class MarkingPeriod {
    private final String id;
    private final int year;
    private final boolean selected;
    private final String name;

    public MarkingPeriod(String id, int year, boolean selected, String name) {
        this.id = id;
        this.year = year;
        this.selected = selected;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getName() {
        return name;
    }
}
