package com.ecotioco.gaze.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.ecotioco.gaze.ActivityNewsInfoDetails;
import com.ecotioco.gaze.R;
import com.ecotioco.gaze.adapter.FeaturedNewsAdapter;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.model.NewsInfo;
import com.ecotioco.gaze.utils.NetworkCheck;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FeaturedNewsFragment extends Fragment {
    public static final String TAG = "FeaturedNews";

    public FeaturedNewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static FeaturedNewsFragment newInstance() {
        FeaturedNewsFragment fragment = new FeaturedNewsFragment();
        return fragment;
    }

    private View rootView;
    private ViewPager2 viewPager;
    private Handler handler = new Handler();
    private Runnable runnableCode = null;
    private FeaturedNewsAdapter adapter;
    private TextView features_news_title;
    private View lyt_main_content;
    private ImageButton bt_previous, bt_next;
    private LinearLayout layout_dots;
    private JsonArrayRequest request;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_featured_news, null);
        initComponent();
        //        requestFeaturedNews();
        List<NewsInfo> list = new ArrayList<>();
        for (long i = 0; i < 10; i++) {
            NewsInfo info = new NewsInfo();
            info.id = i;
            info.briefContent = i + "ABC";
            info.createdDate = new Date();
            info.fullContent = i + "News";
            info.title = i + "Info";
            list.add(info);
        }
        displayResultData(list);
        return rootView;
    }

    private void initComponent() {
        lyt_main_content = rootView.findViewById(R.id.lyt_cart);
        features_news_title = rootView.findViewById(R.id.featured_news_title);
        layout_dots =  rootView.findViewById(R.id.layout_dots);
        viewPager = rootView.findViewById(R.id.pager);
        bt_previous = rootView.findViewById(R.id.bt_previous);
        bt_next = rootView.findViewById(R.id.bt_next);
        adapter = new FeaturedNewsAdapter(getActivity(), new ArrayList<NewsInfo>());
        lyt_main_content.setVisibility(View.GONE);

        bt_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevAction();
            }
        });

        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextAction();
            }
        });
    }

    private void displayResultData(List<NewsInfo> items) {
        adapter.setItems(items);
        viewPager.setAdapter(adapter);

        LayoutParams params = viewPager.getLayoutParams();
        params.height = Tools.getFeaturedNewsImageHeight(getActivity());
        viewPager.setLayoutParams(params);

        // displaying selected image first
        viewPager.setCurrentItem(0);
        features_news_title.setText(adapter.getItem(0).title);
        addBottomDots(layout_dots, adapter.getItemCount(), 0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int pos) {
                NewsInfo cur = adapter.getItem(pos);
                features_news_title.setText(cur.title);
                addBottomDots(layout_dots, adapter.getItemCount(), pos);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        startAutoSlider(adapter.getItemCount());

        adapter.setOnItemClickListener(new FeaturedNewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, NewsInfo obj) {
                Snackbar.make(rootView, obj.title, Snackbar.LENGTH_SHORT).show();
                ActivityNewsInfoDetails.navigate(getActivity(), obj.id, false);
            }
        });

        lyt_main_content.setVisibility(View.VISIBLE);
    }

    private void requestFeaturedNews() {
        GazeAPI api = GazeAPI.getInstance(getContext());
        VolleyCallback<JSONArray> callback = new VolleyCallback<JSONArray>() {
            @Override
            public void handleResponse(JSONArray result) {
                if (result != null) {
                    displayResultData(Arrays.asList(GazeAPI.get(result, NewsInfo[].class)));
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void handleError(VolleyError error) {
                if (request != null && !request.isCanceled()){
                    onFailRequest();
                }
            }
        };
        request = api.getFeaturedNews(callback);
    }

    private void startAutoSlider(final int count) {
        runnableCode = new Runnable() {
            @Override
            public void run() {
                int pos = viewPager.getCurrentItem();
                pos = pos + 1;
                if (pos >= count) pos = 0;
                viewPager.setCurrentItem(pos);
                handler.postDelayed(runnableCode, 3000);
            }
        };
        handler.postDelayed(runnableCode, 3000);
    }

    private void prevAction() {
        int pos = viewPager.getCurrentItem();
        pos = pos - 1;
        if (pos < 0) pos = adapter.getItemCount();
        viewPager.setCurrentItem(pos);
    }

    private void nextAction() {
        int pos = viewPager.getCurrentItem();
        pos = pos + 1;
        if (pos >= adapter.getItemCount()) pos = 0;
        viewPager.setCurrentItem(pos);
    }

    @Override
    public void onDestroy() {
        if (runnableCode != null)
            handler.removeCallbacks(runnableCode);
        super.onDestroy();
    }

    private void addBottomDots(LinearLayout layout_dots, int size, int current) {
        ImageView[] dots = new ImageView[size];

        layout_dots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(getActivity());
            int width_height = 10;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LayoutParams(width_height, width_height));
            params.setMargins(10, 10, 10, 10);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.shape_circle);
            dots[i].setColorFilter(ContextCompat.getColor(getActivity(), R.color.darkOverlaySoft));
            layout_dots.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[current].setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimaryLight));
        }
    }

    private void onFailRequest() {
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(R.string.msg_failed_load_data);
        } else {
            showFailedView(R.string.no_internet_text);
        }
    }

    private void showFailedView(@StringRes int message) {
//        ((OnLoadData) getActivity()).failLoading(message);
    }
}
