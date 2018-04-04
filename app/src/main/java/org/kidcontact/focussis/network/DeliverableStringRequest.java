package org.kidcontact.focussis.network;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

/**
 * Created by slensky on 4/1/18.
 */

public class DeliverableStringRequest extends StringRequest {

    public DeliverableStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public DeliverableStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    @Override
    public void deliverResponse(String response) {
        super.deliverResponse(response);
    }

}
