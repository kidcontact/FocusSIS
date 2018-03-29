package org.kidcontact.focussis.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kidcontact.focussis.data.CalendarEvent;
import org.kidcontact.focussis.data.Student;
import org.kidcontact.focussis.network.UrlBuilder.FocusUrl;
import org.kidcontact.focussis.parser.AddressParser;
import org.kidcontact.focussis.parser.CalendarEventParser;
import org.kidcontact.focussis.parser.CalendarParser;
import org.kidcontact.focussis.parser.CourseParser;
import org.kidcontact.focussis.parser.DemographicParser;
import org.kidcontact.focussis.parser.PageParser;
import org.kidcontact.focussis.parser.PortalParser;
import org.kidcontact.focussis.parser.ReferralsParser;
import org.kidcontact.focussis.parser.ScheduleParser;
import org.kidcontact.focussis.parser.StudentParser;
import org.kidcontact.focussis.util.JSONUtil;

import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slensky on 3/12/18.
 */

public class FocusApi {
    private static final String TAG = "FocusApi";

    private final String username;
    private final String password;
    private boolean hasAccessedStudentPage; // api access requires first sending GET to student url
    private Student student;

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

        queueRequest(loginRequest);
        return loginRequest;
    }

    public Request logout(final Response.Listener<Boolean> listener, final Response.ErrorListener errorListener) {
        StringRequest logoutRequest = new StringRequest(
                Request.Method.POST, UrlBuilder.get(FocusUrl.LOGOUT),new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loggedIn = false;
                listener.onResponse(true);
            }
        }, errorListener);

        queueRequest(logoutRequest);
        return logoutRequest;
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

        queueRequest(portalRequest);
        return portalRequest;
    }

    public Request getCourse(final String id, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest courseRequest = new StringRequest(
                Request.Method.GET, String.format(UrlBuilder.get(FocusUrl.COURSE), id), new Response.Listener<String>() {
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

        queueRequest(courseRequest);
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

        queueRequest(scheduleRequest);
        return scheduleRequest;
    }

    public Request getCalendar(int year, int month, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest calendarRequest = new StringRequest(
                Request.Method.GET, String.format(UrlBuilder.get(FocusUrl.CALENDAR), month, year), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser calendarParser = new CalendarParser();
                try {
                    listener.onResponse(calendarParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing calendar");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        queueRequest(calendarRequest);
        return calendarRequest;
    }

    // alternative to assignment is event
    public Request getCalendarEvent(final String id, CalendarEvent.EventType eventType, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        String url = eventType.equals(CalendarEvent.EventType.ASSIGNMENT) ? UrlBuilder.get(FocusUrl.ASSIGNMENT) : UrlBuilder.get(FocusUrl.EVENT);
        StringRequest eventRequest = new StringRequest(
                Request.Method.GET, String.format(url, id), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser calendarEventParser = new CalendarEventParser();
                try {
                    JSONObject parsed = calendarEventParser.parse(response);
                    parsed.put("id", id);
                    listener.onResponse(parsed);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing calendar");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        queueRequest(eventRequest);
        return eventRequest;
    }

    private void ensureStudentPage(final MultipartRequest nextRequest) {
        if (!hasAccessedStudentPage) {
            Log.d(TAG, "Retrieving student page for first time");
            final StringRequest studentRequest = new StringRequest(Request.Method.GET, UrlBuilder.get(FocusUrl.STUDENT), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                final PageParser studentParser = new StudentParser();
                try {
                    final JSONObject parsed = studentParser.parse(response);
                    ImageRequest imageRequest = new ImageRequest(parsed.getString("picture"),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    try {
                                        parsed.remove("picture");
                                        student = new Student(parsed);
                                        student.setPicture(bitmap);
                                        hasAccessedStudentPage = true;
                                        queueRequest(nextRequest);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing student JSON (error in code, not Focus)");
                                        e.printStackTrace();
                                    }
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    Log.w(TAG, "Network error fetching image, continuing anyway");
                                    try {
                                        parsed.remove("picture");
                                        student = new Student(parsed);
                                        hasAccessedStudentPage = true;
                                        queueRequest(nextRequest);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing student JSON (error in code, not Focus)");
                                        e.printStackTrace();
                                    }
                                }
                            });
                    queueRequest(imageRequest);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing student page");
                    e.printStackTrace();
                    nextRequest.deliverResponse(null);
                }
                }
            }, nextRequest.getErrorListener());
            queueRequest(studentRequest);
        }
        else {
            queueRequest(nextRequest);
        }
    }

    public Request getDemographic(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        final MultipartRequest demographicRequest = new MultipartRequest(
                Request.Method.POST, UrlBuilder.get(FocusUrl.STUDENT), new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                PageParser demographicParser = new DemographicParser();
                String responseStr = new String(response.data);
                try {
                    JSONObject parsed = demographicParser.parse(responseStr);
                    parsed = JSONUtil.concatJson(parsed, student.getJson());
                    listener.onResponse(parsed);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing demographic");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                String req = "{\"requests\":[" +
                        "{\"controller\":\"EditController\",\"method\":\"cache:getFieldData\",\"token\":\"6f62bdafe60d9a146aa7be7174bcc31f8340360e\",\"args\":[\"general\",\"SISStudent\",%s]}," +
                        "{\"controller\":\"EditController\",\"method\":\"cache:getFieldData\",\"token\":\"6f62bdafe60d9a146aa7be7174bcc31f8340360e\",\"args\":[\"9\",\"SISStudent\",%<s]}" +
                        "]}";
                Log.d(TAG,String.format(req, student.getId()));

                params.put("__call__", String.format(req, student.getId()));
                return params;
            }
        };

        ensureStudentPage(demographicRequest);
        return demographicRequest;
    }

    public Request getAddress(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        final MultipartRequest addressRequest = new MultipartRequest(
                Request.Method.POST, UrlBuilder.get(FocusUrl.STUDENT), new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                PageParser addressParser = new AddressParser();
                String responseStr = new String(response.data);
                try {
                    JSONObject parsed = addressParser.parse(responseStr);
                    parsed = JSONUtil.concatJson(parsed, student.getJson());
                    listener.onResponse(parsed);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing address");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                String req = "{\"requests\":[" +
                        "{\"controller\":\"AddressController\",\"method\":\"getAddresses\",\"token\":\"abb1810fed911fb83956bc40780ab74d147548c0\",\"args\":[%s]}," +
                        "{\"controller\":\"AddressController\",\"method\":\"getContacts\",\"token\":\"9c1d08797dbab3df87fb894dd22599c84500897b\",\"args\":[%<s]}" +
                        "]}";
                Log.d(TAG, String.format(req, student.getId()));

                params.put("__call__", String.format(req, student.getId()));
                return params;
            }
        };

        ensureStudentPage(addressRequest);
        return addressRequest;
    }

    public Request getReferrals(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest referralsRequest = new StringRequest(
                Request.Method.GET, UrlBuilder.get(FocusUrl.REFERRALS), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser referralsParser = new ReferralsParser();
                try {
                    listener.onResponse(referralsParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing schedule");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        queueRequest(referralsRequest);
        return referralsRequest;
    }

    public boolean isSessionExpired() {
        return sessionTimeout <= System.currentTimeMillis();
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    private void queueRequest(Request request) {
        Log.i(TAG, "Queuing " + request.getUrl());
        CookieManager cookieManager = RequestSingleton.getCookieManager();
        for (int i = 0; i < cookieManager.getCookieStore().getCookies().size(); i++) {
            cookieManager.getCookieStore().getCookies().get(0).setSecure(false);
        }
        requestQueue.add(request);
    }

    public Student getStudent() {
        return student;
    }

}
