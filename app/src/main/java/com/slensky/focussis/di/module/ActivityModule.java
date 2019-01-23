package com.slensky.focussis.di.module;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import com.slensky.focussis.di.ActivityContext;
import com.slensky.focussis.ui.about.AboutContract;
import com.slensky.focussis.ui.about.AboutPresenter;
import com.slensky.focussis.ui.login.LoginContract;
import com.slensky.focussis.ui.login.LoginPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private AppCompatActivity activity;

    public ActivityModule(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityContext
    Context provideContext() {
        return activity;
    }

    @Provides
    AboutContract.UserActions<AboutContract.ViewActions> provideAboutUserActions(AboutPresenter presenter) {
        return presenter;
    }

    @Provides
    LoginContract.UserActions<LoginContract.ViewActions> provideLoginUserActions(LoginPresenter presenter) {
        return presenter;
    }

}
