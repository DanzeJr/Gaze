package com.ecotioco.gaze.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.adapter.ShoppingCartAdapter;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.model.Order;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class OrderDetailsFragment extends DialogFragment implements DialogInterface.OnClickListener {
    public static final String TAG = "OrderDetails";

    private static final String EXTRA_ORDER = "key.ORDER";

    private Order order;
    private View contentView;
    private ShoppingCartAdapter adapter;

    public OrderDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param order Order.
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderDetailsFragment newInstance(@NonNull Order order) {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.order = (Order) getArguments().getSerializable(EXTRA_ORDER);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        contentView = getLayoutInflater().inflate(R.layout.fragment_order_details, null);
        builder.setView(contentView)
                .setNeutralButton(R.string.CANCEL_ORDER, this);

        RecyclerView recyclerView = contentView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShoppingCartAdapter(getContext(), false, order.cartList);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        FrameLayout fl = contentView.findViewById(R.id.delivery_info_fragment);
        setupComponent();

        return builder.create();
    }

    private void requestCancelOrder() {
        Snackbar.make(contentView, getString(R.string.cancel_success) + " " + order.code, BaseTransientBottomBar.LENGTH_SHORT);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_NEUTRAL) {
            requestCancelOrder();
        }
    }

    private void setupComponent() {
        ((TextView) contentView.findViewById(R.id.code)).setText(order.code);
        contentView.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.copyToClipboard(getActivity().getApplicationContext(), order.code);
            }
        });
        contentView.findViewById(R.id.img_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        ((TextView) contentView.findViewById(R.id.order_date)).setText(Tools.getFormattedDate(order.createdDate));
        ((TextView) contentView.findViewById(R.id.status)).setText(Order.getStatus(getContext(), order.status));
        adapter.setItems(order.cartList);
        ((TextView) contentView.findViewById(R.id.sub_total))
                .setText(getString(R.string.sub_total) + "(" + (order.getAmount() > 1 ? getString(R.string.items) : getString(R.string.item)) + ")");
        ((TextView) contentView.findViewById(R.id.sub_total))
                .setText(Tools.getFormattedPrice(order.getTotal(false), getContext()) + "");
        ((TextView) contentView.findViewById(R.id.shipping_fee))
                .setText(Tools.getFormattedPrice(order.shippingFee, getContext()) + "");
        ((TextView) contentView.findViewById(R.id.total_fees))
                .setText(Tools.getFormattedPrice(order.getTotal(true), getContext()) + "");

//        DeliveryInfoFragment deliveryInfoFragment = (DeliveryInfoFragment) getActivity().getSupportFragmentManager().findFragmentByTag(DeliveryInfoFragment.TAG);
//        if (deliveryInfoFragment == null) {
//            getActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.delivery_info_fragment, DeliveryInfoFragment.newInstance(order.getDeliveryInfo(), false), DeliveryInfoFragment.TAG)
//                    .commit();
//        }
    }
}