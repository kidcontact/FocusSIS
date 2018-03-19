package org.kidcontact.focussis.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.kidcontact.focussis.network.UrlBuilder.FocusUrl;
import org.kidcontact.focussis.parser.CourseParser;
import org.kidcontact.focussis.parser.PageParser;
import org.kidcontact.focussis.parser.PortalParser;
import org.kidcontact.focussis.parser.ScheduleParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by slensky on 3/12/18.
 */

public class FocusApi {
    private static final String TAG = "FocusApi";

    private final String username;
    private final String password;
    private final Context context;
    private final RequestQueue requestQueue;

    private boolean loggedIn = false;
    private long sessionLengthMillis = 20 * 60 * 1000; // milliseconds
    private long sessionTimeout;

    public FocusApi(String username, String password, Context context) {
        this.username = username;
        this.password = password;
        this.context = context;
        requestQueue = RequestSingleton.getInstance(context).getRequestQueue();
    }

    private void updateSessionTimeout() {
        this.sessionTimeout = System.currentTimeMillis() + this.sessionLengthMillis;
    }

    public Request login(final Response.Listener<Boolean> listener, final Response.ErrorListener errorListener) {
        StringRequest loginRequest = new StringRequest(
                Request.Method.POST, UrlBuilder.get(FocusUrl.LOGIN),new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                boolean success = false;
                try {
                    JSONObject responseJson = new JSONObject(response);
                    success = responseJson.getBoolean("success");
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing login response as JSON");
                    e.printStackTrace();
                }

                if (success) {
                    loggedIn = true;
                    updateSessionTimeout();
                }

                listener.onResponse(success);
            }
        }, errorListener){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("login" , "true");
                params.put("data", "username=" + username + "&password=" + password);
                return params;
            }

        };

        requestQueue.add(loginRequest);
        return loginRequest;
    }

    public Request getPortal(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest portalRequest = new StringRequest(
                Request.Method.GET, UrlBuilder.get(FocusUrl.PORTAL), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser portalParser = new PortalParser();
                try {
                    listener.onResponse(portalParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing portal");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        requestQueue.add(portalRequest);
        return portalRequest;
    }

    public Request getCourse(final String id, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest courseRequest = new StringRequest(
                Request.Method.GET, UrlBuilder.get(FocusUrl.COURSE_PRE) + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser courseParser = new CourseParser();
                try {
                    listener.onResponse(courseParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing course (ID " + id + ")");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        requestQueue.add(courseRequest);
        return courseRequest;
    }

    public Request getSchedule(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest scheduleRequest = new StringRequest(
                Request.Method.GET, UrlBuilder.get(FocusUrl.SCHEDULE), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser scheduleParser = new ScheduleParser();
                try {
                    listener.onResponse(scheduleParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing schedule");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        requestQueue.add(scheduleRequest);
        return scheduleRequest;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }
}
