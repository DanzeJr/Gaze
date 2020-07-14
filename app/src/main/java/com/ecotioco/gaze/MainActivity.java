package com.ecotioco.gaze;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.ecotioco.gaze.data.SharedPref;
import com.ecotioco.gaze.fragment.HomeFragment;

public class MainActivity extends AppCompatActivity {
    public static final String FRAGMENT_KEY = "TargetFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null && (getIntent() == null || getIntent().getStringExtra(FRAGMENT_KEY) == null)) {
            SharedPref sharedPref = new SharedPref(this);
            // launch instruction when first launch
            if (sharedPref.isFirstLaunch()) {
                startActivity(new Intent(this, ActivityInstruction.class));
                sharedPref.setFirstLaunch(false);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dynamic_fragment, HomeFragment.newInstance(), HomeFragment.TAG)
                    .commit();
        }
    }
}
