package com.ecotioco.gaze.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ecotioco.gaze.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlertDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    public static final String TAG = "AlertDialog";

    private static final String EXTRA_TITLE = "key.TITLE";
    private static final String EXTRA_MESSAGE = "key.MESSAGE";
    private static final String EXTRA_POSITIVE_BUTTON = "key.POSITIVE_BUTTON";
    private static final String EXTRA_NEGATIVE_BUTTON = "key.NEGATIVE_BUTTON";
    private static final String EXTRA_ICON = "key.ICON";
    private static final String EXTRA_LAYOUT = "key.LAYOUT";
    private static final String EXTRA_CANCELABLE = "key.CANCELABLE";
    private static final String EXTRA_MODE = "key.MODE";

    public enum AlertDialogMode implements Serializable {
        NONE,
        REMOVE_CART_ITEM,
        CLEAR_CART,
        CONFIRM_CHECKOUT,
        RETRY_CHECKOUT,
        CHECKOUT_SUCCESS,
        CANCEL_ORDER
    }

    // TODO: Rename and change types of parameters
    private String title;
    private String message;
    private String positiveButtonText = null;
    private String negativeButtonText = null;
    private int icon = -1;
    private int layout = -1;
    private AlertDialogMode mode = null;

    public AlertDialogFragment() {
        // Required empty public constructor
    }

    //region Methods of newInstance

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title              Title.
     * @param message            Message.
     * @param positiveButtonText Positive Button Text (null means not showing)
     * @param negativeButtonText Negative Button Text (null means not showing)
     * @param icon               Icon
     * @param layout             Layout
     * @param mode               Dialog Mode
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertDialogFragment newInstance(@NonNull String title, @NonNull String message,
                                                  String positiveButtonText, String negativeButtonText,
                                                  @DrawableRes Integer icon, @LayoutRes Integer layout,
                                                  AlertDialogMode mode) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        args.putString(EXTRA_MESSAGE, message);
        args.putString(EXTRA_POSITIVE_BUTTON, positiveButtonText);
        args.putString(EXTRA_NEGATIVE_BUTTON, negativeButtonText);
        if (icon != null) {
            args.putInt(EXTRA_ICON, icon);
        }
        if (layout != null) {
            args.putInt(EXTRA_LAYOUT, layout);
        }
        if (mode == null) {
            mode = AlertDialogMode.NONE;
        }
        args.putSerializable(EXTRA_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title   Title.
     * @param message Message.
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertDialogFragment newInstance(@NonNull String title, @NonNull String message) {
        return newInstance(title, message, null, null, null, null, null);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title              Title.
     * @param message            Message.
     * @param positiveButtonText Positive Button Text (null means not showing)
     * @param negativeButtonText Negative Button Text (null means not showing)
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertDialogFragment newInstance(@NonNull String title, @NonNull String message,
                                                  String positiveButtonText, String negativeButtonText) {
        return newInstance(title, message, positiveButtonText, negativeButtonText, null, null, null);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title              Title.
     * @param message            Message.
     * @param positiveButtonText Positive Button Text (null means not showing)
     * @param negativeButtonText Negative Button Text (null means not showing)
     * @param mode               Mode
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertDialogFragment newInstance(@NonNull String title, @NonNull String message,
                                                  String positiveButtonText, String negativeButtonText,
                                                  AlertDialogMode mode) {
        return newInstance(title, message, positiveButtonText, negativeButtonText, null, null, mode);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title              Title.
     * @param message            Message.
     * @param positiveButtonText Positive Button Text (null means not showing)
     * @param negativeButtonText Negative Button Text (null means not showing)
     * @param icon               Icon
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertDialogFragment newInstance(@NonNull String title, @NonNull String message,
                                                  String positiveButtonText, String negativeButtonText,
                                                  @DrawableRes Integer icon) {
        return newInstance(title, message, positiveButtonText, negativeButtonText, icon, null, null);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title              Title.
     * @param message            Message.
     * @param positiveButtonText Positive Button Text (null means not showing)
     * @param negativeButtonText Negative Button Text (null means not showing)
     * @param icon               Icon
     * @param layout             Layout
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertDialogFragment newInstance(@NonNull String title, @NonNull String message,
                                                  String positiveButtonText, String negativeButtonText,
                                                  @DrawableRes Integer icon, @LayoutRes Integer layout) {
        return newInstance(title, message, positiveButtonText, negativeButtonText, icon, layout, null);
    }
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(EXTRA_TITLE);
            message = getArguments().getString(EXTRA_MESSAGE);
            positiveButtonText = getArguments().getString(EXTRA_POSITIVE_BUTTON, null);
            negativeButtonText = getArguments().getString(EXTRA_NEGATIVE_BUTTON, null);
            icon = getArguments().getInt(EXTRA_ICON, -1);
            layout = getArguments().getInt(EXTRA_LAYOUT, -1);
            mode = (AlertDialogMode) getArguments().getSerializable(EXTRA_MODE);
        }

        if (savedInstanceState != null) {
            boolean cancelable = savedInstanceState.getBoolean(EXTRA_CANCELABLE, true);
            setCancelable(cancelable);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // If there is no layout
        if (layout == -1) {
            builder.setTitle(title).setMessage(message);
            if (positiveButtonText != null) {
                builder.setPositiveButton(positiveButtonText, this);
            }
            if (negativeButtonText != null) {
                builder.setNegativeButton(negativeButtonText, this);
            }
            if (icon != -1) {
                builder.setIcon(icon);
            }
            return builder.create();
        }

        // If there is layout
        View contentView = getLayoutInflater().inflate(layout, null);
        builder.setView(contentView);

        if (layout == R.layout.dialog_info) {
            ((TextView) contentView.findViewById(R.id.title)).setText(title);
            ((TextView) contentView.findViewById(R.id.content)).setText(message);
            ((Button) contentView.findViewById(R.id.bt_positive)).setText(positiveButtonText);
            ((ImageView) contentView.findViewById(R.id.icon)).setImageResource(icon);

            contentView.findViewById(R.id.bt_positive).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialogFragment.this.onClick(getDialog(), AlertDialog.BUTTON_POSITIVE);
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_CANCELABLE, isCancelable());
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (mode) {
            case REMOVE_CART_ITEM: {
                handleRemoveCartItem();
                break;
            }
            case CLEAR_CART: {
                handleClearCart();
                break;
            }
            case CONFIRM_CHECKOUT: {
                handleConfirmCheckout(which);
                break;
            }
            case RETRY_CHECKOUT: {
                handleRetryCheckout(which);
                break;
            }
            case CHECKOUT_SUCCESS: {
                handleCheckoutSuccess(which);
                break;
            }
            case CANCEL_ORDER: {
                handleCancelOrder(which);
                break;
            }
            default:
                break;
        }
    }

    private void handleRemoveCartItem() {
        Toast.makeText(getContext(), "Hê Hê Hê", Toast.LENGTH_LONG).show();
    }

    private void handleClearCart() {
        dismiss();
//        db.deleteActiveCart();
        ShoppingCartFragment fragment = (ShoppingCartFragment) getActivity().getSupportFragmentManager().findFragmentByTag(ShoppingCartFragment.TAG);
        fragment.displayData();
        Snackbar.make(fragment.getView(), R.string.delete_success, Snackbar.LENGTH_SHORT).show();
    }

    private void handleConfirmCheckout(int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            dismiss();
            CheckOutFragment fragment = (CheckOutFragment) getActivity().getSupportFragmentManager().findFragmentByTag(CheckOutFragment.TAG);
            fragment.delaySubmitOrderData();
        }
    }

    private void handleRetryCheckout(int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            dismiss();
            CheckOutFragment fragment = (CheckOutFragment) getActivity().getSupportFragmentManager().findFragmentByTag(CheckOutFragment.TAG);
            fragment.delaySubmitOrderData();
        } else if (which == AlertDialog.BUTTON_NEGATIVE) {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.dynamic_fragment, SettingsFragment.newInstance());
            fragmentTransaction.addToBackStack(SettingsFragment.TAG);
            fragmentTransaction.commit();
        }
    }

    private void handleCheckoutSuccess(int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            dismiss();
            getActivity().getSupportFragmentManager().popBackStack(HomeFragment.TAG, 0);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.dynamic_fragment, OrderHistoryFragment.newInstance(), OrderHistoryFragment.TAG);
            transaction.addToBackStack(OrderHistoryFragment.TAG);
            transaction.commit();
        }

    }

    private void handleCancelOrder(int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            dismiss();
        } else if (which == AlertDialog.BUTTON_NEGATIVE) {
            dismiss();
        }
    }
}