package com.ecotioco.gaze.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.adapter.ShoppingCartAdapter;
import com.ecotioco.gaze.data.DatabaseHandler;
import com.ecotioco.gaze.data.SharedPref;
import com.ecotioco.gaze.model.Cart;
import com.ecotioco.gaze.model.Info;
import com.ecotioco.gaze.model.Product;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingCartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingCartFragment extends Fragment {
    public static final String TAG = "ShoppingCart";

    private View rootView;
    private DatabaseHandler db;
    private ShoppingCartAdapter adapter;
    private Info info;

    public ShoppingCartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ShoppingCartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShoppingCartFragment newInstance() {
        ShoppingCartFragment fragment = new ShoppingCartFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        db = new DatabaseHandler(getContext());
        SharedPref sharedPref = new SharedPref(getContext());
        info = sharedPref.getInfoData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_shopping_cart, container, false);

        initToolbar();
        initComponents();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_shopping_cart, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
        } else if (item_id == R.id.action_delete) {
            if (adapter.getItemCount() == 0) {
                Snackbar.make(rootView, R.string.msg_cart_empty, Snackbar.LENGTH_SHORT).show();
                return true;
            }
            dialogDeleteConfirmation();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData();
    }

    private void initToolbar() {
        ActionBar actionBar;
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_activity_cart);
        Tools.systemBarLolipop(getActivity());
    }

    private void initComponents() {
        rootView.findViewById(R.id.lyt_check_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter.getItemCount() > 0) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    transaction.replace(R.id.dynamic_fragment, CheckOutFragment.newInstance(), CheckOutFragment.TAG);
                    transaction.addToBackStack(CheckOutFragment.TAG);
                    transaction.commit();
                } else {
                    Snackbar.make(rootView, R.string.msg_cart_empty, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void displayData() {
        final List<Cart> items =  new ArrayList<>(); //db.getActiveCartList();
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
        adapter = new ShoppingCartAdapter(getContext(), true, items);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        adapter.setOnRemoveItemClick(new ShoppingCartAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Cart obj) {
                Toast.makeText(getContext(), "Remove one item", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnItemClickListener(new ShoppingCartAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Cart obj) {
                dialogCartAction(obj);
            }
        });
        View noItemLayout = rootView.findViewById(R.id.lyt_no_item);
        View subTotalLayout = rootView.findViewById(R.id.lyt_sub_total);
        View checkOutLayout = rootView.findViewById(R.id.lyt_check_out);
        if (adapter.getItemCount() == 0) {
            noItemLayout.setVisibility(View.VISIBLE);
            subTotalLayout.setVisibility(View.GONE);
            checkOutLayout.setVisibility(View.GONE);
        } else {
            noItemLayout.setVisibility(View.GONE);
            subTotalLayout.setVisibility(View.VISIBLE);
            checkOutLayout.setVisibility(View.VISIBLE);
        }
        setTotalPrice();
    }

    private void setTotalPrice() {
        List<Cart> items = adapter.getItems();
        double totalPrice = 0D;
        for (Cart cartItem : items) {
            totalPrice += cartItem.quantity * cartItem.product.price;
        }
        ((TextView) rootView.findViewById(R.id.price_total)).setText(" " + Tools.getFormattedPrice(totalPrice, getContext()));
    }

    private void dialogCartAction(final Cart model) {

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_cart_option);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        ((TextView) dialog.findViewById(R.id.title)).setText(model.product.name);
        ((TextView) dialog.findViewById(R.id.stock)).setText(getString(R.string.stock) + model.product.stock);
        final TextView qty = (TextView) dialog.findViewById(R.id.quantity);
        qty.setText(model.quantity + "");

        dialog.findViewById(R.id.img_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model.quantity > 1) {
                    model.quantity = model.quantity - 1;
                    qty.setText(model.quantity + "");
                }
            }
        });
        dialog.findViewById(R.id.img_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model.quantity < model.product.stock) {
                    model.quantity = model.quantity + 1;
                    qty.setText(model.quantity + "");
                }
            }
        });
        dialog.findViewById(R.id.bt_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                db.saveCart(model);
                displayData();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.bt_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.deleteActiveCart(model.productId);
                displayData();
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void dialogDeleteConfirmation() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        AlertDialogFragment dialog =
                AlertDialogFragment.newInstance(getActivity().getString(R.string.title_delete_confirm),
                        getActivity().getString(R.string.content_delete_confirm),
                        getActivity().getString(R.string.OK), getString(R.string.CANCEL), AlertDialogFragment.AlertDialogMode.CLEAR_CART);
        dialog.show(fragmentTransaction, AlertDialogFragment.TAG);
    }
}