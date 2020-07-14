package com.ecotioco.gaze.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.adapter.ShoppingCartAdapter;
import com.ecotioco.gaze.adapter.OrderAdapter;
import com.ecotioco.gaze.data.OrderStatus;
import com.ecotioco.gaze.model.Cart;
import com.ecotioco.gaze.model.Order;
import com.ecotioco.gaze.model.Product;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment {
    public static final String TAG = "Order";

    private static final String EXTRA_STATUS = "key.STATUS";

    private View rootView;
    private OrderAdapter adapter;
    private int status;

    public OrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderFragment newInstance(int status) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        if (status < 0 || status > OrderStatus.values().length - 1) {
            status = 0;
        }
        args.putInt(EXTRA_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getInt(EXTRA_STATUS, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_order, container, false);

        iniComponent();
        displayData();

        return rootView;
    }

    private void iniComponent() {
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void displayData() {
        List<Order> items = new ArrayList<>(); //db.getOrderList();
        for (int i = 1; i < 10; i++) {
            Order order = new Order("Name " + i, "Address " + i, "abc" + i + "@gmail.com", i * 213313421 + "",
                    1 % i + 1, new Date(i * 200054511255l), "No comment for long " + i + "time", new ArrayList<Cart>());
            for (int j = 0; j < i * 2; j++) {
                Product product = new Product();
                product.id = i + 12l;
                product.price = i * 25d;
                product.color = "#FF70" + (i < 10 ? i + 10 : i);
                product.createdDate = new Date();
                product.lastUpdate = product.createdDate;
                product.name = "Product " + i * 31;
                product.stock = 33 % (long)i;
                product.status = (int) (2 % i);
                product.image = "";
                product.size = (int) (3 % i);
                product.discount = i * 15d;
                Cart cart = new Cart(product, (int) (22 / i), new Date());
                order.cartList.add(cart);
            }
            order.id = (long) i * 12;
            order.status = status;
            order.code = "#GAZE2020142" + i;
            order.createdDate = new Date();
            items.add(order);
        }
        adapter = new OrderAdapter(getContext(), items);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        adapter.setOnItemClickListener(new OrderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Order obj) {
                dialogOrderHistoryDetails(obj);
            }
        });
        View noItemLayout = rootView.findViewById(R.id.lyt_no_item);
        if (adapter.getItemCount() == 0) {
            noItemLayout.setVisibility(View.VISIBLE);
        } else {
            noItemLayout.setVisibility(View.GONE);
        }
    }

    private void dialogOrderHistoryDetails(final Order order) {
        OrderDetailsFragment fragment = OrderDetailsFragment.newInstance(order);
        fragment.show(getActivity().getSupportFragmentManager().beginTransaction(), OrderDetailsFragment.TAG);
    }

    private void dialogCancelConfirmation() {
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(getString(R.string.title_cancel_confirm),
                getString(R.string.content_cancel_confirm), getString(R.string.YES), getString(R.string.NO),
                AlertDialogFragment.AlertDialogMode.CANCEL_ORDER);
        dialog.show(getActivity().getSupportFragmentManager(), AlertDialogFragment.TAG);
    }

    public void cancelOrder() {
        Snackbar.make(rootView, getString(R.string.cancel_success), BaseTransientBottomBar.LENGTH_LONG);
    }
}