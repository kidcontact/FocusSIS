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

    public abstract void refresh();/* {
        requestFinished = false;
        networkFailed = false;
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onError(error);
                    }
                });
        configureRequest(request);
        RequestSingleton.getInstance(getContext()).addToRequestQueue(request);
    }*/

    public void onFragmentLoad() {

    }

//    public void configureRequest(JsonObjectRequest request) {
//
//    }

}
