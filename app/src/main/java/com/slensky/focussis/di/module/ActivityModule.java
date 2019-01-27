package com.slensky.focussis.di.module;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.slensky.focussis.di.ActivityContext;
import com.slensky.focussis.di.ApplicationContext;
import com.slensky.focussis.ui.about.AboutContract;
import com.slensky.focussis.ui.about.AboutPresenter;
import com.slensky.focussis.ui.login.LoginContract;
import com.slensky.focussis.ui.login.LoginPresenter;
import com.slensky.focussis.ui.main.MainContract;
import com.slensky.focussis.ui.main.MainPresenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.inject.Singleton;

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
    @Singleton
    GoogleSignInClient provideGoogleSignInClient(@ActivityContext Context context) {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(context, googleSignInOptions);
    }

    @Provides
    @Singleton
    GoogleAccountCredential provideGoogleAccountCredential(@ApplicationContext Context context) {
        return GoogleAccountCredential.usingOAuth2(
                context, Collections.singletonList(CalendarScopes.CALENDAR))
                .setBackOff(new ExponentialBackOff() {
                    int tries = 0;
                    @Override
                    public long nextBackOffMillis() throws IOException {
                        tries += 1;
                        if (tries < 3) {
                            return super.nextBackOffMillis();
                        }
                        else {
                            return BackOff.STOP;
                        }
                    }
                });
    }

    @Provides
    AboutContract.UserActions<AboutContract.ViewActions> provideAboutUserActions(AboutPresenter presenter) {
        return presenter;
    }

    @Provides
    LoginContract.UserActions<LoginContract.ViewActions> provideLoginUserActions(LoginPresenter presenter) {
        return presenter;
    }

    @Provides
    MainContract.UserActions<MainContract.ViewActions> provideMainUserActions(MainPresenter presenter) {
        return presenter;
    }

}
