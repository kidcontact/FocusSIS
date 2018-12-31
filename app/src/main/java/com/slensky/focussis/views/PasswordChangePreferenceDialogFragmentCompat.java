package com.slensky.focussis.views;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.data.PasswordResponse;
import com.slensky.focussis.network.FocusApi;
import com.slensky.focussis.network.FocusApiSingleton;

import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by slensky on 4/5/18.
 */

public class PasswordChangePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private static final String TAG = "PasswordChangeDialog";

    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout verifyNewPasswordLayout;
    private EditText currentPassword;
    private EditText newPassword;
    private EditText verifyNewPassword;
    private Context ctx;

    public static PasswordChangePreferenceDialogFragmentCompat newInstance(
            String key) {
        final PasswordChangePreferenceDialogFragmentCompat
                fragment = new PasswordChangePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        currentPassword = view.findViewById(com.slensky.focussis.R.id.current_password);
        newPassword = view.findViewById(com.slensky.focussis.R.id.new_password);
        verifyNewPassword = view.findViewById(com.slensky.focussis.R.id.verify_new_password);
        currentPasswordLayout = view.findViewById(com.slensky.focussis.R.id.current_password_wrapper);
        newPasswordLayout = view.findViewById(com.slensky.focussis.R.id.new_password_wrapper);
        verifyNewPasswordLayout = view.findViewById(com.slensky.focussis.R.id.verify_new_password_wrapper);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        this.ctx = getActivity();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean closeDialog = true;
                    if (currentPassword.getText().toString().isEmpty()) {
                        currentPasswordLayout.setError(getString(com.slensky.focussis.R.string.settings_change_password_empty_error));
                        closeDialog = false;
                    }
                    else {
                        currentPasswordLayout.setErrorEnabled(false);
                    }

                    if (newPassword.getText().toString().isEmpty()) {
                        newPasswordLayout.setError(getString(com.slensky.focussis.R.string.settings_change_password_empty_error));
                        closeDialog = false;
                    }
                    else {
                        newPasswordLayout.setErrorEnabled(false);
                    }

                    if (verifyNewPassword.getText().toString().isEmpty()) {
                        verifyNewPasswordLayout.setError(getString(com.slensky.focussis.R.string.settings_change_password_empty_error));
                        closeDialog = false;
                    }
                    else {
                        verifyNewPasswordLayout.setErrorEnabled(false);
                    }

                    if (closeDialog) {
                        onDialogClosed(true);
                        dismiss();
                    }
                }
            });
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(com.slensky.focussis.R.string.settings_change_password_progress_dialog),true);
            final AlertDialog resultDialog = new AlertDialog.Builder(getContext())
                    .setPositiveButton(com.slensky.focussis.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            final SharedPreferences loginPrefs = getContext().getSharedPreferences(getString(com.slensky.focussis.R.string.login_prefs), MODE_PRIVATE);
            final SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
            FocusApiSingleton.getApi().changePassword(currentPassword.getText().toString(),
                    newPassword.getText().toString(),
                    verifyNewPassword.getText().toString(),
                    new FocusApi.Listener<PasswordResponse>() {
                        @Override
                        public void onResponse(PasswordResponse passwordResponse) {
                            //resultDialog.show();
                            progressDialog.dismiss();
                            if (passwordResponse.isSuccess()) {
                                loginPrefsEditor.remove(ctx.getString(com.slensky.focussis.R.string.login_prefs_password));
                                loginPrefsEditor.apply();
                            }

                            resultDialog.setTitle(passwordResponse.isSuccess()
                                    ? ctx.getString(com.slensky.focussis.R.string.settings_change_password_success_title)
                                    : ctx.getString(com.slensky.focussis.R.string.settings_change_password_error_title));
                            resultDialog.setMessage(errorToString(passwordResponse.getError()));
                            resultDialog.show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            resultDialog.setTitle(ctx.getString(com.slensky.focussis.R.string.settings_change_password_error_title));
                            resultDialog.setMessage(ctx.getString(com.slensky.focussis.R.string.settings_change_password_network_error));
                            resultDialog.show();
                        }
                    });
        }
    }

    private String errorToString(PasswordResponse.Error error) {
        if (error == null) {
            return ctx.getString(com.slensky.focussis.R.string.settings_change_password_success_message);
        }
        switch (error) {
            case CURRENT_PASSWORD_INCORRECT:
                return ctx.getString(com.slensky.focussis.R.string.settings_change_password_incorrect_password);
            case PASSWORDS_DONT_MATCH:
                return ctx.getString(com.slensky.focussis.R.string.settings_change_password_passwords_dont_match);
            case IS_DEBUG_API:
                return "Password cannot be changed for debug user";
            case OTHER:
                return ctx.getString(com.slensky.focussis.R.string.settings_change_password_other_error);
        }
        return null;
    }
}
