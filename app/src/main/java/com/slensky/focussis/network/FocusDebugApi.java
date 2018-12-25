package com.slensky.focussis.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.slensky.focussis.R;
import com.slensky.focussis.data.CalendarEvent;
import com.slensky.focussis.data.FinalGradesPage;
import com.slensky.focussis.data.FocusPreferences;
import com.slensky.focussis.data.Student;
import com.slensky.focussis.util.JSONUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by slensky on 5/3/18.
 * Generates test data offline instead of connecting to Focus
 */

public class FocusDebugApi extends FocusApi {
    private static final String TAG = "FocusDebugApi";
    private static final int FAKE_LOAD_TIME_MS = 500;
    private long sessionLengthMillis = 20 * 60 * 1000; // milliseconds
    private List<Thread> threads = new ArrayList<>();
    private Request dummyRequest;
    private Handler handler;

    public FocusDebugApi(String username, String password, Context context) {
        super(username, password, context);
        dummyRequest = new StringRequest(Request.Method.GET, null, null, null);
        handler = new Handler(context.getMainLooper());
        // TODO: FIX
//        try {
//             student = new Student(readJSON(R.raw.debug_student));
//        } catch (JSONException e) {
//            Log.e(TAG, "JSONException while creating student from debug data");
//            e.printStackTrace();
//        }
        try {
            finalGradesPage = new FinalGradesPage(readJSON(R.raw.debug_final_grades));
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while creating final grades page from debug data");
            e.printStackTrace();
        }
    }

    private JSONObject readJSON(int id) {
        InputStream is = context.getResources().openRawResource(id);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException reading JSON file");
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException closing JSON file");
                e.printStackTrace();
            }
        }

        String jsonString = writer.toString();
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException reading jsonString " + jsonString);
            e.printStackTrace();
        }
        return null;
    }

    private int getIdForRawFile(String name) {
        try {
            return R.raw.class.getField(name).getInt(null);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "No raw field " + name + " found");
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "No raw field " + name + " found");
            throw new RuntimeException(e);
        }
    }

    private void waitForFakeLoad(final Runnable callback) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(FAKE_LOAD_TIME_MS);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Fake load thread interrupted");
                }

                if (!Thread.interrupted()) {
                    handler.post(callback);
                }
            }
        });
        threads.add(thread);
        thread.start();
    }

    @Override
    public Request login(final Response.Listener<Boolean> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                loggedIn = true;
                listener.onResponse(true);
            }
        });
        return dummyRequest;
    }

    @Override
    public Request logout(final Response.Listener<Boolean> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                loggedIn = false;
                listener.onResponse(true);
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getPortal(final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(readJSON(R.raw.debug_portal));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getCourse(final String id, final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(readJSON(getIdForRawFile("debug_course_" + id)));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getSchedule(final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(readJSON(R.raw.debug_schedule));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getCalendar(int year, int month, final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(readJSON(R.raw.debug_calendar));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getCalendarEvent(final String id, final CalendarEvent.EventType eventType, final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                if (eventType == CalendarEvent.EventType.ASSIGNMENT) {
                    listener.onResponse(readJSON(getIdForRawFile("debug_calendar_assignment_" + id)));
                }
                else if (eventType == CalendarEvent.EventType.OCCASION) {
                    listener.onResponse(readJSON(getIdForRawFile("debug_calendar_event_" + id)));
                }
            }
        });
        return dummyRequest;
    }

    public void getDemographic(final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onResponse(JSONUtil.concatJson(readJSON(R.raw.debug_student), readJSON(R.raw.debug_demographic)));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException concatenating student page JSON");
                    e.printStackTrace();
                }
            }
        });
    }

    public void getAddress(final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onResponse(JSONUtil.concatJson(readJSON(R.raw.debug_student), readJSON(R.raw.debug_address)));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException concatenating student page JSON");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public Request getReferrals(final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(readJSON(R.raw.debug_referrals));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getAbsences(final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(readJSON(R.raw.debug_absences));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getFinalGrades(final FinalGradesType type, final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                JSONObject gradePage = readJSON(R.raw.debug_final_grades);
                JSONObject grades = null;
                switch (type) {
                    case COURSE_HISTORY:
                        grades = readJSON(R.raw.debug_final_grades_course_history);
                        break;
                    case CURRENT_SEMESTER:
                        grades = readJSON(R.raw.debug_final_grades_current_semester);
                        break;
                    case CURRENT_SEMESTER_EXAMS:
                        grades = readJSON(R.raw.debug_final_grades_current_semester_exams);
                        break;
                    case ALL_SEMESTERS:
                        grades = readJSON(R.raw.debug_final_grades_all_semesters);
                        break;
                    case ALL_SEMESTERS_EXAMS:
                        grades = readJSON(R.raw.debug_final_grades_all_semesters_exams);
                        break;
                }
                try {
                    listener.onResponse(JSONUtil.concatJson(gradePage, grades));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException concatenating final grades page JSON");
                    e.printStackTrace();
                }
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getPreferences(final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(readJSON(R.raw.debug_preferences));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request setPreferences(FocusPreferences preferences, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        throw new UnsupportedOperationException("Cannot set preferences on debug API");
    }

    @Override
    public Request changePassword(String currentPassword, String newPassword, String verifyNewPassword, final Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(readJSON(R.raw.debug_change_password));
            }
        });
        return dummyRequest;
    }

    @Override
    public void cancelAll(RequestQueue.RequestFilter filter) {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    @Override
    public boolean isSessionExpired() {
        return false;
    }

    @Override
    public long getSessionTimeout() {
        if (sessionTimeout <= System.currentTimeMillis()) {
            sessionTimeout = System.currentTimeMillis() + this.sessionLengthMillis;
        }
        return sessionTimeout;
    }

    @Override
    public void setSessionTimeout(long sessionTimeout) {
        super.setSessionTimeout(sessionTimeout);
    }

    @Override
    public boolean isLoggedIn() {
        return super.isLoggedIn();
    }

    @Override
    public void setLoggedIn(boolean loggedIn) {
        super.setLoggedIn(loggedIn);
    }

    @Override
    public boolean hasSession() {
        return true;
    }

    @Override
    public Student getStudent() {
        return super.getStudent();
    }

}
