package com.slensky.focussis.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.acra.dialog.BaseCrashReportDialog;

/**
 * Created by slensky on 3/31/18.
 */

public class CrashReportActivity extends BaseCrashReportDialog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        setTheme(com.slensky.focussis.R.style.AppTheme_Light);
        super.init(savedInstanceState);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(com.slensky.focussis.R.string.acra_dialog_title)
                .setMessage(com.slensky.focussis.R.string.acra_dialog_text)
                .setPositiveButton(com.slensky.focussis.R.string.acra_dialog_positive_button, this)
                .setNegativeButton(com.slensky.focussis.R.string.acra_dialog_negative_button, this)
                .create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            sendCrash(null, null);
        } else {
            cancelReports();
        }
        finish();
    }

}
