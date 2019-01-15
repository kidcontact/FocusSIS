package com.slensky.focussis.di.component;

import com.slensky.focussis.activities.LoginActivity;
import com.slensky.focussis.activities.MainActivity;
import com.slensky.focussis.data.network.FocusApi;
import com.slensky.focussis.data.network.FocusDebugApi;
import com.slensky.focussis.di.module.AppModule;
import com.slensky.focussis.di.module.NetModule;
import com.slensky.focussis.fragments.NetworkFragment;
import com.slensky.focussis.views.PasswordChangePreferenceDialogFragmentCompat;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface AppComponent {
    void inject(FocusApi api);
    void inject(FocusDebugApi api);
    void inject(LoginActivity activity);
    void inject(MainActivity activity);
    void inject(PasswordChangePreferenceDialogFragmentCompat dialog);
    void inject(NetworkFragment fragment);
}
