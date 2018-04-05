package org.kidcontact.focussis.views;

import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;
import org.kidcontact.focussis.FocusApplication;
import org.kidcontact.focussis.R;
import org.kidcontact.focussis.data.PasswordResponse;
import org.kidcontact.focussis.network.FocusApiSingleton;

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
        currentPassword = view.findViewById(R.id.current_password);
        newPassword = view.findViewById(R.id.new_password);
        verifyNewPassword = view.findViewById(R.id.verify_new_password);
        currentPasswordLayout = view.findViewById(R.id.current_password_wrapper);
        newPasswordLayout = view.findViewById(R.id.new_password_wrapper);
        verifyNewPasswordLayout = view.findViewById(R.id.verify_new_password_wrapper);
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
                        currentPasswordLayout.setError(getString(R.string.settings_change_password_empty_error));
                        closeDialog = false;
                    }
                    else {
                        currentPasswordLayout.setErrorEnabled(false);
                    }

                    if (newPassword.getText().toString().isEmpty()) {
                        newPasswordLayout.setError(getString(R.string.settings_change_password_empty_error));
                        closeDialog = false;
                    }
                    else {
                        newPasswordLayout.setErrorEnabled(false);
                    }

                    if (verifyNewPassword.getText().toString().isEmpty()) {
                        verifyNewPasswordLayout.setError(getString(R.string.settings_change_password_empty_error));
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
            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(R.string.settings_change_password_progress_dialog),true);
            final AlertDialog resultDialog = new AlertDialog.Builder(getContext())
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            final SharedPreferences loginPrefs = getContext().getSharedPreferences(getString(R.string.login_prefs), MODE_PRIVATE);
            final SharedPreferences.Editor loginPrefsEditor = loginPrefs.edit();
            FocusApiSingleton.getApi().changePassword(currentPassword.getText().toString(),
                    newPassword.getText().toString(),
                    verifyNewPassword.getText().toString(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //resultDialog.show();
                            progressDialog.dismiss();
                            PasswordResponse passwordResponse = new PasswordResponse(response);
                            if (passwordResponse.isSuccess()) {
                                loginPrefsEditor.remove(ctx.getString(R.string.login_prefs_password));
                                loginPrefsEditor.apply();
                            }

                            resultDialog.setTitle(passwordResponse.isSuccess()
                                    ? ctx.getString(R.string.settings_change_password_success_title)
                                    : ctx.getString(R.string.settings_change_password_error_title));
                            resultDialog.setMessage(errorToString(passwordResponse.getError()));
                            resultDialog.show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            resultDialog.setTitle(ctx.getString(R.string.settings_change_password_error_title));
                            resultDialog.setMessage(ctx.getString(R.string.settings_change_password_network_error));
                            resultDialog.show();
                        }
                    });
        }
    }

    private String errorToString(PasswordResponse.Error error) {
        if (error == null) {
            return ctx.getString(R.string.settings_change_password_success_message);
        }
        switch (error) {
            case CURRENT_PASSWORD_INCORRECT:
                return ctx.getString(R.string.settings_change_password_incorrect_password);
            case PASSWORDS_DONT_MATCH:
                return ctx.getString(R.string.settings_change_password_passwords_dont_match);
            case OTHER:
                return ctx.getString(R.string.settings_change_password_other_error);
        }
        return null;
    }
}
