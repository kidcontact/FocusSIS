package com.slensky.focussis;

import android.app.Application;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import com.slensky.focussis.activities.CrashReportActivity;

/**
 * Created by slensky on 3/30/18.
 */

@ReportsCrashes(mailTo = "focusbugreports@slensky.com",
    mode = ReportingInteractionMode.DIALOG,
    reportDialogClass = CrashReportActivity.class)
public class FocusApplication extends Application {

    public static boolean USE_DEBUG_API = false;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }

}
