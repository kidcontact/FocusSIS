package com.slensky.focussis.di.component;

import com.slensky.focussis.data.network.ApiProvider;
import com.slensky.focussis.data.network.FocusNetApi;
import com.slensky.focussis.data.prefs.PreferencesHelper;
import com.slensky.focussis.ui.base.BaseFragment;
import com.slensky.focussis.ui.login.LoginActivity;
import com.slensky.focussis.ui.main.MainActivity;
import com.slensky.focussis.data.network.FocusApi;
import com.slensky.focussis.data.network.FocusDebugApi;
import com.slensky.focussis.di.module.AppModule;
import com.slensky.focussis.di.module.NetModule;
import com.slensky.focussis.ui.portal.course.CourseFragment;
import com.slensky.focussis.ui.base.NetworkFragment;
import com.slensky.focussis.ui.schedule.ScheduleSchoolTabFragment;
import com.slensky.focussis.ui.settings.PasswordChangePreferenceDialogFragmentCompat;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface AppComponent {

    ApiProvider getApiProvider();

    FocusApi getFocusApi();

    PreferencesHelper getPreferencesHelper();

    void inject(FocusApi api);

    void inject(FocusDebugApi api);

    void inject(PasswordChangePreferenceDialogFragmentCompat dialog);

    void inject(NetworkFragment fragment);

    void inject(CourseFragment fragment);

    void inject(ScheduleSchoolTabFragment fragment);

    void inject(FocusNetApi focusNetApi);

    void inject(BaseFragment baseFragment);

}
