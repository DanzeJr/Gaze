package com.ecotioco.gaze.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.data.SharedPref;
import com.ecotioco.gaze.model.DeliveryInfo;
import com.ecotioco.gaze.model.Order;
import com.ecotioco.gaze.model.User;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditDeliveryInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditDeliveryInfoFragment extends Fragment {
    public static final String TAG = "EditDeliveryInfo";

    private static final String EXTRA_INFO = "key.INFO";
    private static final String EXTRA_IS_SHOW_INFO = "key.IS_SHOW_INFO";

    // TODO: Rename and change types of parameters
    private DeliveryInfo deliveryInfo;
    private boolean isShowInfo;
    private SharedPref sharedPref;
    private View rootView;
    private Spinner shippingSpinner;
    private EditText etBuyerName, etEmail, etPhone, etAddress, etComment;
    private TextInputLayout buyerNameLayout, emailLayout, phoneLayout, addressLayout;
    private long shipDate;

    public EditDeliveryInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param deliveryInfo Delivery Info.
     * @param isShowInfo Whether to show DeliveryInfoFragment after saving
     * @return A new instance of fragment DeliveryInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditDeliveryInfoFragment newInstance(DeliveryInfo deliveryInfo, boolean isShowInfo) {
        EditDeliveryInfoFragment fragment = new EditDeliveryInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_INFO, deliveryInfo);
        args.putBoolean(EXTRA_IS_SHOW_INFO, isShowInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            deliveryInfo =  (DeliveryInfo) savedInstanceState.getSerializable(EXTRA_INFO);
        } else {
            deliveryInfo = (DeliveryInfo) getArguments().getSerializable(EXTRA_INFO);
        }

        if (deliveryInfo == null) {
            deliveryInfo = new DeliveryInfo();
        }

        sharedPref = new SharedPref(getContext());
        isShowInfo = getArguments().getBoolean(EXTRA_IS_SHOW_INFO, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_delivery_info, container, false);
        initComponents();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        deliveryInfo.fullName = etBuyerName.getText().toString();
        deliveryInfo.email = etEmail.getText().toString();
        deliveryInfo.phone = etPhone.getText().toString();
        deliveryInfo.address = etAddress.getText().toString();
        deliveryInfo.shippingOption = shippingSpinner.getSelectedItemPosition();
        deliveryInfo.shipDate = new Date(shipDate);
        outState.putSerializable(EXTRA_INFO, deliveryInfo);
    }

    private void initComponents() {
        buyerNameLayout = rootView.findViewById(R.id.name_lyt);
        emailLayout = rootView.findViewById(R.id.email_lyt);
        phoneLayout = rootView.findViewById(R.id.phone_lyt);
        addressLayout = rootView.findViewById(R.id.address_lyt);

        // form view
        etBuyerName = rootView.findViewById(R.id.et_name);
        etEmail = rootView.findViewById(R.id.et_email);
        etPhone = rootView.findViewById(R.id.et_phone);
        etAddress = rootView.findViewById(R.id.et_address);
        etComment = rootView.findViewById(R.id.et_comment);

        etBuyerName.setText(deliveryInfo.fullName);
        etEmail.setText(deliveryInfo.email);
        etPhone.setText(deliveryInfo.phone);
        etAddress.setText(deliveryInfo.address);
        etComment.setText(deliveryInfo.comment);

        etBuyerName.addTextChangedListener(new EditDeliveryInfoTextWatcher(etBuyerName));
        etEmail.addTextChangedListener(new EditDeliveryInfoTextWatcher(etEmail));
        etPhone.addTextChangedListener(new EditDeliveryInfoTextWatcher(etPhone));
        etAddress.addTextChangedListener(new EditDeliveryInfoTextWatcher(etAddress));
        etComment.addTextChangedListener(new EditDeliveryInfoTextWatcher(etComment));

        final List<String> shippingList = new ArrayList<>();
        shippingList.add(getString(R.string.choose_shipping_option));
        shippingList.add(getString(R.string.shipping_option_standard));
        shippingList.add(getString(R.string.shipping_option_premium));

        // Initialize and set Adapter
        shippingSpinner = rootView.findViewById(R.id.spn_shipping_options);
        ArrayAdapter shippingAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, shippingList);
        shippingSpinner.setAdapter(shippingAdapter);

        final EditText etShipping = rootView.findViewById(R.id.et_shipping_options);
        etShipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shippingSpinner.performClick();
            }
        });

        shippingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    etShipping.setText("");
                    shipDate = -1;
                    EditText et = rootView.findViewById(R.id.et_shipping_date);
                    et.setText("");
                    et.setEnabled(false);
                    rootView.findViewById(R.id.ib_shipping_date).setVisibility(View.GONE);
                } else if (position == 1) {
                    etShipping.setText(shippingList.get(position));
                    shipDate = getDateAfter(getResources().getInteger(R.integer.standard_delivery_days));
                    EditText et = rootView.findViewById(R.id.et_shipping_date);
                    et.setText(Tools.getFormattedDateSimple(shipDate));
                    et.setEnabled(false);
                    rootView.findViewById(R.id.ib_shipping_date).setVisibility(View.GONE);
                } else {
                    etShipping.setText(shippingList.get(position));
                    shipDate = getDateAfter(getResources().getInteger(R.integer.premium_delivery_days));
                    EditText et = rootView.findViewById(R.id.et_shipping_date);
                    et.setText(Tools.getFormattedDateSimple(shipDate));
                    et.setEnabled(true);
                    rootView.findViewById(R.id.ib_shipping_date).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (deliveryInfo.shippingOption >= 0) {
            shippingSpinner.setSelection(deliveryInfo.shippingOption);
        }

        EditText etShippingDate = rootView.findViewById(R.id.et_shipping_date);
        etShippingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDatePicker();
            }
        });

        if (deliveryInfo.shipDate != null) {
            shipDate = deliveryInfo.shipDate.getTime();
            etShippingDate.setText(Tools.getFormattedDateSimple(shipDate));
        }

        rootView.findViewById(R.id.ib_shipping_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogDatePicker();
            }
        });
        rootView.findViewById(R.id.bt_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDeliveryInfo();
            }
        });
    }

    public boolean saveDeliveryInfo() {
        if (!validateName()) {
            Snackbar.make(rootView, R.string.invalid_name, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!validateEmail()) {
            Snackbar.make(rootView, R.string.invalid_email, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!validatePhone()) {
            Snackbar.make(rootView, R.string.invalid_phone, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!validateAddress()) {
            Snackbar.make(rootView, R.string.invalid_address, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!validateShipping()) {
            Snackbar.make(rootView, R.string.invalid_shipping, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!validateShipDate()) {
            Snackbar.make(rootView, R.string.invalid_ship_date, Snackbar.LENGTH_SHORT).show();
            return false;
        }

        deliveryInfo.fullName = etBuyerName.getText().toString();
        deliveryInfo.email = etEmail.getText().toString();
        deliveryInfo.phone = etPhone.getText().toString();
        deliveryInfo.address = etAddress.getText().toString();
        deliveryInfo.shippingOption = shippingSpinner.getSelectedItemPosition();
        deliveryInfo.shipDate = new Date(shipDate);
        sharedPref.setDeliveryInfo(deliveryInfo);

        // hide keyboard
        hideKeyboard();

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if (isShowInfo) {
            transaction.setCustomAnimations(R.anim.card_flip_right_in, R.anim.card_flip_right_out,
                    R.anim.card_flip_left_in, R.anim.card_flip_left_out);
            transaction.remove(EditDeliveryInfoFragment.this);
            transaction.replace(R.id.delivery_info_fragment, DeliveryInfoFragment.newInstance(deliveryInfo, true), DeliveryInfoFragment.TAG);
            transaction.commit();
        } else {
            transaction.remove(this).commit();
        }

        return true;
    }

    private long getDateAfter(int days) {
        Calendar result = Calendar.getInstance();
        result.set(result.get(Calendar.YEAR), result.get(Calendar.MONTH), result.get(Calendar.DAY_OF_MONTH) + days);
        return result.getTimeInMillis();
    }

    private void dialogDatePicker() {
        DatePickerDialogFragment dialog = DatePickerDialogFragment.newInstance(
                getDateAfter(getResources().getInteger(R.integer.premium_delivery_days)),
                null,
                shipDate,
                DatePickerDialogFragment.DatePickerDialogMode.SHIPPING_DATE);
        dialog.show(getActivity().getSupportFragmentManager(), DatePickerDialogFragment.TAG);
    }

    public void setShippingDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        shipDate = calendar.getTimeInMillis();
        EditText etShippingDate = rootView.findViewById(R.id.et_shipping_date);
        etShippingDate.setText(Tools.getFormattedDateSimple(shipDate));
//        etShippingDate.setEnabled(false);
    }

    // validation method
    private boolean validateEmail() {
        String str = etEmail.getText().toString().trim();
        if (str.isEmpty() || !Tools.isValidEmail(str)) {
            emailLayout.setError(getString(R.string.invalid_email));
            requestFocus(etEmail);
            return false;
        } else {
            emailLayout.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateName() {
        String str = etBuyerName.getText().toString().trim();
        if (str.isEmpty()) {
            buyerNameLayout.setError(getString(R.string.invalid_name));
            requestFocus(etBuyerName);
            return false;
        } else {
            buyerNameLayout.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePhone() {
        String str = etPhone.getText().toString().trim();
        if (str.isEmpty()) {
            phoneLayout.setError(getString(R.string.invalid_phone));
            requestFocus(etPhone);
            return false;
        } else {
            phoneLayout.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateAddress() {
        String str = etAddress.getText().toString().trim();
        if (str.isEmpty()) {
            addressLayout.setError(getString(R.string.invalid_address));
            requestFocus(etAddress);
            return false;
        } else {
            addressLayout.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateShipping() {
        int pos = shippingSpinner.getSelectedItemPosition();
        if (pos == 0) {
            return false;
        }
        return true;
    }

    private boolean validateShipDate() {
        if (shipDate < 0 || shipDate < System.currentTimeMillis() ) {
            return false;
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class EditDeliveryInfoTextWatcher implements TextWatcher {
        private View view;

        private EditDeliveryInfoTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_email:
                    validateEmail();
                    break;
                case R.id.et_name:
                    validateName();
                    break;
                case R.id.et_phone:
                    validatePhone();
                    break;
                case R.id.address:
                    validateAddress();
                    break;
            }
        }
    }

}