package com.slensky.focussis.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.android.volley.VolleyError;
import com.slensky.focussis.network.FocusApi;

import org.json.JSONObject;

/**
 * Created by slensky on 4/22/17.
 */

public abstract class NetworkFragment extends Fragment {
    private static final String TAG = "NetworkFragment";

    protected boolean networkFailed = false;
    protected VolleyError networkError;
    protected volatile boolean requestFinished = false;
    protected FocusApi api;

    public NetworkFragment() {
        // required empty constructor
    }

    protected abstract void onSuccess(JSONObject response);
    protected void onError(VolleyError error) {
        Log.d("NetworkFragment", error.toString());
        requestFinished = true;
        networkFailed = true;
        networkError = error;
    }
    public boolean hasNetworkError() {
        return networkFailed;
    }
    public VolleyError getNetworkError() {
        return networkError;
    }
    public boolean isRequestFinished() {
        return requestFinished;
    }

    public void refresh() {
        requestFinished = false;
        networkFailed = false;
        Thread waitForLogin = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Waiting for API to be logged in before making request");
                while (api.isSessionExpired() || !api.isLoggedIn()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Making request");
                makeRequest();
            }
        });
        waitForLogin.start();
    }

    protected abstract void makeRequest();

    public void onFragmentLoad() {

    }

//    public void configureRequest(JsonObjectRequest request) {
//
//    }

}
