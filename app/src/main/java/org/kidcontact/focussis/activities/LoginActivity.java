package org.kidcontact.focussis.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.kidcontact.focussis.data.FocusPreferences;
import org.kidcontact.focussis.network.ApiBuilder;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.network.FocusApi;
import org.kidcontact.focussis.network.FocusApiSingleton;
import org.kidcontact.focussis.network.RequestSingleton;

import butterknife.ButterKnife;
import butterknife.BindView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @BindView(R.id.input_username) EditText _usernameText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.text_layout_email) TextInputLayout _usernameLayout;
    @BindView(R.id.text_layout_password) TextInputLayout _passwordLayout;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.check_remember) CheckBox _saveLoginCheckBox;

    private SharedPreferences loginPrefs;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    private AlertDialog languageErrorDialog;
    private FocusPreferences focusPreferences;

    private FocusApi api;

    private SharedPreferences defaultSharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
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

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Intent intent = getIntent();

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.login_layout);
        rl.setPadding(rl.getPaddingLeft(), getWindowManager().getDefaultDisplay().getHeight() / 6, rl.getPaddingRight(), rl.getPaddingBottom());

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        loginPrefs = getSharedPreferences(getString(R.string.login_prefs), MODE_PRIVATE);
        loginPrefsEditor = loginPrefs.edit();
        saveLogin = loginPrefs.getBoolean(getString(R.string.login_prefs_save_login), false);
        if (saveLogin) {
            _usernameText.setText(loginPrefs.getString(getString(R.string.login_prefs_username), ""));
            _passwordText.setText(loginPrefs.getString(getString(R.string.login_prefs_password), ""));
            _saveLoginCheckBox.setChecked(true);
        }

        languageErrorDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.language_alert_title)
                .setMessage(R.string.language_alert_message)
                .setPositiveButton(R.string.language_alert_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, null, getString(R.string.language_change_progress),true);
                        focusPreferences.setEnglishLanguage(true);
                        api.setPreferences(focusPreferences, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                progressDialog.hide();
                                progressDialog.dismiss();
                                login();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.hide();
                                progressDialog.dismiss();
                                onLoginFailed(getString(R.string.network_error_timeout));
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.language_alert_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();

        defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (saveLogin && defaultSharedPrefs.getBoolean("automatic_login", false)
                && !intent.getBooleanExtra(getString(R.string.EXTRA_DISABLE_AUTO_SIGN_IN), false)
                && loginPrefs.getString(getString(R.string.login_prefs_password), null) != null) { // occurs after password change
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

        boolean attemptLogin = true;
        if (username.isEmpty()) {
            _usernameLayout.setError(getString(R.string.login_blank_username_error));
            attemptLogin = false;
        }
        else {
            _usernameLayout.setErrorEnabled(false);
        }

        if (password.isEmpty()) {
            _passwordLayout.setError(getString(R.string.login_blank_password_error));
            attemptLogin = false;
        }
        else {
            _passwordLayout.setErrorEnabled(false);
        }

        if (!attemptLogin) {
            _loginButton.setEnabled(true);
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, null, getString(R.string.auth_progress_dialog),true);

        api = new FocusApi(username, password, this);
        api.login(new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if (response) {
                    Log.d(TAG, "Login successful");
                    if (_saveLoginCheckBox.isChecked()) {
                        Log.i(TAG, "Remembering user " + username);
                        loginPrefsEditor.putBoolean(getString(R.string.login_prefs_save_login), true);
                        loginPrefsEditor.putString(getString(R.string.login_prefs_username), username);
                        loginPrefsEditor.putString(getString(R.string.login_prefs_password), password);
                        loginPrefsEditor.commit();
                    } else {
                        Log.i(TAG, "Remember me not checked, clearing prefs");
                        loginPrefsEditor.clear();
                        loginPrefsEditor.commit();
                    }

                    if (defaultSharedPrefs.getBoolean("always_check_preferences", true)) {
                        api.getPreferences(new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                focusPreferences = new FocusPreferences(response);
                                if (focusPreferences.isEnglishLanguage()) {
                                    progressDialog.hide();
                                    progressDialog.dismiss();
                                    FocusApiSingleton.setApi(api);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra(getString(R.string.EXTRA_USERNAME), username);
                                    intent.putExtra(getString(R.string.EXTRA_PASSWORD), password);
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
                                Log.i(TAG, "Getting preferences failed");
                                if (error.networkResponse != null) {
                                    if (error.networkResponse.statusCode == 500) {
                                        onLoginFailed(getString(R.string.network_error_server));
                                    }
                                    else {
                                        onLoginFailed(getString(R.string.network_error_timeout));
                                    }
                                }
                                else {
                                    onLoginFailed(getString(R.string.network_error_timeout));
                                }
                            }
                        });
                    }
                    else {
                        progressDialog.hide();
                        progressDialog.dismiss();
                        FocusApiSingleton.setApi(api);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(getString(R.string.EXTRA_USERNAME), username);
                        intent.putExtra(getString(R.string.EXTRA_PASSWORD), password);
                        startActivity(intent);
                        finish();
                    }
                }
                else {
                    progressDialog.hide();
                    Log.i(TAG, "Login unsuccessful");
                    onLoginFailed(getString(R.string.network_error_auth));
                    _passwordText.setText("");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
                Log.i(TAG, "Login failed");
                if (error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 500) {
                        onLoginFailed(getString(R.string.network_error_server));
                    }
                    else {
                        onLoginFailed(getString(R.string.network_error_timeout));
                    }
                }
                else {
                    onLoginFailed(getString(R.string.network_error_timeout));
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginFailed(final String error) {
        Log.e(TAG, error);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        _loginButton.setEnabled(true);
    }

}