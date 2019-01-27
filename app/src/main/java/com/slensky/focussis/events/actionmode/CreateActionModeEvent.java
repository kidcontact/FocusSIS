package com.slensky.focussis.events.actionmode;

import androidx.annotation.IdRes;

public class CreateActionModeEvent {

    @IdRes
    private int menuResId;
    private String title;

    public CreateActionModeEvent(int menuResId) {
        this.menuResId = menuResId;
    }

    public int getMenuResId() {
        return menuResId;
    }

    public String getTitle() {
        return title;
    }

}
