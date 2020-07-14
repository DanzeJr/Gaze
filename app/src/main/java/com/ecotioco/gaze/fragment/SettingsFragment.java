package com.ecotioco.gaze.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.utils.PermissionUtil;
import com.ecotioco.gaze.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    public static final String TAG = "Settings";

    private View mRootView;

    private int mTotalItems = 0;
    private int mFailedPage = 0;

    public SettingsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CategoryDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public static class MainSettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
        public static final String TAG = "MainSettings";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.setting_preferences, rootKey);

            // Ringtone
            Preference ringTonePref = findPreference(getString(R.string.pref_title_ringtone));
            setRingtoneSummary(ringTonePref.getKey());
            ringTonePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    RingtoneDialogFragment ringtoneDialogFragment = RingtoneDialogFragment
                            .newInstance(getString(R.string.pref_title_ringtone),
                                    getString(R.string.pref_title_ringtone),
                                    getString(R.string.OK),
                                    getString(R.string.CANCEL));
                    ringtoneDialogFragment.show(fragmentTransaction, RingtoneDialogFragment.TAG);
                    return true;
                }
            });

            // Notification
            if (!PermissionUtil.isStorageGranted(getActivity())) {
                PreferenceCategory prefCat = findPreference(getString(R.string.pref_group_notif));
                prefCat.setTitle(Html.fromHtml("<b>" + getString(R.string.pref_group_notif)
                        + "</b><br><i>" + getString(R.string.grant_permission_storage) + "</i>"));
                SwitchPreference notifPref = findPreference(getString(R.string.pref_title_notif));
                notifPref.setEnabled(false);
            }

            // Term
            findPreference(getString(R.string.pref_title_term)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showTermDialog(getActivity());
                    return false;
                }
            });

            // Build Version
            findPreference(getString(R.string.pref_title_build)).setSummary(Tools.getVersionName(getContext()) + " ( " + Tools.getDeviceID(getActivity()) + " )");

            // Email
            findPreference(getString(R.string.pref_title_contact_us)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getString(R.string.pref_title_contact_us), getString(R.string.developer_email));
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Email Copied to Clipboard", Snackbar.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(newValue.toString());
                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                // For all other preferences, set the summary to the value's simple string representation.
                preference.setSummary(newValue.toString());
            }
            return true;
        }

        private void showTermDialog(Activity activity) {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            AlertDialogFragment dialog =
                    AlertDialogFragment.newInstance(activity.getString(R.string.pref_title_term),
                            activity.getString(R.string.content_term),
                            activity.getString(R.string.OK), null);
            dialog.show(fragmentTransaction, AlertDialogFragment.TAG);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.pref_title_ringtone))) {
                setRingtoneSummary(key);
            }
        }

        private void setRingtoneSummary(String key) {
            String uri = getPreferenceManager().getSharedPreferences().getString(key, getString(R.string.default_ringtone));
            Preference preference = findPreference(key);
            if (TextUtils.isEmpty(uri)) {
                // Empty values correspond to 'silent' (no ringtone).
                preference.setSummary(R.string.silent_ringtone);
            } else if (uri.equalsIgnoreCase(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).getPath())) {
                preference.setSummary(getString(R.string.default_ringtone));
            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(uri));
                if (ringtone == null) {
                    // Clear the summary if there was a lookup error.
                    preference.setSummary(null);
                } else {
                    // Set the summary to reflect the new ringtone display name.
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            }
        }
    }

}