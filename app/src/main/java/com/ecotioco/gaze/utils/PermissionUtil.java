package com.ecotioco.gaze.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {

    public static final String STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /* Permission required for application */
    public static final String[] PERMISSION_ALL = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void goToPermissionSettingScreen(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public static boolean isAllPermissionGranted(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permission = PERMISSION_ALL;
            if (permission.length == 0) return false;
            for (String s : permission) {
                if (ActivityCompat.checkSelfPermission(ctx, s) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String[] getDeniedPermission(Context ctx) {
        List<String> permissions = new ArrayList<>();
        for (int i = 0; i < PERMISSION_ALL.length; i++) {
            int status = PackageManager.PERMISSION_GRANTED;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                status = ctx.checkSelfPermission(PERMISSION_ALL[i]);
            }
            if (status != PackageManager.PERMISSION_GRANTED) {
                permissions.add(PERMISSION_ALL[i]);
            }
        }

        return permissions.toArray(new String[permissions.size()]);
    }


    public static boolean isGranted(Context ctx, String permission) {
        if (!Tools.needRequestPermission()) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (ctx.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }

    public static boolean isStorageGranted(Context ctx) {
        return isGranted(ctx, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
}
