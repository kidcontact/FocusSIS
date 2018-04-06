package com.slensky.focussis.util;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by slensky on 4/16/17.
 */

public class GsonSingleton {

    private static Gson gson = Converters.registerDateTime(new GsonBuilder()).create();

    public static Gson getInstance() {
        return gson;
    }

}
