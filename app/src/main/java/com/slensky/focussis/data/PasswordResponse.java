package com.slensky.focussis.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by slensky on 4/5/18.
 */

public class PasswordResponse {
    private final static String TAG = "PasswordResponse";

    private final boolean success;
    public enum Error {
        CURRENT_PASSWORD_INCORRECT,
        PASSWORDS_DONT_MATCH,
        IS_DEBUG_API,
        OTHER
    }
    private final Error error;

    public PasswordResponse(boolean success, Error error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public Error getError() {
        return error;
    }

}
