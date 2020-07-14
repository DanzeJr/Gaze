package com.ecotioco.gaze.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.data.AppConfig;
import com.ecotioco.gaze.data.SharedPref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class Tools {

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static void setSystemBarColor(Activity act, int color) {
        if (isLolipopOrHigher()) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(color);
            }

        }
    }

    public static void setSystemBarColor(Activity act, String color) {
        setSystemBarColor(act, Color.parseColor(color));
    }

    public static void setSystemBarColorDarker(Activity act, String color) {
        setSystemBarColor(act, colorDarker(Color.parseColor(color)));
    }

    public static boolean isLolipopOrHigher() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public static void systemBarLolipop(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(act, R.color.colorPrimaryDark));
        }
    }

    public static void rateAction(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    public static void showDialogAbout(Activity activity) {
        Dialog dialog = new DialogUtils(activity).buildDialogInfo(R.string.title_about, R.string.content_about, R.string.OK, R.drawable.img_about, new CallbackDialog() {
            @Override
            public void onPositiveClick(Dialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void onNegativeClick(Dialog dialog) {
            }
        });
        dialog.show();
    }

    /**
     * For device info parameters
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE + "";
    }

    public static int getVersionCode(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return (int) PackageInfoCompat.getLongVersionCode(info);
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static String getVersionNamePlain(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return ctx.getString(R.string.version_unknown);
        }
    }

    public static String getFormattedDate(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedDate(Date dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm");
        return newFormat.format(dateTime);
    }

    public static String getFormattedDateSimple(Long dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy");
        return newFormat.format(new Date(dateTime));
    }

    public static String getFormattedDateSimple(Date dateTime) {
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy");
        return newFormat.format(dateTime);
    }

    public static String getConvenientFormattedDate(Date dateTime) {
        Calendar inputDate = Calendar.getInstance();
        Calendar currentDate = Calendar.getInstance();
        String prefix = "";
        String pattern = null;

        inputDate.setTimeInMillis(dateTime.getTime());
        if (inputDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
            && inputDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)) {
            if (inputDate.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)) {
                prefix = "Today ";
                pattern = "hh:mm";
            } else if (inputDate.get(Calendar.DAY_OF_MONTH) == (currentDate.get(Calendar.DAY_OF_MONTH) - 1)) {
                prefix = "Yesterday ";
                pattern = "hh:mm";
            }
        }
        if (pattern == null) {
            pattern = "MMM dd, yyyy hh:mm";
        }
        SimpleDateFormat newFormat = new SimpleDateFormat(pattern);
        return prefix + newFormat.format(dateTime);
    }

    public static void displayImageOriginal(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img);
        } catch (Exception e) {
        }
    }

    public static void displayImageThumbnail(Context ctx, ImageView img, String url, float thumb) {
        try {
            Glide.with(ctx).load(url)
                    .transition(withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(thumb)
                    .into(img);
        } catch (Exception e) {

        }

    }


    public static int colorDarker(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int colorBrighter(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] /= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int getGridSpanCount(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth = displayMetrics.widthPixels;
        float cellWidth = activity.getResources().getDimension(R.dimen.item_product_width);
        return Math.round(screenWidth / cellWidth);
    }

    public static int getFeaturedNewsImageHeight(Activity activity) {
        float w_ratio = 2, h_ratio = 1; // we use 2:1 ratio
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        float screenWidth = displayMetrics.widthPixels - 10;
        float resHeight = (screenWidth * h_ratio) / w_ratio;
        return Math.round(resHeight);
    }

    public static void tintMenuIcon(Context context, MenuItem item, @ColorRes int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(context, color));

        item.setIcon(wrapDrawable);
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static Bitmap getBitmap(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static String getVersionName(Context ctx) {
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            return ctx.getString(R.string.app_version) + " " + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return ctx.getString(R.string.version_unknown);
        }
    }

    public static void copyToClipboard(Context context, String data) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clipboard", data);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.msg_copied_clipboard, Toast.LENGTH_SHORT).show();
    }

    public static String getFormattedPrice(Double price, Context ctx) {
        SharedPref sharedPref = new SharedPref(ctx);
        NumberFormat format = NumberFormat.getInstance(AppConfig.PRICE_LOCAL_FORMAT);
        String result = format.format(price);

        if (!AppConfig.PRICE_WITH_DECIMAL) {
            result = format.format(price.longValue());
        }

        if (AppConfig.PRICE_CURRENCY_IN_END) {
            result = result + " " + (sharedPref.getInfoData().currency == null ? "$" : sharedPref.getInfoData().currency);
        } else {
            result = sharedPref.getInfoData().currency + " " + result;
        }
        return result;
    }

    public static Map<String, String> getRingTones(Context context, Integer type) {
        RingtoneManager ringtoneManager = new RingtoneManager(context);
        if (type == null) {
            ringtoneManager.setType(type);
        } else {
            ringtoneManager.setType(RingtoneManager.TYPE_ALL);
        }
        Cursor cursor = ringtoneManager.getCursor();
        HashMap<String, String> ringTones = new HashMap<>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            ringTones.put(uri, title);
        }

        return ringTones;
    }

    public static String getDeviceID(Activity act) {
        String deviceID = Build.SERIAL;
        if (deviceID == null || deviceID.trim().isEmpty() || deviceID.equals("unknown")) {
            try {
                deviceID = Settings.Secure.getString(act.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
            }
        }
        return deviceID;
    }

    public static String getDeviceId() {
        return FirebaseInstanceId.getInstance().getId();
    }

}
