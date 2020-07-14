package com.ecotioco.gaze.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.adapter.OrderHistoryAdapter;
import com.ecotioco.gaze.data.DatabaseHandler;
import com.ecotioco.gaze.model.Order;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderHistoryFragment extends Fragment {
    public static final String TAG = "OrderHistory";

    private static final String EXTRA_STATUS = "key.STATUS";

    private View rootView;
    private DatabaseHandler db;
    private OrderHistoryAdapter adapter;
    private int status;

    public OrderHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param status Order Status.
     * @return A new instance of fragment OrderHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderHistoryFragment newInstance(Integer status) {
        OrderHistoryFragment fragment = new OrderHistoryFragment();
        Bundle args = new Bundle();
        if (status == null) {
            status = 0;
        }
        args.putInt(EXTRA_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OrderHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderHistoryFragment newInstance() {
        return newInstance(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        db = new DatabaseHandler(getContext());

        if (getArguments() != null) {
            status = getArguments().getInt(EXTRA_STATUS, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_order_history, container, false);

        initToolbar();
        iniComponent();

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_activity_history);
        Tools.systemBarLolipop(getActivity());
    }
    
    private void iniComponent() {
        ViewPager2 viewPager = rootView.findViewById(R.id.pager);
        adapter = new OrderHistoryAdapter(this);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(Order.getStatus(getContext(), position));
            }
        }).attach();
        tabLayout.selectTab(tabLayout.getTabAt(status), true);
//        for (int i = 0; i < tabLayout.getTabCount(); i++) {
//            TabLayout.Tab tab = tabLayout.getTabAt(i);
//            if (i == 0) {
//                tab.setIcon(R.drawable.ic_delivering);
//            } else if (i == 1) {
//                tab.setIcon(R.drawable.icons_checked1);
//            } else {
//                tab.setIcon(R.drawable.ic_canceled);
//            }
//        }
    }
}