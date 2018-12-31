package com.slensky.focussis.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.FocusApplication;
import com.slensky.focussis.R;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.network.FocusApiSingleton;

import org.json.JSONObject;
import com.slensky.focussis.data.FocusPreferences;
import com.slensky.focussis.network.FocusDebugApi;

import butterknife.ButterKnife;
import butterknife.BindView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @BindView(com.slensky.focussis.R.id.input_username) EditText _usernameText;
    @BindView(com.slensky.focussis.R.id.input_password) EditText _passwordText;
    @BindView(com.slensky.focussis.R.id.text_layout_email) TextInputLayout _usernameLayout;
    @BindView(com.slensky.focussis.R.id.text_layout_password) TextInputLayout _passwordLayout;
    @BindView(com.slensky.focussis.R.id.btn_login) Button _loginButton;
    @BindView(com.slensky.focussis.R.id.check_remember) CheckBox _saveLoginCheckBox;

    private SharedPreferences loginPrefs;

    private AlertDialog languageErrorDialog;
    private FocusPreferences focusPreferences;

    private FocusApi api;

    private SharedPreferences defaultSharedPrefs;

    // count the number of times the login has failed due to invalid credentials
    // if it has failed too many times, show a different message ensuring the student is from ASD
    private int authErrors;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(com.slensky.focussis.R.style.AppTheme_Light);
        super.onCreate(savedInstanceState);

        // finish activity and resume MainActivity if the app was already open
        // https://stackoverflow.com/questions/19545889/app-restarts-rather-than-resumes
        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }

        setContentView(com.slensky.focussis.R.layout.activity_login);
        ButterKnife.bind(this);
        loginPrefs = getSharedPreferences(getString(com.slensky.focussis.R.string.login_prefs), MODE_PRIVATE);
        authErrors = 0;
        Intent intent = getIntent();

        // show a dialog indicating that the app only works for ASD. Mandatory delay before allowing the user to close it
        if (!loginPrefs.getBoolean(getString(R.string.login_prefs_read_school_msg), false)) {
            final AlertDialog schoolMessage = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.login_asd_notice_title))
                    .setMessage(getString(R.string.login_asd_notice_message))
                    .setNegativeButton(R.string.login_asd_notice_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setPositiveButton(R.string.login_asd_notice_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
                            loginPrefsEditor.putBoolean(getString(R.string.login_prefs_read_school_msg), true);
                            loginPrefsEditor.apply();
                        }
                    }).setCancelable(false)
                    .create();

            schoolMessage.show();

            final Button positive = schoolMessage.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setEnabled(false);

            final CharSequence positiveMessage = positive.getText();
            final Thread waitToEnableButtonThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int waitTime = 6;
                    while (true) {
                        final int finalWaitTime = waitTime;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                positive.setText(positiveMessage + " (" + finalWaitTime + ")");
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        waitTime--;
                        if (waitTime == 0) {
                            break;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            positive.setText(positiveMessage);
                            positive.setEnabled(true);
                        }
                    });
                }
            });
            waitToEnableButtonThread.start();

        }

        RelativeLayout rl = (RelativeLayout) findViewById(com.slensky.focussis.R.id.login_layout);
        rl.setPadding(rl.getPaddingLeft(), getWindowManager().getDefaultDisplay().getHeight() / 6, rl.getPaddingRight(), rl.getPaddingBottom());

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        Boolean saveLogin = loginPrefs.getBoolean(getString(R.string.login_prefs_save_login), false);
        if (saveLogin) {
            _usernameText.setText(loginPrefs.getString(getString(com.slensky.focussis.R.string.login_prefs_username), ""));
            _passwordText.setText(loginPrefs.getString(getString(com.slensky.focussis.R.string.login_prefs_password), ""));
            _saveLoginCheckBox.setChecked(true);
        }

        languageErrorDialog = new AlertDialog.Builder(this)
                .setTitle(com.slensky.focussis.R.string.language_alert_title)
                .setMessage(com.slensky.focussis.R.string.language_alert_message)
                .setPositiveButton(com.slensky.focussis.R.string.language_alert_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, null, getString(com.slensky.focussis.R.string.language_change_progress),true);
                        focusPreferences.setEnglishLanguage(true);
                        api.setPreferences(focusPreferences, new FocusApi.Listener<FocusPreferences>() {
                            @Override
                            public void onResponse(FocusPreferences response) {
                                progressDialog.hide();
                                progressDialog.dismiss();
                                login();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.hide();
                                progressDialog.dismiss();
                                onLoginFailed(getString(com.slensky.focussis.R.string.network_error_timeout));
                            }
                        });
                    }
                })
                .setNegativeButton(com.slensky.focussis.R.string.language_alert_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();

        defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (saveLogin && defaultSharedPrefs.getBoolean("automatic_login", false)
                && !intent.getBooleanExtra(getString(com.slensky.focussis.R.string.EXTRA_DISABLE_AUTO_SIGN_IN), false)
                && loginPrefs.getString(getString(com.slensky.focussis.R.string.login_prefs_password), null) != null) { // occurs after password change
            login();
        }

    }

    public void login() {
        _loginButton.setEnabled(false);

        String tempUsername = _usernameText.getText().toString();
        if (tempUsername.contains("@")) {
            tempUsername = tempUsername.split("@")[0];
        }

        final String username = tempUsername;
        final String password = _passwordText.getText().toString();

        if (username.equals(getString(R.string.debug_username)) && password.equals(getString(R.string.debug_password))) {
            Log.d(TAG, "Using debug API");
            FocusApplication.USE_DEBUG_API = true;
        }
        else {
            FocusApplication.USE_DEBUG_API = false;
        }

        boolean attemptLogin = true;
        if (username.isEmpty()) {
            _usernameLayout.setError(getString(com.slensky.focussis.R.string.login_blank_username_error));
            attemptLogin = false;
        }
        else {
            _usernameLayout.setErrorEnabled(false);
        }

        if (password.isEmpty()) {
            _passwordLayout.setError(getString(com.slensky.focussis.R.string.login_blank_password_error));
            attemptLogin = false;
        }
        else {
            _passwordLayout.setErrorEnabled(false);
        }

        if (!attemptLogin) {
            _loginButton.setEnabled(true);
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, null, getString(com.slensky.focussis.R.string.auth_progress_dialog),true);

        if (FocusApplication.USE_DEBUG_API) {
            api = new FocusDebugApi(username, password, getApplicationContext());
        }
        else {
            api = new FocusApi(username, password, getApplicationContext());
        }
        api.login(new FocusApi.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if (response) {
                    Log.d(TAG, "Login successful");
                    final SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
                    if (_saveLoginCheckBox.isChecked()) {
                        Log.d(TAG, "Remembering user " + username);
                        loginPrefsEditor.putBoolean(getString(com.slensky.focussis.R.string.login_prefs_save_login), true);
                        loginPrefsEditor.putString(getString(com.slensky.focussis.R.string.login_prefs_username), username);
                        loginPrefsEditor.putString(getString(com.slensky.focussis.R.string.login_prefs_password), password);
                        loginPrefsEditor.apply();
                    } else {
                        loginPrefsEditor.putBoolean(getString(com.slensky.focussis.R.string.login_prefs_save_login), false);
                        loginPrefsEditor.putString(getString(com.slensky.focussis.R.string.login_prefs_username), "");
                        loginPrefsEditor.putString(getString(com.slensky.focussis.R.string.login_prefs_password), "");
                        loginPrefsEditor.apply();
                    }
                    loginPrefsEditor.apply();

                    if (defaultSharedPrefs.getBoolean("always_check_preferences", true)) {
                        api.getPreferences(new FocusApi.Listener<FocusPreferences>() {
                            @Override
                            public void onResponse(FocusPreferences response) {
                                focusPreferences = response;
                                if (focusPreferences.isEnglishLanguage()) {
                                    progressDialog.hide();
                                    progressDialog.dismiss();
                                    FocusApiSingleton.setApi(api);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra(getString(com.slensky.focussis.R.string.EXTRA_USERNAME), username);
                                    intent.putExtra(getString(com.slensky.focussis.R.string.EXTRA_PASSWORD), password);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    progressDialog.hide();
                                    progressDialog.dismiss();
                                    _loginButton.setEnabled(true);
                                    languageErrorDialog.show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.hide();
                                Log.d(TAG, "Getting preferences failed");
                                if (error.networkResponse != null) {
                                    if (error.networkResponse.statusCode == 500) {
                                        onLoginFailed(getString(com.slensky.focussis.R.string.network_error_server));
                                    }
                                    else {
                                        onLoginFailed(getString(com.slensky.focussis.R.string.network_error_timeout));
                                    }
                                }
                                else {
                                    onLoginFailed(getString(com.slensky.focussis.R.string.network_error_timeout));
                                }
                            }
                        });
                    }
                    else {
                        progressDialog.hide();
                        try {
                            progressDialog.dismiss();
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Not attached to window manager, could not dismiss dialog");
                            e.printStackTrace();
                        }
                        FocusApiSingleton.setApi(api);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(getString(com.slensky.focussis.R.string.EXTRA_USERNAME), username);
                        intent.putExtra(getString(com.slensky.focussis.R.string.EXTRA_PASSWORD), password);
                        startActivity(intent);
                        finish();
                    }
                }
                else {
                    progressDialog.hide();
                    Log.d(TAG, "Login unsuccessful");
                    onLoginFailed(getString(com.slensky.focussis.R.string.network_error_auth));
                    _passwordText.setText("");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Log.d(TAG, "Login failed");
                if (error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 500) {
                        onLoginFailed(getString(com.slensky.focussis.R.string.network_error_server));
                    }
                    else {
                        onLoginFailed(getString(com.slensky.focussis.R.string.network_error_timeout));
                    }
                }
                else {
                    onLoginFailed(getString(com.slensky.focussis.R.string.network_error_timeout));
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginFailed(String error) {
        Log.e(TAG, error);
        if (error.equals(getString(R.string.network_error_auth))) {
            authErrors++;
            if (authErrors > 2) {
                error = getString(R.string.network_error_auth_expanded);
            }
        }
        final String finalError = error;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), finalError, Toast.LENGTH_LONG).show();
            }
        });
        _loginButton.setEnabled(true);
    }

}