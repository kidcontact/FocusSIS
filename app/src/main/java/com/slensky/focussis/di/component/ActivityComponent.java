package com.slensky.focussis.di.component;

import com.slensky.focussis.di.PerActivity;
import com.slensky.focussis.di.module.ActivityModule;
import com.slensky.focussis.ui.about.AboutFragment;
import com.slensky.focussis.ui.login.LoginActivity;
import com.slensky.focussis.ui.main.MainActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(AboutFragment fragment);

    void inject(LoginActivity loginActivity);

    void inject(MainActivity mainActivity);

}
