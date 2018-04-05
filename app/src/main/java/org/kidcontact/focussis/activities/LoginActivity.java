package org.kidcontact.focussis.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.check_remember) CheckBox _saveLoginCheckBox;

    private SharedPreferences loginPrefs;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.login_layout);
        rl.setPadding(rl.getPaddingLeft(), getWindowManager().getDefaultDisplay().getHeight() / 6, rl.getPaddingRight(), rl.getPaddingBottom());

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    login();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
    }

    public void login() throws JSONException {
        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, null, getString(R.string.auth_progress_dialog),true);

        String tempUsername = _usernameText.getText().toString();
        if (tempUsername.contains("@")) {
            tempUsername = tempUsername.split("@")[0];
        }

        final String username = tempUsername;
        final String password = _passwordText.getText().toString();

        final FocusApi api = new FocusApi(username, password, this);
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

//        final RequestQueue queue = RequestSingleton.getInstance(this).getRequestQueue();
//        JSONObject data = new JSONObject().put(getString(R.string.key_login_username), username).put(getString(R.string.key_login_password), password);
//        JsonObjectRequest loginRequest = new JsonObjectRequest
//                (Request.Method.POST, ApiBuilder.getSessionUrl(), data, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.d(TAG, "Login successful");
//                        if (_saveLoginCheckBox.isChecked()) {
//                            Log.i(TAG, "Remembering user " + username);
//                            loginPrefsEditor.putBoolean(getString(R.string.login_prefs_save_login), true);
//                            loginPrefsEditor.putString(getString(R.string.login_prefs_username), username);
//                            loginPrefsEditor.putString(getString(R.string.login_prefs_password), password);
//                            loginPrefsEditor.commit();
//                        } else {
//                            Log.i(TAG, "Remember me not checked, clearing prefs");
//                            loginPrefsEditor.clear();
//                            loginPrefsEditor.commit();
//                        }
//                        progressDialog.hide();
//                        progressDialog.dismiss();
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        intent.putExtra(getString(R.string.EXTRA_USERNAME), username);
//                        intent.putExtra(getString(R.string.EXTRA_PASSWORD), password);
//                        startActivity(intent);
//                        finish();
//
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        progressDialog.hide();
//                        Log.i(TAG, "Login failed");
//                        if (error.networkResponse != null) {
//                            if (error.networkResponse.statusCode == 401) {
//                                onLoginFailed(getString(R.string.network_error_auth));
//                                _passwordText.setText("");
//                            }
//                            else if (error.networkResponse.statusCode == 500) {
//                                onLoginFailed(getString(R.string.network_error_server));
//                            }
//                            else {
//                                onLoginFailed(getString(R.string.network_error_timeout));
//                            }
//                        }
//                        else {
//                            onLoginFailed(getString(R.string.network_error_timeout));
//                        }
//                    }
//                });
//        Log.i(TAG, "Sending request to login");
//        queue.add(loginRequest);

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