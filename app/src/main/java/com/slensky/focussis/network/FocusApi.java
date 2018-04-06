package com.slensky.focussis.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import com.slensky.focussis.data.CalendarEvent;
import com.slensky.focussis.data.FinalGradesPage;
import com.slensky.focussis.data.FocusPreferences;
import com.slensky.focussis.data.Student;
import com.slensky.focussis.network.UrlBuilder.FocusUrl;
import com.slensky.focussis.parser.AbsencesParser;
import com.slensky.focussis.parser.AddressParser;
import com.slensky.focussis.parser.CalendarEventParser;
import com.slensky.focussis.parser.CalendarParser;
import com.slensky.focussis.parser.CourseParser;
import com.slensky.focussis.parser.DemographicParser;
import com.slensky.focussis.parser.FinalGradesPageParser;
import com.slensky.focussis.parser.FinalGradesParser;
import com.slensky.focussis.parser.PageParser;
import com.slensky.focussis.parser.PasswordResponseParser;
import com.slensky.focussis.parser.PortalParser;
import com.slensky.focussis.parser.PreferencesParser;
import com.slensky.focussis.parser.ReferralsParser;
import com.slensky.focussis.parser.ScheduleParser;
import com.slensky.focussis.parser.StudentParser;
import com.slensky.focussis.util.JSONUtil;

import java.net.HttpCookie;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by slensky on 3/12/18.
 */

public class FocusApi {
    private static final String TAG = "FocusApi";

