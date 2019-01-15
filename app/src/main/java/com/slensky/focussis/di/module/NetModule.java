package com.slensky.focussis.di.module;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.slensky.focussis.FocusApplication;
import com.slensky.focussis.data.network.ApiProvider;
import com.slensky.focussis.data.network.FocusApi;
import com.slensky.focussis.data.network.FocusDebugApi;

import java.net.CookieHandler;
import java.net.CookieManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        Converters.registerDateTime(gsonBuilder);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    CookieManager provideCookieManager() {
        CookieManager cm = new CookieManager();
        CookieHandler.setDefault(cm);
        return cm;
    }

    @Provides
    @Singleton
    RequestQueue provideRequestQueue(Application application) {
        return Volley.newRequestQueue(application);
    }

    @Provides
    @Singleton
    ApiProvider provideApiProvider(Application application) {
        ApiProvider p = new ApiProvider();
        p.setApi(new FocusApi(application));
        return p;
    }

    @Provides
    @Singleton
    FocusApi provideFocusApi(ApiProvider provider) {
        return provider.getApi();
    }

}
