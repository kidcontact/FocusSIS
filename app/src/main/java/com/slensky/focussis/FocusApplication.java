package com.slensky.focussis;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import com.slensky.focussis.ui.crashreport.CrashReportActivity;
import com.slensky.focussis.di.component.AppComponent;
import com.slensky.focussis.di.component.DaggerAppComponent;
import com.slensky.focussis.di.module.AppModule;
import com.slensky.focussis.di.module.NetModule;

/**
 * Created by slensky on 3/30/18.
 */

@ReportsCrashes(mailTo = "focusbugreports@slensky.com",
    mode = ReportingInteractionMode.DIALOG,
    reportDialogClass = CrashReportActivity.class)
public class FocusApplication extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule())
                .build();
    }

    public AppComponent getComponent() {
        return appComponent;
    }

}
