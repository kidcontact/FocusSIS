package com.slensky.focussis.events.actionmode;

import androidx.annotation.IdRes;

public class ActionModeClickEvent {

    @IdRes
    private int actionId;

    public ActionModeClickEvent(int actionId) {
        this.actionId = actionId;
    }

    public int getActionId() {
        return actionId;
    }

}
