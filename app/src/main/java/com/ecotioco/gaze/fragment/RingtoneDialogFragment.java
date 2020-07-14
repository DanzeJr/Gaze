package com.ecotioco.gaze.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RingtoneDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RingtoneDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    public static final String TAG = "RingtoneDialog";

    private static final String EXTRA_TITLE = "key.TITLE";
    private static final String EXTRA_PREF_KEY = "key.PREF_KEY";
    private static final String EXTRA_POSITIVE_BUTTON = "key.POSITIVE_BUTTON";
    private static final String EXTRA_NEGATIVE_BUTTON = "key.NEGATIVE_BUTTON";
    private static final String EXTRA_CURRENT_URI = "key.CURRENT_URI";

    // TODO: Rename and change types of parameters
    private String title;
    private String prefKey;
    private String positiveButtonText = null;
    private String negativeButtonText = null;
    private String currentUri;
    private Ringtone selectedRingtone;
    private List<String> ringtoneTitles;
    private List<String> ringtoneURIs;

    public RingtoneDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title              Title.
     * @param prefKey            Preference Key.
     * @param positiveButtonText Positive Button Text (null means not showing)
     * @param negativeButtonText Negative Button Text (null means not showing)
     * @return A new instance of fragment AlertDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RingtoneDialogFragment newInstance(@NonNull String title, @NonNull String prefKey,
                                                     @NonNull String positiveButtonText, String negativeButtonText) {
        RingtoneDialogFragment fragment = new RingtoneDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        args.putString(EXTRA_PREF_KEY, prefKey);
        args.putString(EXTRA_POSITIVE_BUTTON, positiveButtonText);
        args.putString(EXTRA_NEGATIVE_BUTTON, negativeButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = getArguments().getString(EXTRA_TITLE);
        prefKey = getArguments().getString(EXTRA_PREF_KEY);
        positiveButtonText = getArguments().getString(EXTRA_POSITIVE_BUTTON, null);
        negativeButtonText = getArguments().getString(EXTRA_NEGATIVE_BUTTON, null);

        Map<String, String> ringTones = Tools.getRingTones(getContext(), RingtoneManager.TYPE_NOTIFICATION);
        ringtoneTitles = new ArrayList<>(ringTones.values());
        ringtoneURIs = new ArrayList<>(ringTones.keySet());

        ringtoneTitles.add(0, getString(R.string.default_ringtone));
        ringtoneURIs.add(0, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).getPath());
        ringtoneTitles.add(1, getString(R.string.silent_ringtone));
        ringtoneURIs.add(1, "");

        currentUri = savedInstanceState == null
                ? PreferenceManager.getDefaultSharedPreferences(getContext()).getString(prefKey, "")
                : savedInstanceState.getString(EXTRA_CURRENT_URI);
        selectedRingtone = TextUtils.isEmpty(currentUri) ? null : RingtoneManager.getRingtone(getContext(), Uri.parse(currentUri));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, ringtoneTitles);
        int currentPos = ringtoneURIs.indexOf(currentUri);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(positiveButtonText, this)
                .setSingleChoiceItems(adapter, currentPos, this);
        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, this);
        }
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_CURRENT_URI, currentUri);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (selectedRingtone != null && selectedRingtone.isPlaying()) {
            selectedRingtone.stop();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_NEGATIVE) {
            dismiss();
        } else if (which == AlertDialog.BUTTON_POSITIVE) {
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit()
                    .putString(prefKey, currentUri)
                    .commit();
        } else {
            if (selectedRingtone != null && selectedRingtone.isPlaying()) {
                selectedRingtone.stop();
            }
            currentUri = ringtoneURIs.get(which);
            if (TextUtils.isEmpty(currentUri)) {
                selectedRingtone = null;
            } else {
                selectedRingtone = RingtoneManager.getRingtone(getContext(), Uri.parse(currentUri));
                selectedRingtone.play();
            }

        }
    }
}