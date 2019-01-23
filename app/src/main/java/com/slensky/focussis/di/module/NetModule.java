package com.slensky.focussis.di.module;

import android.content.Context;

import androidx.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.slensky.focussis.data.network.ApiProvider;
import com.slensky.focussis.data.network.FocusApi;
import com.slensky.focussis.data.network.FocusDebugApi;
import com.slensky.focussis.data.network.FocusNetApi;
import com.slensky.focussis.di.ApplicationContext;

import java.net.CookieHandler;
import java.net.CookieManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetModule {

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
    RequestQueue provideRequestQueue(@ApplicationContext Context context) {
        return Volley.newRequestQueue(context);
    }

    @Provides
    @Singleton
    ApiProvider provideApiProvider(@ApplicationContext Context context) {
        return new ApiProvider(new FocusNetApi(context), new FocusDebugApi(context));
    }

    @Provides
    FocusApi provideFocusApi(ApiProvider provider) {
        return provider.getApi();
    }

}
