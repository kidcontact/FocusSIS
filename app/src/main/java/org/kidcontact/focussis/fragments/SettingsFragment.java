package org.kidcontact.focussis.fragments;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import org.kidcontact.focussis.R;
import org.kidcontact.focussis.activities.MainActivity;
import org.kidcontact.focussis.views.PasswordChangePreference;
import org.kidcontact.focussis.views.PasswordChangePreferenceDialogFragmentCompat;

/**
 * Created by slensky on 4/5/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements PageFragment {

    public SettingsFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Preference usernameDisplay = getPreferenceManager().findPreference("username_display");
        usernameDisplay.setSummary(((MainActivity) getActivity()).getUsername());

        return view;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof PasswordChangePreference) {
            dialogFragment = PasswordChangePreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        }

        // If it was one of our custom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }
        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.settings_label);
    }

}
