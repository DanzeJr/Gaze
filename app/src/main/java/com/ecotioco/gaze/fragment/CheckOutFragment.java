package com.ecotioco.gaze.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.ecotioco.gaze.R;
import com.ecotioco.gaze.adapter.ShoppingCartAdapter;
import com.ecotioco.gaze.connection.GazeAPI;
import com.ecotioco.gaze.connection.VolleyCallback;
import com.ecotioco.gaze.data.DatabaseHandler;
import com.ecotioco.gaze.data.SharedPref;
import com.ecotioco.gaze.model.Cart;
import com.ecotioco.gaze.model.DeliveryInfo;
import com.ecotioco.gaze.model.Info;
import com.ecotioco.gaze.model.Order;
import com.ecotioco.gaze.model.Product;
import com.ecotioco.gaze.model.User;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CheckOutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckOutFragment extends Fragment {
    public static final String TAG = "CheckOut";

    private View rootView;
    private ShoppingCartAdapter adapter;
    private DeliveryInfo deliveryInfo;
    private SharedPref sharedPref;
    private RecyclerView recyclerView;
    private TextView tvSubTotal, tvShippingFee, tvTotalFees;
    private ProgressDialogFragment progressDialog;

    private double mTotalFees = 0D;
    private double mShippingFee = 0D;

    public CheckOutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CheckOutFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckOutFragment newInstance() {
        CheckOutFragment fragment = new CheckOutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPref = new SharedPref(getContext());
        deliveryInfo = sharedPref.getDeliveryInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_check_out, container, false);
        initToolbar();
        iniComponent();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
        actionBar.setTitle(R.string.title_activity_checkout);
        Tools.systemBarLolipop(getActivity());
    }

    private void iniComponent() {
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // cost view
        tvSubTotal = rootView.findViewById(R.id.sub_total);
        tvShippingFee = rootView.findViewById(R.id.shipping_fee);
        tvTotalFees = rootView.findViewById(R.id.total_fees);

        rootView.findViewById(R.id.lyt_submit_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogConfirmCheckout();
            }
        });

        EditDeliveryInfoFragment editDeliveryInfoFragment = (EditDeliveryInfoFragment) getActivity().getSupportFragmentManager().findFragmentByTag(EditDeliveryInfoFragment.TAG);
        if (editDeliveryInfoFragment == null) {
            DeliveryInfoFragment deliveryInfoFragment = (DeliveryInfoFragment) getActivity().getSupportFragmentManager().findFragmentByTag(DeliveryInfoFragment.TAG);
            if (deliveryInfoFragment == null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.delivery_info_fragment, DeliveryInfoFragment.newInstance(deliveryInfo, true), DeliveryInfoFragment.TAG)
                        .commit();
            }
        }
    }

    private void displayData() {
        List<Cart> items = new ArrayList<>(); //db.getActiveCartList();
        for (long i = 1; i < 20; i++) {
            Product product = new Product();
            product.id = i + 12;
            product.price = i * 25d;
            product.color = "#FF70" + (i < 10 ? i + 10 : i);
            product.createdDate = new Date();
            product.lastUpdate = product.createdDate;
            product.name = "Product " + i * 31;
            product.stock = 33 % i;
            product.status = (int) (2 % i);
            product.image = "";
            product.size = (int) (3 % i);
            product.discount = i * 15d;
            Cart cart = new Cart(product, (int) (22 / i), new Date());
            items.add(cart);
        }
        adapter = new ShoppingCartAdapter(getContext(), false, items);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        setTotalPrice();
    }

    private void setTotalPrice() {
        List<Cart> items = adapter.getItems();
        double subTotal = 0d;
        for (Cart c : items) {
            subTotal += (c.quantity * c.product.price);
        }
        mTotalFees = subTotal + mShippingFee;

        // set to display
        tvSubTotal.setText(Tools.getFormattedPrice(subTotal, getContext()));
        tvShippingFee.setText(Tools.getFormattedPrice(mShippingFee, getContext()));
        tvTotalFees.setText(Tools.getFormattedPrice(mTotalFees, getContext()));
    }

    private void submitOrderData() {
        // prepare checkout data
        final Order order = new Order(deliveryInfo, adapter.getItems());

        // submit data to server
        GazeAPI api = GazeAPI.getInstance(getContext());
        VolleyCallback<JSONObject> callback = new VolleyCallback<JSONObject>() {
            @Override
            public void handleResponse(JSONObject result) {
                if (result != null) {
                    Order order = GazeAPI.get(result, Order.class);
                    for (Cart c : adapter.getItems()) {
                        c.orderId = order.id;
                        order.cartList.add(c);
                    }
                    dialogSuccess(order.code);
                } else {
                    dialogFailedRetry();
                }
            }
            @Override
            public void handleError(VolleyError error) {
                dialogFailedRetry();
            }
        };
//        api.submitProductOrder(checkout, callback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogSuccess(order.code);
            }
        }, 3000);
    }

    // give delay when submit data to give good UX
    public void delaySubmitOrderData() {
        progressDialog = (ProgressDialogFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(ProgressDialogFragment.TAG);
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance(getString(R.string.title_please_wait), getString(R.string.submitting) + "...");
            progressDialog.show(getActivity().getSupportFragmentManager(), ProgressDialogFragment.TAG);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                submitOrderData();
            }
        }, 2000);
    }

    private void dialogConfirmCheckout() {
        long delayTime = 0;
        EditDeliveryInfoFragment editDeliveryInfoFragment = (EditDeliveryInfoFragment) getActivity().getSupportFragmentManager().findFragmentByTag(EditDeliveryInfoFragment.TAG);
        if (editDeliveryInfoFragment != null) {
            if (!editDeliveryInfoFragment.saveDeliveryInfo()) {
                return;
            }
            delayTime = 1000;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlertDialogFragment dialog = AlertDialogFragment.newInstance(getString(R.string.confirmation),
                        getString(R.string.confirm_checkout), getString(R.string.YES), getString(R.string.NO), AlertDialogFragment.AlertDialogMode.CONFIRM_CHECKOUT);
                dialog.show(getActivity().getSupportFragmentManager(), AlertDialogFragment.TAG);
            }
        }, delayTime);
    }

    private void dialogFailedRetry() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(getString(R.string.failed),
                getString(R.string.failed_checkout), getString(R.string.TRY_AGAIN), getString(R.string.SETTING), AlertDialogFragment.AlertDialogMode.RETRY_CHECKOUT);
        dialog.show(getActivity().getSupportFragmentManager(), AlertDialogFragment.TAG);
    }

    private void dialogSuccess(String code) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        AlertDialogFragment dialog = AlertDialogFragment.newInstance(getString(R.string.success_checkout),
                String.format(getString(R.string.msg_success_checkout), code),
                getString(R.string.OK), null, R.drawable.img_checkout_success, R.layout.dialog_info, AlertDialogFragment.AlertDialogMode.CHECKOUT_SUCCESS);
        dialog.show(getActivity().getSupportFragmentManager(), AlertDialogFragment.TAG);
    }

}