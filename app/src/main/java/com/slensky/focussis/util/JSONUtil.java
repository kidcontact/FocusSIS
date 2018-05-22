package com.slensky.focussis.util;

import android.content.res.Resources;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

/**
 * Created by slensky on 3/26/18.
 */

public class JSONUtil {
    private static final String TAG = "JSONUtil";

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

    public static JSONObject JSONFromRawResource(Resources resources, int id) throws JSONException {
        InputStream resourceReader = resources.openRawResource(id);
        Writer writer = new StringWriter();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceReader, "UTF-8"));
            String line = reader.readLine();
            while (line != null) {
                writer.write(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unhandled exception while opening raw JSON", e);
        } finally {
            try {
                resourceReader.close();
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception while opening raw JSON", e);
            }
        }

        String jsonString = writer.toString();
        return new JSONObject(jsonString);
    }

}
