package com.ecotioco.gaze.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.data.SharedPref;
import com.ecotioco.gaze.model.DeliveryInfo;
import com.ecotioco.gaze.model.Order;
import com.ecotioco.gaze.utils.Tools;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeliveryInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeliveryInfoFragment extends Fragment {
    public static final String TAG = "DeliveryInfo";

    private static final String EXTRA_INFO = "key.INFO";
    private static final String EXTRA_IS_SHOW_EDIT = "key.IS_SHOW_EDIT";

    // TODO: Rename and change types of parameters
    private DeliveryInfo deliveryInfo;
    private boolean isShowEdit;
    private View rootView;

    public DeliveryInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param deliveryInfo Delivery Info.
     * @param isShowEdit   Is Show Edit Button
     * @return A new instance of fragment DeliveryInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeliveryInfoFragment newInstance(DeliveryInfo deliveryInfo, boolean isShowEdit) {
        DeliveryInfoFragment fragment = new DeliveryInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_INFO, deliveryInfo);
        args.putBoolean(EXTRA_IS_SHOW_EDIT, isShowEdit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deliveryInfo = (DeliveryInfo) getArguments().getSerializable(EXTRA_INFO);
            isShowEdit = getArguments().getBoolean(EXTRA_IS_SHOW_EDIT, true);
        }

        if (deliveryInfo == null) {
            SharedPref sharedPref = new SharedPref(getContext());
            deliveryInfo = sharedPref.getDeliveryInfo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_delivery_info, container, false);

        if (deliveryInfo == null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.card_flip_right_in, R.anim.card_flip_right_out,
                    R.anim.card_flip_left_in, R.anim.card_flip_left_out);
            transaction.remove(DeliveryInfoFragment.this);
            transaction.replace(R.id.delivery_info_fragment, EditDeliveryInfoFragment.newInstance(null, true), EditDeliveryInfoFragment.TAG);
            transaction.commit();
            return rootView;
        }
        ((TextView) rootView.findViewById(R.id.name_phone)).setText(deliveryInfo.fullName + " | " + deliveryInfo.phone);
        ((TextView) rootView.findViewById(R.id.email)).setText(deliveryInfo.email);
        ((TextView) rootView.findViewById(R.id.address)).setText(deliveryInfo.address);
        ((TextView) rootView.findViewById(R.id.shipping_option))
                .setText(Order.getShippingOption(getContext(), deliveryInfo.shippingOption) + " - " + getString(R.string.receive_by) + " "
                        + Tools.getFormattedDateSimple(deliveryInfo.shipDate));
        TextView tvComment = rootView.findViewById(R.id.comment);
        if (!TextUtils.isEmpty(deliveryInfo.comment)) {
            tvComment.setText(getString(R.string.hint_comment) + ": " + deliveryInfo.comment);
        }

        ImageView editBtn = rootView.findViewById(R.id.edit);
        if (isShowEdit) {
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.card_flip_right_in, R.anim.card_flip_right_out,
                            R.anim.card_flip_left_in, R.anim.card_flip_left_out);
                    transaction.remove(DeliveryInfoFragment.this);
                    transaction.replace(R.id.delivery_info_fragment, EditDeliveryInfoFragment.newInstance(deliveryInfo, true), EditDeliveryInfoFragment.TAG);
                    transaction.commit();
                }
            });
        } else {
            editBtn.setVisibility(View.GONE);
        }

        return rootView;
    }
}