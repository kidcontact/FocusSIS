package com.slensky.focussis.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.slensky.focussis.R;
import com.slensky.focussis.data.Absences;
import com.slensky.focussis.data.Address;
import com.slensky.focussis.data.Calendar;
import com.slensky.focussis.data.CalendarEvent;
import com.slensky.focussis.data.CalendarEventDetails;
import com.slensky.focussis.data.Course;
import com.slensky.focussis.data.Demographic;
import com.slensky.focussis.data.FinalGrades;
import com.slensky.focussis.data.FinalGradesPage;
import com.slensky.focussis.data.FocusPreferences;
import com.slensky.focussis.data.PasswordResponse;
import com.slensky.focussis.data.Portal;
import com.slensky.focussis.data.Referrals;
import com.slensky.focussis.data.Schedule;
import com.slensky.focussis.data.Student;
import com.slensky.focussis.util.GsonSingleton;
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
    }

    private String readResource(int id) {
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

        return writer.toString();
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
    public Request login(final Listener<Boolean> listener, Response.ErrorListener errorListener) {
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
    public Request logout(final Listener<Boolean> listener, Response.ErrorListener errorListener) {
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
    public Request getPortal(final Listener<Portal> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_portal), Portal.class));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getCourse(final String id, final Listener<Course> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(getIdForRawFile("debug_course_" + id)), Course.class));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getSchedule(final Listener<Schedule> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_schedule), Schedule.class));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getCalendar(int year, int month, final Listener<Calendar> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_calendar), Calendar.class));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getCalendarEvent(final String id, final CalendarEvent.EventType eventType, final Listener<CalendarEventDetails> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                if (eventType == CalendarEvent.EventType.ASSIGNMENT) {
                    listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_calendar_assignment_1), CalendarEventDetails.class));
                }
                else if (eventType == CalendarEvent.EventType.OCCASION) {
                    listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_calendar_event_1), CalendarEventDetails.class));
                }
            }
        });
        return dummyRequest;
    }

    @Override
    public void getDemographic(final Listener<Demographic> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_demographic), Demographic.class));
            }
        });
    }

    @Override
    public void getAddress(final Listener<Address> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_address), Address.class));
            }
        });
    }

    @Override
    public Request getReferrals(final Listener<Referrals> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_referrals), Referrals.class));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getAbsences(final Listener<Absences> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_absences), Absences.class));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getFinalGrades(final FinalGradesType type, final Listener<FinalGrades> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                int gradesId = -1;
                switch (type) {
                    case COURSE_HISTORY:
                        gradesId = R.raw.debug_final_grades_course_history;
                        break;
                    case CURRENT_SEMESTER:
                        gradesId = R.raw.debug_final_grades_current_semester;
                        break;
                    case CURRENT_SEMESTER_EXAMS:
                        gradesId = R.raw.debug_final_grades_current_semester_exams;
                        break;
                    case ALL_SEMESTERS:
                        gradesId = R.raw.debug_final_grades_all_semesters;
                        break;
                    case ALL_SEMESTERS_EXAMS:
                        gradesId = R.raw.debug_final_grades_all_semesters_exams;
                        break;
                }
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(gradesId), FinalGrades.class));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request getPreferences(final Listener<FocusPreferences> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_preferences), FocusPreferences.class));
            }
        });
        return dummyRequest;
    }

    @Override
    public Request setPreferences(FocusPreferences preferences, Listener<FocusPreferences> listener, Response.ErrorListener errorListener) {
        throw new UnsupportedOperationException("Cannot set preferences on debug API");
    }

    @Override
    public Request changePassword(String currentPassword, String newPassword, String verifyNewPassword, final Listener<PasswordResponse> listener, Response.ErrorListener errorListener) {
        waitForFakeLoad(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(GsonSingleton.getInstance().fromJson(readResource(R.raw.debug_change_password), PasswordResponse.class));
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

}
