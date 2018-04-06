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
        OTHER
    }
    private final Error error;

    public PasswordResponse(JSONObject json) {
        boolean success = false;
        try {
            success = json.getBoolean("success");
        } catch (JSONException e) {
            Log.e(TAG, "Success not found in password response json");
            e.printStackTrace();
        }
        this.success = success;

        String error = "other";
        try {
            if (!json.has("error")) {
                error = null;
            }
            else {
                error = json.getString("error");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error not found in password response json");
            e.printStackTrace();
        }

        if (error == null) {
            this.error = null;
        }
        else {
            switch (error) {
                case "current_password_incorrect":
                    this.error = Error.CURRENT_PASSWORD_INCORRECT;
                    break;
                case "passwords_dont_match":
                    this.error = Error.PASSWORDS_DONT_MATCH;
                    break;
                default:
                    this.error = Error.OTHER;
                    break;
            }
        }

    }

    public boolean isSuccess() {
        return success;
    }

    public Error getError() {
        return error;
    }

}
