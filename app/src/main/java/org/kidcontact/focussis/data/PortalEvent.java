package org.kidcontact.focussis.data;

import org.joda.time.DateTime;

/**
 * Created by slensky on 4/14/17.
 */

public class PortalEvent {

    private final String description;
    private final DateTime date;

    public PortalEvent(String description, DateTime date) {
        this.description = description;
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public DateTime getDate() {
        return date;
    }

}
