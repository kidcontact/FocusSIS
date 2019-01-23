package com.slensky.focussis.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.slensky.focussis.R;
import com.slensky.focussis.di.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppPreferencesHelper implements PreferencesHelper {

    private final SharedPreferences prefs;

    private final String KEY_SAVE_LOGIN;
    private final String KEY_SAVED_USERNAME;
    private final String KEY_SAVED_PASSWORD;
    private final String KEY_READ_SCHOOL_MESSAGE;
    private final String KEY_COURSE_ASSIGNMENT_MENU_CATEGORY; // format with course ID
    private final String KEY_SCHEDULE_REMEMBERED_TYPE_SELECTION;
    private final String KEY_GCALENDAR_ID_FOR_USER; // format with username
    private final String KEY_AUTOMATIC_LOGIN;
    private final String KEY_ALWAYS_CHECK_PREFERENCES;
    private final String KEY_SHOW_CUSTOM_ASSIGNMENTS_IN_PORTAL;

    @Inject
    public AppPreferencesHelper(@ApplicationContext Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        KEY_SAVE_LOGIN = context.getString(R.string.key_save_login);
        KEY_SAVED_USERNAME = context.getString(R.string.key_saved_username);
        KEY_SAVED_PASSWORD = context.getString(R.string.key_saved_password);
        KEY_READ_SCHOOL_MESSAGE = context.getString(R.string.key_read_school_message);
        KEY_COURSE_ASSIGNMENT_MENU_CATEGORY = context.getString(R.string.key_assignment_menu_category_for_course);
        KEY_SCHEDULE_REMEMBERED_TYPE_SELECTION = context.getString(R.string.key_schedule_remembered_type_selection);
        KEY_GCALENDAR_ID_FOR_USER = context.getString(R.string.key_gcalendar_id_for_user);
        KEY_AUTOMATIC_LOGIN = context.getString(R.string.key_automatic_login);
        KEY_ALWAYS_CHECK_PREFERENCES = context.getString(R.string.key_always_check_preferences);
        KEY_SHOW_CUSTOM_ASSIGNMENTS_IN_PORTAL = context.getString(R.string.key_show_custom_assignments_in_portal);
        if (!prefs.getBoolean(context.getString(R.string.key_already_converted_prefs_to_new), false)) {
            convertOldPreferencesToNew(context);
        }
    }

    // maintain compatibility with preferences set by old versions of the app (<=1.3)
    private void convertOldPreferencesToNew(Context context) {
        SharedPreferences.Editor editor = prefs.edit();

        SharedPreferences loginPrefs = context.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        if (loginPrefs.contains("saveLogin")) {
            editor.putBoolean(KEY_SAVE_LOGIN, loginPrefs.getBoolean("saveLogin", false));
        }
        if (loginPrefs.contains("username")) {
            editor.putString(KEY_SAVED_USERNAME, loginPrefs.getString("username", null));
        }
        if (loginPrefs.contains("password")) {
            editor.putString(KEY_SAVED_PASSWORD, loginPrefs.getString("password", null));
        }
        if (loginPrefs.contains("readSchoolMsg")) {
            editor.putBoolean(KEY_READ_SCHOOL_MESSAGE, loginPrefs.getBoolean("readSchoolMsg", false));
        }
        loginPrefs.edit().clear().apply();

        SharedPreferences schedulePrefs = context.getSharedPreferences("schedulePrefs", Context.MODE_PRIVATE);
        if (schedulePrefs.contains("spinnerSelection")) {
            editor.putInt(KEY_SCHEDULE_REMEMBERED_TYPE_SELECTION, schedulePrefs.getInt("spinnerSelection", 0));
        }
        schedulePrefs.edit().clear().apply();

        // old settings preferences
        if (prefs.contains("automatic_login")) {
            editor.putBoolean(KEY_AUTOMATIC_LOGIN, prefs.getBoolean("automatic_login", false));
            editor.remove("automatic_login");
        }
        if (prefs.contains("always_check_preferences")) {
            editor.putBoolean(KEY_ALWAYS_CHECK_PREFERENCES, prefs.getBoolean("always_check_preferences", true));
            editor.remove("always_check_preferences");
        }
        if (prefs.contains("show_custom_assignments_in_portal")) {
            editor.putBoolean(KEY_SHOW_CUSTOM_ASSIGNMENTS_IN_PORTAL, prefs.getBoolean("show_custom_assignments_in_portal", true));
            editor.remove("show_custom_assignments_in_portal");
        }

        editor.putBoolean(context.getString(R.string.key_already_converted_prefs_to_new), true);

        editor.apply();
    }


    @Override
    public boolean getSaveLogin() {
        return prefs.getBoolean(KEY_SAVE_LOGIN, false);
    }

    @Override
    public void setSaveLogin(boolean saveLogin) {
        prefs.edit().putBoolean(KEY_SAVE_LOGIN, saveLogin).apply();
    }

    @Override
    public String getSavedUsername() {
        return prefs.getString(KEY_SAVED_USERNAME, null);
    }

    @Override
    public void setSavedUsername(String username) {
        prefs.edit().putString(KEY_SAVED_USERNAME, username).apply();
    }

    @Override
    public String getSavedPassword() {
        return prefs.getString(KEY_SAVED_PASSWORD, null);
    }

    @Override
    public void setSavedPassword(String password) {
        prefs.edit().putString(KEY_SAVED_PASSWORD, password).apply();
    }

    @Override
    public boolean getAutomaticLogin() {
        return prefs.getBoolean(KEY_AUTOMATIC_LOGIN, false);
    }

    @Override
    public boolean checkPreferencesOnLogin() {
        return prefs.getBoolean(KEY_ALWAYS_CHECK_PREFERENCES, true);
    }

    @Override
    public boolean getReadSchoolMessage() {
        return prefs.getBoolean(KEY_READ_SCHOOL_MESSAGE, false);
    }

    @Override
    public void setReadSchoolMessage(boolean readSchoolMessage) {
        prefs.edit().putBoolean(KEY_READ_SCHOOL_MESSAGE, readSchoolMessage).apply();
    }

    @Override
    public boolean getShowCustomAssignmentsInPortal() {
        return prefs.getBoolean(KEY_SHOW_CUSTOM_ASSIGNMENTS_IN_PORTAL, true);
    }

    @Override
    public void setShowCustomAssignmentsInPortal(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_CUSTOM_ASSIGNMENTS_IN_PORTAL, show).apply();
    }

    @Override
    public String getAssignmentMenuCategoryForCourse(String course) {
        return prefs.getString(String.format(KEY_COURSE_ASSIGNMENT_MENU_CATEGORY, course), null);
    }

    @Override
    public void setAssignmentMenuCategoryForCourse(String course, String category) {
        prefs.edit().putString(String.format(KEY_COURSE_ASSIGNMENT_MENU_CATEGORY, course), category).apply();
    }

    @Override
    public int getScheduleRememberedTypeSelection() {
        return prefs.getInt(KEY_SCHEDULE_REMEMBERED_TYPE_SELECTION, 0);
    }

    @Override
    public void setScheduleRememberedTypeSelection(int selection) {
        prefs.edit().putInt(KEY_SCHEDULE_REMEMBERED_TYPE_SELECTION, selection).apply();
    }

    @Override
    public String getGoogleCalendarIdForUser(String user) {
        return prefs.getString(String.format(KEY_GCALENDAR_ID_FOR_USER, user), null);
    }

    @Override
    public void setGoogleCalendarIdForUser(String user, String id) {
        prefs.edit().putString(String.format(KEY_GCALENDAR_ID_FOR_USER, user), id).apply();
    }

}
