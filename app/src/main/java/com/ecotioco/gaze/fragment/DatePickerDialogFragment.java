package com.ecotioco.gaze.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.utils.Tools;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DatePickerDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public static final String TAG = "DatePickerDialog";

    private static final String EXTRA_MIN_DATE = "key.MIN_DATE";
    private static final String EXTRA_MAX_DATE = "key.MAX_DATE";
    private static final String EXTRA_SELECTED_DATE = "key.SELECTED_DATE";
    private static final String EXTRA_MODE = "key.MAX_MODE";

    public enum DatePickerDialogMode implements Serializable {
        NONE,
        SHIPPING_DATE
    }

    private long minDate = -1l;
    private long maxDate = -1l;
    private long selectedDate;
    private DatePickerDialogMode mode;

    public DatePickerDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param minDate Minimum Date.
     * @param maxDate Maximum Date.
     * @param selectedDate Selected Date
     * @param mode Mode
     * @return A new instance of fragment DatePickerDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DatePickerDialogFragment newInstance(Long minDate, Long maxDate, Long selectedDate, DatePickerDialogMode mode) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        if (minDate != null) {
            args.putLong(EXTRA_MIN_DATE, minDate);
        }
        if (maxDate != null) {
            args.putLong(EXTRA_MAX_DATE, maxDate);
        }
        if (selectedDate != null) {
            args.putLong(EXTRA_SELECTED_DATE, selectedDate);
        }
        if (mode == null) {
            mode = DatePickerDialogMode.NONE;
        }
        args.putSerializable(EXTRA_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param minDate Minimum Date.
     * @param maxDate Maximum Date.
     * @param selectedDate Selected Date
     * @param mode Mode
     * @return A new instance of fragment DatePickerDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DatePickerDialogFragment newInstance(Long minDate, Long maxDate, Date selectedDate, DatePickerDialogMode mode) {
        return newInstance(minDate, maxDate, selectedDate == null ? null : selectedDate.getTime(), mode);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param minDate Minimum Date.
     * @param maxDate Maximum Date.
     * @param mode Mode
     * @return A new instance of fragment DatePickerDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DatePickerDialogFragment newInstance(Long minDate, Long maxDate, DatePickerDialogMode mode) {
        return newInstance(minDate, maxDate, (Long) null, mode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            minDate = getArguments().getLong(EXTRA_MIN_DATE, -1l);
            maxDate = getArguments().getLong(EXTRA_MAX_DATE, -1l);
            selectedDate = getArguments().getLong(EXTRA_SELECTED_DATE, System.currentTimeMillis());
            mode = (DatePickerDialogMode) getArguments().getSerializable(EXTRA_MODE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar currentCalender = Calendar.getInstance();
        currentCalender.setTimeInMillis(selectedDate);
        DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.DatePickerTheme,this,
                currentCalender.get(Calendar.YEAR), currentCalender.get(Calendar.MONTH), currentCalender.get(Calendar.DAY_OF_MONTH));

        if (minDate > 0) {
            dialog.getDatePicker().setMinDate(minDate);
        }
        if (maxDate > 0) {
            dialog.getDatePicker().setMaxDate(maxDate);
        }
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        switch (mode) {
            case SHIPPING_DATE: {
                handleShippingDate(year, month, dayOfMonth);
                break;
            }
        }
    }

    private void handleShippingDate(int year, int month, int dayOfMonth) {
        EditDeliveryInfoFragment fragment = (EditDeliveryInfoFragment) getActivity().getSupportFragmentManager().findFragmentByTag(EditDeliveryInfoFragment.TAG);
        fragment.setShippingDate(year, month, dayOfMonth);
        dismiss();
    }
}