package com.ecotioco.gaze;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.data.SharedPref;
import com.ecotioco.gaze.fcm.FcmMessagingService;
import com.ecotioco.gaze.model.DeviceInfo;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.ecotioco.gaze.utils.Tools;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;

public class ThisApplication extends MultiDexApplication {

    private static ThisApplication mInstance;
    private SharedPref sharedPref;
    private FirebaseAnalytics mFirebaseAnalytics;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            obtainFirebaseToken();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        sharedPref = new SharedPref(this);

        // obtain regId & registering device to server
        obtainFirebaseToken();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public static synchronized ThisApplication getInstance() {
        return mInstance;
    }

    private void obtainFirebaseToken() {
        String token = FcmMessagingService.getToken(this);
        if (TextUtils.isEmpty(token)) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 10 * 1000);
        } else {
//            sendRegistrationToServer(token);
        }
    }

    private void sendRegistrationToServer(String token) {
        Log.d("FCM_TOKEN", token + "");
        if (NetworkCheck.isConnect(this) && !TextUtils.isEmpty(token) && sharedPref.isOpenAppCounterReach()) {
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.device = Tools.getDeviceName();
            deviceInfo.osVersion = Tools.getAndroidVersion();
            deviceInfo.appVersion = Tools.getVersionCode(this) + " (" + Tools.getVersionNamePlain(this) + ")";
//            deviceInfo.serial = Tools.getDeviceID(getApplicationContext());
            deviceInfo.regId = token;

            GazeAPI api = GazeAPI.getInstance(this);
            VolleyCallback<JSONObject> callback = new VolleyCallback<JSONObject>() {
                @Override
                public void handleResponse(JSONObject result) {
                    if (result != null) {
                        sharedPref.setOpenAppCounter(0);
                    }
                }

                @Override
                public void handleError(VolleyError error) {

                }
            };
            JsonObjectRequest request = api.registerDevice(deviceInfo, callback);
        }
    }

    public void saveLogEvent(long id, String name, String type) {
        Bundle bundle = new Bundle();
        bundle.putLong(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void saveCustomLogEvent(String event, String key, String value) {
        Bundle params = new Bundle();
        params.putString(key, value);
        mFirebaseAnalytics.logEvent(event, params);
    }
}
