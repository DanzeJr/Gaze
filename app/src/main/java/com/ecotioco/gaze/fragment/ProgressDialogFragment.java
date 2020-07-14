package com.ecotioco.gaze.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ecotioco.gaze.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgressDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgressDialogFragment extends DialogFragment {
    public static final String TAG = "ProgressDialog";

    private static final String EXTRA_TITLE = "key.TITLE";
    private static final String EXTRA_MESSAGE = "key.MESSAGE";
    private static final String EXTRA_PROGRESS = "key.PROGRESS";

    View contentView;
    private String title = null;
    private String message = null;
    private double progress = 0;

    public ProgressDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Title.
     * @param message Message.
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgressDialogFragment newInstance(@NonNull String title, String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        args.putString(EXTRA_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        if (getArguments() != null) {
            title = getArguments().getString(EXTRA_TITLE);
            message = getArguments().getString(EXTRA_MESSAGE);
        }

        if (savedInstanceState != null) {
            progress = savedInstanceState.getDouble(EXTRA_PROGRESS, 0);
            message = savedInstanceState.getString(EXTRA_MESSAGE, message);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        contentView = getLayoutInflater().inflate(R.layout.fragment_progress_dialog, null);
        setDialogProgress(progress, message);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(contentView);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(EXTRA_PROGRESS, progress);
        outState.putString(EXTRA_MESSAGE, message);
    }

    public void setDialogProgress(double progress, String message) {
        this.progress = progress;
        ((ProgressBar) contentView.findViewById(R.id.progress_circular)).setProgress((int) Math.round(progress));
        if (message != null) {
            this.message = message;
            ((TextView) contentView.findViewById(R.id.progress_text)).setText(message);
        }
    }
}