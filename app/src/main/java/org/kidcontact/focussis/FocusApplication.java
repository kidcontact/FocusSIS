package org.kidcontact.focussis;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.kidcontact.focussis.activities.CrashReportActivity;

/**
 * Created by slensky on 3/30/18.
 */

@ReportsCrashes(mailTo = "focusbugreports@slensky.com",
    mode = ReportingInteractionMode.DIALOG,
    reportDialogClass = CrashReportActivity.class)
public class FocusApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }

}
