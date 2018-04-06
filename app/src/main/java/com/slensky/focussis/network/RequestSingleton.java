package com.slensky.focussis.network;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.net.CookieHandler;
import java.net.CookieManager;

/**
 * Created by slensky on 3/20/17.
 */

public class RequestSingleton {
    private static RequestSingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;
    private static CookieManager manager = new CookieManager();

    private RequestSingleton(Context context) {
        ctx = context;
        CookieHandler.setDefault(manager);
        requestQueue = getRequestQueue();
    }

    public static synchronized RequestSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new RequestSingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static CookieManager getCookieManager() {
        return manager;
    }

}
