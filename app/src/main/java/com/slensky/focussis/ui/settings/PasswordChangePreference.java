package com.slensky.focussis.ui.settings;

import android.content.Context;
import androidx.preference.DialogPreference;
import android.util.AttributeSet;

import com.slensky.focussis.R;

/**
 * Created by slensky on 4/5/18.
 */

public class PasswordChangePreference extends DialogPreference {

    public PasswordChangePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    public PasswordChangePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public PasswordChangePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public PasswordChangePreference(Context context) {
        this(context, null);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_password_change;
    }

}
