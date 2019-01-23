package com.slensky.focussis.ui.settings;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.slensky.focussis.FocusApplication;
import com.slensky.focussis.data.focus.PasswordResponse;
import com.slensky.focussis.data.network.FocusApi;
import com.slensky.focussis.data.prefs.PreferencesHelper;
import com.slensky.focussis.di.ApplicationContext;

import java.util.Objects;

import javax.inject.Inject;

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

    @Inject FocusApi api;
    @Inject @ApplicationContext Context context;
    @Inject PreferencesHelper preferencesHelper;

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
        ((FocusApplication) Objects.requireNonNull(getActivity()).getApplication()).getComponent().inject(this);
        AlertDialog d = (AlertDialog) getDialog();
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
            final AlertDialog resultDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setPositiveButton(com.slensky.focussis.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            api.changePassword(currentPassword.getText().toString(),
                    newPassword.getText().toString(),
                    verifyNewPassword.getText().toString(),
                    new FocusApi.Listener<PasswordResponse>() {
                        @Override
                        public void onResponse(PasswordResponse passwordResponse) {
                            //resultDialog.show();
                            progressDialog.dismiss();
                            if (passwordResponse.isSuccess()) {
                                preferencesHelper.setSavedPassword(null);
                            }

                            resultDialog.setTitle(passwordResponse.isSuccess()
                                    ? context.getString(com.slensky.focussis.R.string.settings_change_password_success_title)
                                    : context.getString(com.slensky.focussis.R.string.settings_change_password_error_title));
                            resultDialog.setMessage(errorToString(passwordResponse.getError()));
                            resultDialog.show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            resultDialog.setTitle(context.getString(com.slensky.focussis.R.string.settings_change_password_error_title));
                            resultDialog.setMessage(context.getString(com.slensky.focussis.R.string.settings_change_password_network_error));
                            resultDialog.show();
                        }
                    });
        }
    }

    private String errorToString(PasswordResponse.Error error) {
        if (error == null) {
            return context.getString(com.slensky.focussis.R.string.settings_change_password_success_message);
        }
        switch (error) {
            case CURRENT_PASSWORD_INCORRECT:
                return context.getString(com.slensky.focussis.R.string.settings_change_password_incorrect_password);
            case PASSWORDS_DONT_MATCH:
                return context.getString(com.slensky.focussis.R.string.settings_change_password_passwords_dont_match);
            case IS_DEBUG_API:
                return "Password cannot be changed for debug user";
            case OTHER:
                return context.getString(com.slensky.focussis.R.string.settings_change_password_other_error);
        }
        return null;
    }
}
