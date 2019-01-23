package com.slensky.focussis.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.slensky.focussis.R;
import com.slensky.focussis.data.network.FocusDebugApi;
import com.slensky.focussis.ui.base.BaseActivity;
import com.slensky.focussis.ui.main.MainActivity;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class LoginActivity extends BaseActivity implements LoginContract.ViewActions {

    private static final String TAG = "LoginActivity";

    @BindView(R.id.input_username)
    EditText usernameText;
    @BindView(R.id.input_password)
    EditText passwordText;
    @BindView(R.id.text_layout_email)
    TextInputLayout usernameLayout;
    @BindView(R.id.text_layout_password)
    TextInputLayout passwordLayout;
    @BindView(R.id.btn_login)
    Button loginButton;
    @BindView(R.id.check_remember)
    CheckBox rememberMeCheckbox;
    @BindView(R.id.progress_bar)
    SmoothProgressBar progressBar;

    @Inject
    LoginContract.UserActions<LoginContract.ViewActions> presenter;

    private AlertDialog languageErrorDialog;
    private boolean isDisableAutoSignIn;

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

        getActivityComponent().inject(this);

        ButterKnife.bind(this);

        isDisableAutoSignIn = getIntent().getBooleanExtra(getString(com.slensky.focussis.R.string.EXTRA_DISABLE_AUTO_SIGN_IN), false);

        presenter.onAttach(this);

        setupView();
    }

    @Override
    protected void setupView() {
        RelativeLayout rl = findViewById(R.id.login_layout);
        rl.setPadding(rl.getPaddingLeft(), getWindowManager().getDefaultDisplay().getHeight() / 6, rl.getPaddingRight(), rl.getPaddingBottom());
        progressBar.setSmoothProgressDrawableInterpolator(new FastOutSlowInInterpolator());
        progressBar.setSmoothProgressDrawableColors(getResources().getIntArray(R.array.gplus_colors));
        progressBar.setVisibility(View.INVISIBLE);
        enableInputs(true);
    }

    @OnClick(R.id.btn_login)
    void onLoginClick(View v) {
        presenter.onLogin();
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    // show a dialog indicating that the app only works for ASD. Mandatory delay before allowing the user to close it
    public void showSchoolMessage() {
        final AlertDialog schoolMessage = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.login_asd_notice_title))
                .setMessage(getString(R.string.login_asd_notice_message))
                .setNegativeButton(R.string.login_asd_notice_negative, (dialog, which) -> finish()).setPositiveButton(R.string.login_asd_notice_positive, (dialog, which) -> presenter.onReadSchoolMessage()).setCancelable(false)
                .create();

        schoolMessage.show();

        final Button positive = schoolMessage.getButton(AlertDialog.BUTTON_POSITIVE);
        positive.setEnabled(false);

        final CharSequence positiveMessage = positive.getText();
        final Thread waitToEnableButtonThread = new Thread(() -> {
            int waitTime = 6;
            do {
                final int finalWaitTime = waitTime;
                runOnUiThread(() -> positive.setText(positiveMessage + " (" + finalWaitTime + ")"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitTime--;
            } while (waitTime != 0);
            runOnUiThread(() -> {
                positive.setText(positiveMessage);
                positive.setEnabled(true);
            });
        });
        waitToEnableButtonThread.start();
    }

    private void enableInputs(boolean enabled) {
        usernameLayout.setEnabled(enabled);
        passwordLayout.setEnabled(enabled);
        loginButton.setEnabled(enabled);
    }

    @Override
    public void showAuthenticatingProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.progressiveStart();
        enableInputs(false);
    }

    @Override
    public void hideAuthenticatingProgress() {
        progressBar.progressiveStop();
        enableInputs(true);
    }

    @Override
    public void showLanguageChangeProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.progressiveStart();
        enableInputs(false);
    }

    @Override
    public void hideLanguageChangeProgress() {
        progressBar.progressiveStop();
        enableInputs(true);
    }

    @Override
    public String getUsername() {
        return usernameText.getText().toString();
    }

    @Override
    public void setUsername(String username) {
        usernameText.setText(username);
    }

    @Override
    public void setUsernameBlankError(boolean usernameBlankError) {
        if (usernameBlankError) {
            usernameLayout.setError(getString(com.slensky.focussis.R.string.login_blank_username_error));
        } else {
            usernameLayout.setErrorEnabled(false);
        }
    }

    @Override
    public String getPassword() {
        return passwordText.getText().toString();
    }

    @Override
    public void setPassword(String password) {
        passwordText.setText(password);
    }

    @Override
    public void setPasswordBlankError(boolean passwordBlankError) {
        if (passwordBlankError) {
            passwordLayout.setError(getString(com.slensky.focussis.R.string.login_blank_password_error));
        } else {
            passwordLayout.setErrorEnabled(false);
        }
    }

    @Override
    public boolean getRememberMe() {
        return rememberMeCheckbox.isChecked();
    }

    @Override
    public void setRememberMe(boolean saveLogin) {
        rememberMeCheckbox.setChecked(saveLogin);
    }

    @Override
    public boolean isDisableAutoSignIn() {
        return isDisableAutoSignIn;
    }

    @Override
    public void showServerError() {
        Toast.makeText(getBaseContext(), R.string.network_error_server, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showTimeoutError() {
        Toast.makeText(getBaseContext(), R.string.network_error_timeout, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showAuthError() {
        Toast.makeText(getBaseContext(), R.string.network_error_auth, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showSchoolVerificationAuthError() {
        Toast.makeText(getBaseContext(), R.string.network_error_auth_verify_school, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLanguageError() {
        if (languageErrorDialog == null) {
            languageErrorDialog = new AlertDialog.Builder(this)
                    .setTitle(com.slensky.focussis.R.string.language_alert_title)
                    .setMessage(com.slensky.focussis.R.string.language_alert_message)
                    .setPositiveButton(com.slensky.focussis.R.string.language_alert_positive, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        presenter.onChangeLanguageToEnglish();
                    })
                    .setNegativeButton(com.slensky.focussis.R.string.language_alert_negative, (dialogInterface, i) -> dialogInterface.cancel())
                    .create();
        }
        languageErrorDialog.show();
    }

    @Override
    public boolean checkIsDebugCredentials(String user, String pass) {
        return user.equals(getString(R.string.debug_username)) && pass.equals(getString(R.string.debug_password));
    }

    @Override
    public FocusDebugApi getDebugApiInstance() {
        return new FocusDebugApi(this);
    }

    @Override
    public void startMainActivity(String user, String pass) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(getString(com.slensky.focussis.R.string.EXTRA_USERNAME), user);
        intent.putExtra(getString(com.slensky.focussis.R.string.EXTRA_PASSWORD), pass);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        super.onDestroy();
    }

}