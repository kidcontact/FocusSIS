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
import com.android.volley.toolbox.RequestFuture;
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
import java.util.concurrent.ExecutionException;

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
    protected Student student;
    private boolean hasAccessedFinalGradesPage; // api access requires first sending GET to final grades url
    protected FinalGradesPage finalGradesPage;

    protected final Context context;

    boolean loggedIn = false;
    private long sessionLengthMillis = 20 * 60 * 1000; // milliseconds
    long sessionTimeout;

    public FocusApi(String username, String password, Context context) {
        this.username = username;
        this.password = password;
        this.context = context;
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
                Map<String,String> params = new HashMap<>();
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
                }
            }
        }, errorListener);

        queueRequest(eventRequest);
        return eventRequest;
    }

    private void ensureStudentPage(final Runnable onResponse, final Response.ErrorListener errorListener) {
        if (!hasAccessedStudentPage) {
            Log.d(TAG, "Retrieving student page for first time");
            final StringRequest studentRequest = new StringRequest(Request.Method.GET, UrlBuilder.get(FocusUrl.STUDENT), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                final PageParser studentParser = new StudentParser();
                try {
                    final JSONObject parsed = studentParser.parse(response);
                    student = new Student(parsed);

                    ImageRequest imageRequest = new ImageRequest(parsed.getString("picture"),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    parsed.remove("picture");
                                    student.setPicture(bitmap);
                                    hasAccessedStudentPage = true;
                                    onResponse.run();
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    Log.w(TAG, "Network error fetching image, continuing anyway");
                                    parsed.remove("picture");
                                    hasAccessedStudentPage = true;
                                    onResponse.run();
                                }
                            });
                    queueRequest(imageRequest);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing student page");
                    e.printStackTrace();
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
                }
                }
            }, errorListener);
            queueRequest(studentRequest);
        } else {
            onResponse.run();
        }
    }

    public void getDemographic(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        ensureStudentPage(new Runnable() {
            @Override
            public void run() {
                final String csrfToken = student.getMethods().get("EditController").get("getFieldData");

                final MultipartRequest demographicRequest = new MultipartRequest(
                        Request.Method.POST, student.getApiUrl(), new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        PageParser demographicParser = new DemographicParser();
                        String responseStr = new String(response.data);
                        try {
                            JSONObject parsed = demographicParser.parse(responseStr);
                            parsed = JSONUtil.concatJson(parsed, student.getJson());
                            listener.onResponse(parsed);
                        } catch (JSONException e) {
                            Log.d(TAG, responseStr);
                            Log.e(TAG, "JSONException while parsing demographic");
                            e.printStackTrace();
                            errorListener.onErrorResponse(new VolleyError(e.toString()));
                            throw new RuntimeException(e);
                        }
                    }
                }, errorListener) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        String req = "{\"requests\":[" +
                                "{\"controller\":\"EditController\",\"method\":\"cache:getFieldData\",\"token\":\"" + csrfToken + "\",\"args\":[\"general\",\"SISStudent\",%s]}," +
                                "{\"controller\":\"EditController\",\"method\":\"cache:getFieldData\",\"token\":\"" + csrfToken + "\",\"args\":[\"9\",\"SISStudent\",%<s]}" +
                                "]}";
                        Log.d(TAG,String.format(req, student.getId()));

                        params.put("__call__", String.format(req, student.getId()));
                        return params;
                    }
                };
                queueRequest(demographicRequest);
            }
        }, errorListener);
    }

    public void getAddress(final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        ensureStudentPage(new Runnable() {
            @Override
            public void run() {
                final String csrfTokenAddresses = student.getMethods().get("AddressController").get("getAddresses");
                final String csrfTokenContacts = student.getMethods().get("AddressController").get("getContacts");

                final MultipartRequest addressRequest = new MultipartRequest(
                        Request.Method.POST, student.getApiUrl(), new Response.Listener<NetworkResponse>() {
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
                            errorListener.onErrorResponse(new VolleyError(e.toString()));
                            throw new RuntimeException(e);
                        }
                    }
                }, errorListener) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        String req = "{\"requests\":[" +
                                "{\"controller\":\"AddressController\",\"method\":\"getAddresses\",\"token\":\"" + csrfTokenAddresses + "\",\"args\":[%s]}," +
                                "{\"controller\":\"AddressController\",\"method\":\"getContacts\",\"token\":\"" + csrfTokenContacts + "\",\"args\":[%<s]}" +
                                "]}";
                        Log.d(TAG, String.format(req, student.getId()));

                        params.put("__call__", String.format(req, student.getId()));
                        return params;
                    }
                };
                queueRequest(addressRequest);
            }
        }, errorListener);
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
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
                        nextRequest.getErrorListener().onErrorResponse(new VolleyError(e.toString()));
                        throw new RuntimeException(e);
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
                }
            }
        }, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
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
                    errorListener.onErrorResponse(new VolleyError(e.toString()));
                    throw new RuntimeException(e);
                }
            }
        }, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
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

    public boolean isLoggedIn() {
        return loggedIn;
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
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    public void cancelAll(final RequestQueue.RequestFilter filter) {
        RequestSingleton.getInstance(context).getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return filter.apply(request);
            }
        });
    }

    public Student getStudent() {
        return student;
    }

}