    private final String username;
    private final String password;
    private boolean hasAccessedStudentPage; // api access requires first sending GET to student url
    private Student student;
    private boolean hasAccessedFinalGradesPage; // api access requires first sending GET to final grades url
    private FinalGradesPage finalGradesPage;

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
        hasAccessedStudentPage = false;
        hasAccessedFinalGradesPage = false;
        StringRequest loginRequest = new StringRequest(
                Request.Method.POST, UrlBuilder.get(FocusUrl.LOGIN), new Response.Listener<String>() {
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
                    Log.e(TAG, "JSONException while parsing referrals");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        queueRequest(referralsRequest);
        return referralsRequest;
    }

    public Request getAbsences(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest absencesRequest = new StringRequest(
                Request.Method.GET, UrlBuilder.get(FocusUrl.ABSENCES), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser absencesParser = new AbsencesParser();
                try {
                    listener.onResponse(absencesParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing absences");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        queueRequest(absencesRequest);
        return absencesRequest;
    }

    private void ensureFinalGradesPage(final DeliverableStringRequest nextRequest) {
        if (!hasAccessedFinalGradesPage) {
            Log.d(TAG, "Retrieving final grades page for first time");
            final StringRequest finalGradesRequest = new StringRequest(Request.Method.GET, UrlBuilder.get(FocusUrl.FINAL_GRADES), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    final PageParser finalGradesPageParser = new FinalGradesPageParser();
                    try {
                        final JSONObject parsed = finalGradesPageParser.parse(response);
                        finalGradesPage = new FinalGradesPage(parsed);
                        hasAccessedFinalGradesPage = true;
                        queueRequest(nextRequest);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException while parsing final grades page");
                        e.printStackTrace();
                        nextRequest.deliverResponse(null);
                    }
                }
            }, nextRequest.getErrorListener());
            queueRequest(finalGradesRequest);
        }
        else {
            queueRequest(nextRequest);
        }
    }

    private void signRequest(Map<String, String> request, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        String digest = String.format("-%s-%s-%s", request.get("accessID"), request.get("api"), request.get("method"));
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);

        Formatter formatter = new Formatter();
        for (byte b : mac.doFinal(digest.getBytes())) {
            formatter.format("%02x", b);
        }
        request.put("signature", formatter.toString());
    }

    public enum FinalGradesType {
        COURSE_HISTORY,
        CURRENT_SEMESTER,
        CURRENT_SEMESTER_EXAMS,
        ALL_SEMESTERS,
        ALL_SEMESTERS_EXAMS
    }
    public Request getFinalGrades(final FinalGradesType type, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        DeliverableStringRequest finalGradesRequest = new DeliverableStringRequest(
                Request.Method.POST, UrlBuilder.get(FocusUrl.API), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser finalGradesParser = new FinalGradesParser();
                try {
                    JSONObject parsed = finalGradesParser.parse(response);
                    parsed = JSONUtil.concatJson(parsed, finalGradesPage.getJson());
                    listener.onResponse(parsed);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing final grades");
                    e.printStackTrace();
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
//                    listener.onResponse(null);
                }
            }
        }, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new LinkedHashMap<>();
                params.put("accessID", finalGradesPage.getStudentId());
                params.put("api", "finalGrades");
                params.put("method", "requestGrades");
                params.put("modname", "Grades/StudentRCGrades.php");
                String argKey = "arguments[]";
                switch (type) {
                    case COURSE_HISTORY:
                        params.put(argKey, "-1");
                        break;
                    case CURRENT_SEMESTER:
                        params.put(argKey, finalGradesPage.getCurrentSemesterTargetMarkingPeriod());
                        break;
                    case CURRENT_SEMESTER_EXAMS:
                        params.put(argKey, finalGradesPage.getCurrentSemesterExamsTargetMarkingPeriod());
                        break;
                    case ALL_SEMESTERS:
                        params.put(argKey, "all_SEM");
                        break;
                    case ALL_SEMESTERS_EXAMS:
                        params.put(argKey, "all_SEM_exams");
                        break;
                }
                params.put("arguments[1][**FIRST-REQUEST**]", "true");
                try {
                    signRequest(params, finalGradesPage.getHmacSecret());
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, "Could not find HmacSHA1 algorithm for signing final grades request");
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    Log.e(TAG, "Invalid key for HmacSHA1 algorithm while signing final grades request");
                    e.printStackTrace();
                }
                return  params;
            }
        };

        ensureFinalGradesPage(finalGradesRequest);
        return finalGradesRequest;
    }

    public Request getPreferences(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest preferencesRequest = new StringRequest(
                Request.Method.GET, UrlBuilder.get(FocusUrl.PREFERENCES), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser preferencesParser = new PreferencesParser();
                try {
                    listener.onResponse(preferencesParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing preferences");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener);

        queueRequest(preferencesRequest);
        return preferencesRequest;
    }

    public Request setPreferences(final FocusPreferences preferences, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest preferencesRequest = new StringRequest(
                Request.Method.POST, UrlBuilder.get(FocusUrl.PREFERENCES), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser preferencesParser = new PreferencesParser();
                try {
                    listener.onResponse(preferencesParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing preferences");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                if (preferences.isEnglishLanguage()) {
                    params.put("values[Preferences][LANGUAGE]", "en_US");
                    params.put("btn_save", "Save");
                }
                return params;
            }
        };

        queueRequest(preferencesRequest);
        return preferencesRequest;
    }

    public Request changePassword(final String currentPassword, final String newPassword, final String verifyNewPassword, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        StringRequest passwordRequest = new StringRequest(
                Request.Method.POST, UrlBuilder.get(FocusUrl.PREFERENCES_PASSWORD), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                PageParser passwordParser = new PasswordResponseParser();
                try {
                    listener.onResponse(passwordParser.parse(response));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing password");
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("values[current]", currentPassword);
                params.put("values[verify]", verifyNewPassword);
                params.put("values[new]", newPassword);
                params.put("btn_save", "Save");
                return params;
            }
        };

        queueRequest(passwordRequest);
        return passwordRequest;
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

    public boolean hasSession() {
        for (HttpCookie c : RequestSingleton.getCookieManager().getCookieStore().getCookies()) {
            if (c.getName().equals("PHPSESSID")) {
                return true;
            }
        }
        return false;
    }

    private void queueRequest(Request request) {
        Log.d(TAG, "Queuing " + request.getUrl());
        // necessary for http monitoring (debug only)
//        CookieManager cookieManager = RequestSingleton.getCookieManager();
//        for (int i = 0; i < cookieManager.getCookieStore().getCookies().size(); i++) {
//            cookieManager.getCookieStore().getCookies().get(0).setSecure(false);
//        }
        requestQueue.add(request);
    }

    public Student getStudent() {
        return student;
    }

}
