package com.pawel.p7_go4lunch;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.pawel.p7_go4lunch.tools.ViewWidgets;

public class SettingsActivity extends AppCompatActivity {

    //View view = findViewById(R.id.settings_layout);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference sync = findPreference("sync");
            if (sync != null) {
                sync.setOnPreferenceChangeListener((preference, newValue) -> {
                    ViewWidgets.showSnackBar(0,getView(),"Settings test");
                    return false;
                });
            }
        }
    }
}