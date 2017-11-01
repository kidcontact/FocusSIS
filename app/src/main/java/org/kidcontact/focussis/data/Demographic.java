package org.kidcontact.focussis.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by slensky on 5/15/17.
 */

public class Demographic extends MarkingPeriodPage {

    private static final String TAG = "Demographic";
    private final int arrivalBus;
    private final int dismissalBus;
    private final DateTime birthday;
    private final boolean cumulativeFile;
    private final String email;
    private final boolean forcePasswordChange;
    private final String gender;
    private final int grade;
    private final int id;
    private final int level;
    private final int locker;
    private final String medicalRecordStatus;
    private final String name;
    private final int passwordLength;
    private final boolean photoAuthorized;
    private final Bitmap picture;
    private final String username;

    public Demographic(JSONObject demographicJSON) {
        super(demographicJSON);

        int arrivalBus;
        int dismissalBus;
        DateTime birthday;
        boolean cumulativeFile;
        String email;
        boolean forcePasswordChange;
        String gender;
        int grade;
        int id;
        int level;
        int locker;
        String medicalRecordStatus;
        String name;
        int passwordLength;
        boolean photoAuthorized;
        Bitmap picture;
        String username;

        try {
            arrivalBus = demographicJSON.getInt("arrival_bus");
        } catch (JSONException e) {
            Log.e(TAG, "No arrival bus in JSON");
            arrivalBus = 0;
        }

        try {
            dismissalBus = demographicJSON.getInt("dismissal_bus");
        } catch (JSONException e) {
            Log.e(TAG, "No dismissal bus in JSON");
            dismissalBus = 0;
        }

        try {
            birthday = new DateTime(demographicJSON.getString("birthday"));
        } catch (JSONException e) {
            Log.e(TAG, "No birthday in JSON");
            birthday = null;
        }

        try {
            cumulativeFile = demographicJSON.getBoolean("cumulative_file");
        } catch (JSONException e) {
            Log.e(TAG, "No cumulative file in JSON");
            cumulativeFile = false;
        }

        try {
            email = demographicJSON.getString("email");
        } catch (JSONException e) {
            Log.e(TAG, "No email in JSON");
            email = null;
        }

        try {
            forcePasswordChange = demographicJSON.getBoolean("force_pass_change");
        } catch (JSONException e) {
            Log.e(TAG, "No force pass change found in JSON");
            forcePasswordChange = false;
        }

        try {
            gender = demographicJSON.getString("gender");
            gender = Character.toUpperCase(gender.charAt(0)) + gender.substring(1);
        } catch (JSONException e) {
            Log.e(TAG, "No gender found in JSON");
            gender = null;
        }

        try {
            grade = demographicJSON.getInt("grade");
        } catch (JSONException e) {
            Log.e(TAG, "No grade found in JSON");
            grade = 0;
        }

        try {
            id = demographicJSON.getInt("id");
        } catch (JSONException e) {
            Log.e(TAG, "No id found in JSON");
            id = 0;
        }

        try {
            level = demographicJSON.getInt("level");
        } catch (JSONException e) {
            Log.e(TAG, "No level found in JSON");
            level = 0;
        }

        try {
            locker = demographicJSON.getInt("locker");
        } catch (JSONException e) {
            Log.e(TAG, "No locker found in JSON");
            locker = 0;
        }

        try {
            medicalRecordStatus = demographicJSON.getString("medical_record_status");
            medicalRecordStatus = Character.toUpperCase(medicalRecordStatus.charAt(0)) + medicalRecordStatus.substring(1);
        } catch (JSONException e) {
            Log.e(TAG, "No medical record found in JSON");
            medicalRecordStatus = null;
        }

        try {
            name = demographicJSON.getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "No name found in JSON");
            name = null;
        }

        try {
            passwordLength = demographicJSON.getInt("pass_length");
        } catch (JSONException e) {
            Log.e(TAG, "No password length found in JSON");
            passwordLength = 0;
        }

        try {
            photoAuthorized = demographicJSON.getBoolean("photo_auth");
        } catch (JSONException e) {
            Log.e(TAG, "No photo auth found in JSON");
            photoAuthorized = false;
        }

        try {
            byte[] decoded = Base64.decode(demographicJSON.getString("picture"), Base64.DEFAULT);
            picture = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (JSONException e) {
            Log.e(TAG, "No picture found in JSON");
            picture = null;
        }

        try {
            username = demographicJSON.getString("username");
        } catch (JSONException e) {
            Log.e(TAG, "username not found in JSON");
            username = null;
        }

        this.arrivalBus = arrivalBus;
        this.dismissalBus = dismissalBus;
        this.birthday = birthday;
        this.cumulativeFile = cumulativeFile;
        this.email = email;
        this.forcePasswordChange = forcePasswordChange;
        this.gender = gender;
        this.grade = grade;
        this.id = id;
        this.level = level;
        this.locker = locker;
        this.medicalRecordStatus = medicalRecordStatus;
        this.name = name;
        this.passwordLength = passwordLength;
        this.photoAuthorized = photoAuthorized;
        this.picture = picture;
        this.username = username;

    }

    public int getArrivalBus() {
        return arrivalBus;
    }

    public int getDismissalBus() {
        return dismissalBus;
    }

    public DateTime getBirthday() {
        return birthday;
    }

    public boolean isCumulativeFile() {
        return cumulativeFile;
    }

    public String getEmail() {
        return email;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public String getGender() {
        return gender;
    }

    public int getGrade() {
        return grade;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getLocker() {
        return locker;
    }

    public String getMedicalRecordStatus() {
        return medicalRecordStatus;
    }

    public String getName() {
        return name;
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public boolean isPhotoAuthorized() {
        return photoAuthorized;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public String getUsername() {
        return username;
    }
}
