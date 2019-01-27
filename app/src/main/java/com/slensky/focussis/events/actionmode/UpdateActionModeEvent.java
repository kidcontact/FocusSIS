package com.slensky.focussis.events.actionmode;

public class UpdateActionModeEvent {

    private String title;

    public UpdateActionModeEvent(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
