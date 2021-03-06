package com.pawel.p7_go4lunch.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.firebase.ui.auth.AuthUI;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.SettingsActivityBinding;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.DialogWidget;
import com.pawel.p7_go4lunch.utils.ViewWidgets;

public class SettingsActivity extends AppCompatActivity implements DialogWidget.DialogWidgetListener {

    SettingsActivityBinding binding;
    View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.pawel.p7_go4lunch.databinding.SettingsActivityBinding
                .inflate(getLayoutInflater());
        mView = binding.getRoot();
        setContentView(mView);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // This value is about "home" id: https://developer.android.com/reference/android/R.id#home
        // Command: <if (item.getItemId() == R.id.home)> don't give expected result.
        if (item.getItemId() == 16908332) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteUserAccount(View view) {
        openDialog();
    }

    private void openDialog() {
        String title = getString(R.string.delete_account_title);
        String message = getString(R.string.delete_account_message);
        String positiveBtn = getString(R.string.btn_ok);
        String negativeBtn = getString(R.string.btn_cancel);
        DialogWidget dialog = new DialogWidget(true, getBaseContext(), title, message, negativeBtn, positiveBtn);
        dialog.show(getSupportFragmentManager(), Const.DELETE_ALERT_DIALOG);
    }

    @Override
    public void OnPositiveBtnAlertDialogClick() {
        ViewWidgets.showSnackBar(1, mView, "deletion WORKS");
        AuthUI.getInstance().delete(this)
                .addOnCompleteListener(task -> {
                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                    navController.navigate(R.id.navigation_map_view);
                });
    }

    @Override
    public void OnNegativeBtnAlertDialogClick() {
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference deleteAccountBtn = SettingsFragment.this.findPreference("delete_account");
            if (deleteAccountBtn != null) {
                deleteAccountBtn.setLayoutResource(R.layout.delete_btn_layout);
            }
        }
    }
}