package com.slensky.focussis.data.prefs;

public interface PreferencesHelper {

    // whether or not to save the user's login information ("remember me")
    boolean getSaveLogin();

    void setSaveLogin(boolean saveLogin);

    // saved credentials available if getSaveLogin is true
    String getSavedUsername();

    void setSavedUsername(String username);

    String getSavedPassword();

    void setSavedPassword(String password);

    // whether or not to automatically login when the user opens the app (only works if credentials are saved)
    boolean getAutomaticLogin();

    // whether or not to check the Focus preferences page on login to ensure the language is set to english
    boolean checkPreferencesOnLogin();

    // whether or not the user has read the popup indicating that this app is for ASD only (do not show the popup again after this)
    boolean getReadSchoolMessage();

    void setReadSchoolMessage(boolean readSchoolMessage);

    // whether or not to show custom assignments in the "assignments" portal tab
    boolean getShowCustomAssignmentsInPortal();

    void setShowCustomAssignmentsInPortal(boolean show);

    // remember the last selected category when creating a new course assignment
    String getAssignmentMenuCategoryForCourse(String course);

    void setAssignmentMenuCategoryForCourse(String course, String category);

    // remember the last selected schedule type on the "school" tab of the schedule page
    int getScheduleRememberedTypeSelection();

    void setScheduleRememberedTypeSelection(int selection);

    // ID of the Google Calendar to export events/assignments to, usually set to the user's primary calendar
    String getGoogleCalendarIdForUser(String user);

    void setGoogleCalendarIdForUser(String user, String id);

}
