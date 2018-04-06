package com.slensky.focussis.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by slensky on 3/26/18.
 */

public class JSONUtil {

    public static JSONObject concatJson(JSONObject json1, JSONObject json2) throws JSONException {
        JSONObject n = new JSONObject();
        Iterator it = json1.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            n.put(key, json1.get(key));
        }
        it = json2.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            n.put(key, json2.get(key));
        }
        return n;
    }

}
