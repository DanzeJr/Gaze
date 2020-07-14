package com.ecotioco.gaze.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ecotioco.gaze.data.OrderStatus;
import com.ecotioco.gaze.fragment.OrderFragment;

public class OrderHistoryAdapter extends FragmentStateAdapter {

    public OrderHistoryAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return OrderFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return OrderStatus.values().length;
    }
}
