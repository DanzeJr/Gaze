package com.ecotioco.gaze;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.data.AppConfig;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.model.NewsInfo;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.ecotioco.gaze.utils.Tools;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityNewsInfoDetails extends AppCompatActivity {

    private static final String EXTRA_OBJECT_ID = "key.EXTRA_OBJECT_ID";
    private static final String EXTRA_FROM_NOTIF = "key.EXTRA_FROM_NOTIF";

    // activity transition
    public static void navigate(Activity activity, Long id, Boolean from_notif) {
        Intent i = navigateBase(activity, id, from_notif);
        activity.startActivity(i);
    }

    public static Intent navigateBase(Context context, Long id, Boolean from_notif) {
        Intent i = new Intent(context, ActivityNewsInfoDetails.class);
        i.putExtra(EXTRA_OBJECT_ID, id);
        i.putExtra(EXTRA_FROM_NOTIF, from_notif);
        return i;
    }

    private Long news_id;
    private Boolean from_notif;

    // extra obj
    private NewsInfo newsInfo;

    private JsonObjectRequest request = null;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private View parent_view;
    private SwipeRefreshLayout swipe_refresh;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_info_details);

        news_id = getIntent().getLongExtra(EXTRA_OBJECT_ID, -1L);
        from_notif = getIntent().getBooleanExtra(EXTRA_FROM_NOTIF, false);

        initComponent();
        initToolbar();
        requestAction();
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        // on swipe
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestAction();
            }
        });
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestNewsInfoDetailsApi();
            }
        }, 1000);
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void requestNewsInfoDetailsApi() {
        GazeAPI api = GazeAPI.getInstance(this);
        VolleyCallback<JSONObject> callback = new VolleyCallback<JSONObject>() {
            @Override
            public void handleResponse(JSONObject result) {
                if (result != null) {
                    newsInfo = GazeAPI.get(result, NewsInfo.class);
                    displayPostData();
                    swipeProgress(false);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void handleError(VolleyError error) {
                if (!request.isCanceled()) onFailRequest();
            }
        };
        request = api.getNewsDetails(news_id, callback);
    }

    private void displayPostData() {
        TextView titleTextView = findViewById(R.id.title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            titleTextView.setText(Html.fromHtml(newsInfo.title, Html.FROM_HTML_MODE_LEGACY));
        } else {
            titleTextView.setText(Html.fromHtml(newsInfo.title));
        }

        webview = (WebView) findViewById(R.id.content);
        String html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
        html_data += newsInfo.fullContent;
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings();
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setBackgroundColor(Color.TRANSPARENT);
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadData(html_data, "text/html; charset=UTF-8", null);
        // disable scroll on touch
        webview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        ((TextView) findViewById(R.id.date)).setText(Tools.getFormattedDate(newsInfo.lastUpdate));
        if (newsInfo.status.equalsIgnoreCase("FEATURED")) {
            ((TextView) findViewById(R.id.featured)).setVisibility(View.VISIBLE);
        }

        Tools.displayImageOriginal(this, ((ImageView) findViewById(R.id.image)), Constant.getURLimgNews(newsInfo.image));

        ((MaterialRippleLayout) findViewById(R.id.lyt_image)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> images_list = new ArrayList<>();
                images_list.add(Constant.getURLimgNews(newsInfo.image));
                Intent i = new Intent(ActivityNewsInfoDetails.this, ActivityFullScreenImage.class);
                i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list);
                startActivity(i);
            }
        });

        Snackbar.make(parent_view, R.string.msg_data_loaded, Snackbar.LENGTH_SHORT).show();


        // analytics track
        ThisApplication.getInstance().saveLogEvent(newsInfo.id, newsInfo.title, "NEWS_DETAILS");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webview != null) webview.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webview != null) webview.onPause();
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = (View) findViewById(R.id.lyt_failed);
        View lyt_main_content = (View) findViewById(R.id.lyt_main_content);

        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_main_content.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_main_content.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        ((Button) findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            return;
        }
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(show);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackAction();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onBackAction();
    }

    private void onBackAction() {
        if (from_notif) {
//            if (ActivityMain.active) {
//                finish();
//            } else {
                Intent intent = new Intent(getApplicationContext(), ActivitySplash.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
//            }
        } else {
            super.onBackPressed();
        }
    }

}
