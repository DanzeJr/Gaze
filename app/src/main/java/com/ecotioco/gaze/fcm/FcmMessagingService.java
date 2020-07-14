package com.ecotioco.gaze.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ecotioco.gaze.ActivityDialogNotification;
import com.ecotioco.gaze.R;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.data.DatabaseHandler;
import com.ecotioco.gaze.data.SharedPref;
import com.ecotioco.gaze.model.Notification;
import com.ecotioco.gaze.utils.CallbackImageNotif;
import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Date;

public class FcmMessagingService extends FirebaseMessagingService {
    private static final String CHANEL_ID = "FcmMessagingService";
    private static int VIBRATION_TIME = 500; // in millisecond
    private static SharedPref sharedPref;
    private DatabaseHandler db;
    private int retry_count = 0;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        sharedPref = new SharedPref(this);
        sharedPref.setFcmRegId(s);
        sharedPref.setOpenAppCounter(SharedPref.MAX_OPEN_COUNTER);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (sharedPref == null) {
            sharedPref = new SharedPref(this);
        }
        db = new DatabaseHandler(this);
        retry_count = 0;
        if (sharedPref.getNotification()) {
            if (remoteMessage.getData().size() <= 0) return;
            Object obj = remoteMessage.getData();
            String data = new Gson().toJson(obj);

            Notification notification = new Gson().fromJson(data, Notification.class);
            notification.id = System.currentTimeMillis();
            notification.createdDate = new Date();
            notification.read = false;

            // display notification
            prepareImageNotification(notification);

            // save notification to relam db
            saveNotification(notification);
        }
    }

    private void prepareImageNotification(final Notification notif) {
        String image_url = null;
        if (notif.type.equals("PRODUCT")) {
            image_url = Constant.getURLimgProduct(notif.image);
        } else if (notif.type.equals("NEWS_INFO")) {
            image_url = Constant.getURLimgNews(notif.image);
        } else if (notif.type.equals("PROCESS_ORDER")) {
            // update order status
            db.updateStatusOrder(notif.code, notif.status);
        }
        if (image_url != null) {
            glideLoadImageFromUrl(this, image_url, new CallbackImageNotif() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    showNotification(notif, bitmap);
                }

                @Override
                public void onFailed() {
                    if (retry_count <= Constant.LOAD_IMAGE_NOTIF_RETRY) {
                        retry_count++;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                prepareImageNotification(notif);
                            }
                        }, 1000);
                    } else {
                        showNotification(notif, null);
                    }
                }
            });
        } else {
            showNotification(notif, null);
        }
    }

    private void showNotification(Notification notif, Bitmap bitmap) {
        Intent intent = ActivityDialogNotification.navigateBase(this, notif, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(CHANEL_ID, "FCM Messaging Service", NotificationManager.IMPORTANCE_HIGH));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANEL_ID);
        builder.setContentTitle(notif.title);
        builder.setContentText(notif.content);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setDefaults(android.app.Notification.DEFAULT_LIGHTS);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(android.app.Notification.PRIORITY_HIGH);
        }
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notif.content));
        if (bitmap != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText(notif.content));
        }

        // display push notif
        int unique_id = (int) System.currentTimeMillis();
        notificationManager.notify(unique_id, builder.build());

        vibrationAndPlaySound();
    }

    private void vibrationAndPlaySound() {
        // play vibration
        if (sharedPref.getVibration()) {
            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VIBRATION_TIME);
        }
        // play tone
        RingtoneManager.getRingtone(this, Uri.parse(sharedPref.getRingtone())).play();
    }

    // load image with callback
    Handler mainHandler = new Handler(Looper.getMainLooper());
    Runnable myRunnable;

    private void glideLoadImageFromUrl(final Context ctx, final String url, final CallbackImageNotif callback) {

        myRunnable = new Runnable() {
            @Override
            public void run() {
                Glide.with(ctx).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        callback.onSuccess(resource);
                        mainHandler.removeCallbacks(myRunnable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        callback.onFailed();
                        super.onLoadFailed(errorDrawable);
                        mainHandler.removeCallbacks(myRunnable);
                    }
                });
            }
        };
        mainHandler.post(myRunnable);
    }

    private void saveNotification(Notification notification) {
        db.saveNotification(notification);
    }

    public static String getToken(Context context) {
        if (sharedPref == null) {
            sharedPref = new SharedPref(context);
        }
        return sharedPref.getFcmToken();
    }

}
