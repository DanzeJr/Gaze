package com.ecotioco.gaze;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.data.DatabaseHandler;
import com.ecotioco.gaze.model.Notification;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

public class ActivityDialogNotification extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";
    private static final String EXTRA_fromNotif = "key.EXTRA_fromNotif";
    private static final String EXTRA_POSITION = "key.EXTRA_FROM_POSITION";

    // activity transition
    public static void navigate(Activity activity, Notification obj, Boolean fromNotif, int position) {
        Intent i = navigateBase(activity, obj, fromNotif);
        i.putExtra(EXTRA_POSITION, position);
        activity.startActivity(i);
    }

    public static Intent navigateBase(Context context, Notification obj, Boolean fromNotif) {
        Intent i = new Intent(context, ActivityDialogNotification.class);
        i.putExtra(EXTRA_OBJECT, obj);
        i.putExtra(EXTRA_fromNotif, fromNotif);
        return i;
    }

    private Boolean fromNotif;
    private Notification notification;
    private Intent intent;
    private DatabaseHandler db;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_notification);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        db = new DatabaseHandler(this);

        notification = (Notification) getIntent().getSerializableExtra(EXTRA_OBJECT);
        fromNotif = getIntent().getBooleanExtra(EXTRA_fromNotif, false);
        position = getIntent().getIntExtra(EXTRA_POSITION, -1);

        // set notification as read
        notification.read = true;
        db.saveNotification(notification);

        initComponent();
    }

    private void initComponent() {
        ((TextView) findViewById(R.id.title)).setText(notification.title);
        ((TextView) findViewById(R.id.content)).setText(notification.content);
        ((TextView) findViewById(R.id.date)).setText(Tools.getFormattedDate(notification.createdDate));

        String image_url = null;
        if (notification.type.equals("PRODUCT")) {
            image_url = Constant.getURLimgProduct(notification.image);
        } else if (notification.type.equals("NEWS_INFO")) {
            image_url = Constant.getURLimgNews(notification.image);
        }

        findViewById(R.id.lyt_image).setVisibility(View.GONE);
        if (image_url != null) {
            ((RelativeLayout) findViewById(R.id.lyt_image)).setVisibility(View.VISIBLE);
            Tools.displayImageOriginal(this, ((ImageView) findViewById(R.id.image)), image_url);
        } else if (!fromNotif) {
            ((Button) findViewById(R.id.bt_open)).setVisibility(View.GONE);
        }

        if (fromNotif) {
            ((Button) findViewById(R.id.bt_delete)).setVisibility(View.GONE);
            if (image_url == null) {
                findViewById(R.id.lyt_action).setVisibility(View.GONE);
            }
        } else {
            ((TextView) findViewById(R.id.dialog_title)).setText(getString(R.string.title_notif_details));
            ((ImageView) findViewById(R.id.logo)).setVisibility(View.GONE);
            ((View) findViewById(R.id.view_space)).setVisibility(View.GONE);
        }

        intent = new Intent(this, ActivitySplash.class);
        if (notification.type.equals("PRODUCT")) {
//            intent = ActivityProductDetails.navigateBase(this, notification.objectId, fromNotif);
        } else if (notification.type.equals("NEWS_INFO")) {
            intent = ActivityNewsInfoDetails.navigateBase(this, notification.objectId, fromNotif);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        ((ImageView) findViewById(R.id.img_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ((Button) findViewById(R.id.bt_open)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(intent);
            }
        });

        ((Button) findViewById(R.id.bt_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                if (!fromNotif && position != -1) {
                    db.deleteNotification(notification.id);
                    ActivityNotification.getInstance().adapter.removeItem(position);
                    Snackbar.make(ActivityNotification.getInstance().parent_view, "Delete successfully", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
