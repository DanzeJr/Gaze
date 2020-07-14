package com.ecotioco.gaze;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.data.SharedPref;
import com.ecotioco.gaze.model.Info;
import com.ecotioco.gaze.utils.CallbackDialog;
import com.ecotioco.gaze.utils.DialogUtils;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.ecotioco.gaze.utils.PermissionUtil;
import com.ecotioco.gaze.utils.Tools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ActivitySplash extends AppCompatActivity {

    private SharedPref sharedPref;
    private boolean on_permission_result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPref = new SharedPref(this);
        sharedPref.clearInfoData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // permission checker for android M or higher
        if (Tools.needRequestPermission() && !on_permission_result) {
            String[] permission = PermissionUtil.getDeniedPermission(this);
            if (permission.length != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permission, 200);
            } else {
                startProcess();
            }
        } else {
            startProcess();
        }
    }

    private void startProcess() {
        if (!NetworkCheck.isConnect(this)) {
            dialogNoInternet();
        } else {
            requestInfo();
        }
    }

    private void startActivityMainDelay() {
        // Show splash screen for 2 seconds
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent i = new Intent(ActivitySplash.this, MainActivity.class);
                startActivity(i);
                finish(); // kill current activity
            }
        };
        new Timer().schedule(task, 4000);
    }

    private void requestInfo() {
        GazeAPI api = GazeAPI.getInstance(this);
        VolleyCallback<JSONObject> callback = new VolleyCallback<JSONObject>() {
            @Override
            public void handleResponse(JSONObject result) {
                if (result != null) {
                    Info info = new Info(); //sharedPref.setInfoData(GazeAPI.get(result, Info.class));
                    info.active = true;
                    info.currency = "VND";
                    info.tax = 2d;
                    info.shipping.add("Basic Delivery");
                    info.shipping.add("Fast Delivery");
                    sharedPref.setInfoData(info);
                    checkAppVersion(info);
                } else {
                    dialogServerNotConnect();
                }
            }
            @Override
            public void handleError(VolleyError error) {
                dialogServerNotConnect();
            }
        };
        api.getInfo(Tools.getVersionCode(this), callback);
    }

    private void checkAppVersion(Info info) {
//        if (!info.active) {
//            dialogOutDate();
//        } else {
            startActivityMainDelay();
//        }
    }

    public void dialogServerNotConnect() {
        Dialog dialog = new DialogUtils(this).buildDialogWarning(R.string.title_unable_connect, R.string.msg_unable_connect, R.string.TRY_AGAIN, R.string.CLOSE, R.drawable.img_no_connect, new CallbackDialog() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
                retryOpenApplication();
            }

            @Override
            public void onNegativeClick(Dialog dialog) {
                finish();
            }
        });
        dialog.show();
    }

    public void dialogOutDate() {
        Dialog dialog = new DialogUtils(this).buildDialogInfo(R.string.title_info, R.string.msg_app_out_date, R.string.UPDATE, R.drawable.img_app_outdate, new CallbackDialog() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
                Tools.rateAction(ActivitySplash.this);
            }

            @Override
            public void onNegativeClick(Dialog dialog) {
            }
        });
        dialog.show();
    }

    public void dialogNoInternet() {
        Dialog dialog = new DialogUtils(this).buildDialogWarning(R.string.title_no_internet, R.string.msg_no_internet, R.string.TRY_AGAIN, R.string.CLOSE, R.drawable.img_no_internet, new CallbackDialog() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
                retryOpenApplication();
            }

            @Override
            public void onNegativeClick(Dialog dialog) {
                finish();
            }
        });
        dialog.show();
    }

    // make a delay to start next activity
    private void retryOpenApplication() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startProcess();
            }
        }, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 200) {
            for (String perm : permissions) {
                boolean rationale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? shouldShowRequestPermissionRationale(perm) : false;
                sharedPref.setNeverAskAgain(perm, !rationale);
            }
            on_permission_result = true;
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
