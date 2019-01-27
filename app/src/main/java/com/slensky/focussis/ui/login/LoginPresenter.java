package com.slensky.focussis.ui.login;

import android.util.Log;

import com.android.volley.VolleyError;
import com.slensky.focussis.data.focus.FocusPreferences;
import com.slensky.focussis.data.network.ApiProvider;
import com.slensky.focussis.data.prefs.PreferencesHelper;
import com.slensky.focussis.ui.base.BasePresenter;

import javax.inject.Inject;

public class LoginPresenter
        extends BasePresenter<LoginContract.ViewActions>
        implements LoginContract.UserActions<LoginContract.ViewActions> {

    private static final String TAG = "LoginPresenter";

    // count the number of times the login has failed due to invalid credentials
    // if it has failed too many times, show a different message asking if the user is from ASD
    private int authErrors;

    // store the last retrieved FocusPreferences after checking the language
    // that way, if the language needs to be set to english, all other preferences can be kept as they were
    private FocusPreferences focusPreferences;

    private PreferencesHelper preferencesHelper;
    private ApiProvider apiProvider;

    @Inject
    public LoginPresenter(PreferencesHelper preferencesHelper, ApiProvider apiProvider) {
        this.preferencesHelper = preferencesHelper;
        this.apiProvider = apiProvider;
    }

    @Override
    public void onAttach(LoginContract.ViewActions view) {
        super.onAttach(view);
        authErrors = 0;
        preferencesHelper.setUseDebugApi(false);
        if (preferencesHelper.getSaveLogin()) {
            String user = preferencesHelper.getSavedUsername();
            String pass = preferencesHelper.getSavedPassword();
            if (user != null) {
                view.setUsername(user);
            }
            if (pass != null) {
                view.setPassword(pass);
            }
            view.setRememberMe(true);

            if (preferencesHelper.getAutomaticLogin() && user != null && pass != null && !view.isDisableAutoSignIn()) {
                onLogin();
                preferencesHelper.setReadSchoolMessage(true); // if a user has already succesfully logged in, we can assume they don't need to read this message
            }
        }

        if (!preferencesHelper.getReadSchoolMessage()) {
            view.showSchoolMessage();
        }
    }

    @Override
    public void onLogin() {
        onLogin(preferencesHelper.checkPreferencesOnLogin());
    }

    private void onLogin(boolean checkPreferences) {
        String username = view.getUsername().split("@")[0];
        String password = view.getPassword();

        view.setUsernameBlankError(username.isEmpty());
        view.setPasswordBlankError(password.isEmpty());
        if (username.isEmpty() || password.isEmpty()) {
            return;
        }

        apiProvider.setUseDebugApi(view.checkIsDebugCredentials(username, password));
        if (apiProvider.isUseDebugApi()) {
            Log.d(TAG, "Using debug API");
            preferencesHelper.setUseDebugApi(true);
        } else {
            preferencesHelper.setUseDebugApi(false);
        }

        view.showAuthenticatingProgress();
        apiProvider.getApi().login(username, password, response -> {
            if (isViewAttached()) {
                if (response) {
                    Log.d(TAG, "Login successful");
                    if (view.getRememberMe()) {
                        Log.d(TAG, "Remembering user " + username);
                        preferencesHelper.setSaveLogin(true);
                        preferencesHelper.setSavedUsername(username);
                        preferencesHelper.setSavedPassword(password);
                    } else {
                        preferencesHelper.setSaveLogin(false);
                        preferencesHelper.setSavedUsername(null);
                        preferencesHelper.setSavedPassword(null);
                    }

                    if (checkPreferences) {
                        doLanguageCheck();
                        return;
                    }

                    view.startMainActivity(username, password);
                } else {
                    view.hideAuthenticatingProgress();
                    view.setPassword("");
                    showAuthError();
                }
            }
        }, error -> {
            if (isViewAttached()) {
                view.hideAuthenticatingProgress();
                showNetworkErrorFor(error);
            }
        });
    }

    private void showNetworkErrorFor(VolleyError error) {
        Log.d(TAG, "Login failed (network error)");
        if (isViewAttached()) {
            if (error.networkResponse != null) {
                if (error.networkResponse.statusCode == 500) {
                    view.showServerError();
                }
                // handle other error codes here if necessary
                return;
            }
            view.showTimeoutError();
        }
    }

    private void showAuthError() {
        if (isViewAttached()) {
            authErrors++;
            if (authErrors < 3) {
                view.showAuthError();
            } else {
                view.showSchoolVerificationAuthError();
            }
        }
    }

    @Override
    public void onReadSchoolMessage() {
        preferencesHelper.setReadSchoolMessage(true);
    }

    private void doLanguageCheck() {
        apiProvider.getApi().getPreferences(preferences -> {
            if (isViewAttached()) {
                focusPreferences = preferences;
                view.hideAuthenticatingProgress();
                if (preferences.isEnglishLanguage()) {
                    view.startMainActivity(view.getUsername(), view.getPassword());
                } else {
                    view.showLanguageError();
                }
            }
        }, error -> {
            if (isViewAttached()) {
                view.hideAuthenticatingProgress();
                showNetworkErrorFor(error);
            }
        });
    }

    @Override
    public void onChangeLanguageToEnglish() {
        if (focusPreferences == null) {
            focusPreferences = new FocusPreferences(true);
        }
        focusPreferences.setEnglishLanguage(true);
        view.showLanguageChangeProgress();
        apiProvider.getApi().setPreferences(focusPreferences, response -> {
            if (isViewAttached()) {
                onLogin(false);
            }
        }, error -> {
            if (isViewAttached()) {
                view.hideLanguageChangeProgress();
                view.hideAuthenticatingProgress();
                showNetworkErrorFor(error);
            }
        });
    }

}
