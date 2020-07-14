package com.ecotioco.gaze;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ecotioco.gaze.adapter.AdapterNewsInfo;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.model.NewsInfo;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.ecotioco.gaze.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityNewsInfo extends AppCompatActivity {

    private View parent_view;
    private RecyclerView recyclerView;
    private AdapterNewsInfo mAdapter;
    private SwipeRefreshLayout swipe_refresh;
    private JsonObjectRequest request = null;

    private int post_total = 0;
    private int failed_page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_info);
        parent_view = findViewById(android.R.id.content);
        initToolbar();
        iniComponent();
    }

    private void initToolbar() {
        ActionBar actionBar;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_activity_news_info);
        Tools.systemBarLolipop(this);
    }

    public void iniComponent() {
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set data and list adapter
        mAdapter = new AdapterNewsInfo(this, recyclerView, new ArrayList<NewsInfo>());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterNewsInfo.OnItemClickListener() {
            @Override
            public void onItemClick(View v, NewsInfo obj, int position) {
                ActivityNewsInfoDetails.navigate(ActivityNewsInfo.this, obj.id, false);
            }
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(new AdapterNewsInfo.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (post_total > mAdapter.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    mAdapter.setLoaded();
                }
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (request != null) request.cancel();
                mAdapter.resetListData();
                requestAction(1);
            }
        });

        requestAction(1);
    }

    private void displayApiResult(final List<NewsInfo> items) {
        mAdapter.insertData(items);
        swipeProgress(false);
        if (items.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListNewsInfo(final int page_no) {
        GazeAPI api = GazeAPI.getInstance(this);
        VolleyCallback<JSONObject> callback = new VolleyCallback<JSONObject>() {
            @Override
            public void handleResponse(JSONObject result) {
                if (result != null) {
                    try {
                        post_total = result.getInt("count_total");
                        displayApiResult(Arrays.asList(GazeAPI.get(result.getJSONArray("news_infos"), NewsInfo[].class)));
                    } catch (JSONException e) {
                        onFailRequest(page_no);
                    }
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void handleError(VolleyError error) {
                if (!request.isCanceled()) onFailRequest(page_no);
            }
        };
        request = api.getListNewsInfo(page_no, Constant.NEWS_PER_REQUEST, null, callback);
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        mAdapter.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            mAdapter.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestListNewsInfo(page_no);
            }
        }, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (request != null) {
            request.cancel();
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = (View) findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        ((Button) findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = (View) findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText("no_post");
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
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
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
