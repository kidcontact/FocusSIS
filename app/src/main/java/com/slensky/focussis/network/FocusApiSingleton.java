package com.slensky.focussis.network;

/**
 * Created by slensky on 3/13/18.
 */

public class FocusApiSingleton {
    private static FocusApi api;

    public static FocusApi getApi() {
        return api;
    }

    public static void setApi(FocusApi api) {
        FocusApiSingleton.api = api;
    }
}
