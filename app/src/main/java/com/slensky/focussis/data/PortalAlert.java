package com.slensky.focussis.data;

/**
 * Created by slensky on 3/20/18.
 */

public class PortalAlert {

    private final String message;
    private final String url;

    public PortalAlert(String message, String url) {
        this.message = message;
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }

}
