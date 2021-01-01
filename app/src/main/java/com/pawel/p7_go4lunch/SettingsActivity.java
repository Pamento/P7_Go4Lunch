package com.pawel.p7_go4lunch;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

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
        }
    }

    public void deleteUserAccount(View view) {
        openDialog();
    }

    private void openDialog() {
        String title = getString(R.string.delete_account_title);
        String message = getString(R.string.delete_account_message);
        String positiveBtn = getString(R.string.btn_ok);
        String negativeBtn = getString(R.string.btn_cancel);
        DialogWidget dialog = new DialogWidget(true, getBaseContext(), title,message,negativeBtn,positiveBtn);
        dialog.show(getSupportFragmentManager(), Const.DELETE_ALERT_DIALOG);
    }

    @Override
    public void OnPositiveBtnAlertDialogClick() {
        ViewWidgets.showSnackBar(1,mView,"deletion WORKS");
        // TODO uncomment for release version
//        AuthUI.getInstance().delete(this)
//                .addOnCompleteListener(task -> {
//                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//                    navController.navigate(R.id.navigation_map_view);
//                });
    }

    @Override
    public void OnNegativeBtnAlertDialogClick() {
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference test = SettingsFragment.this.findPreference("delete_account");
            if (test != null) {
                test.setLayoutResource(R.layout.delete_btn_layout);
            } else {
                ViewWidgets.showSnackBar(1,getView(),"Indentured error. please retry.");
            }
        }
    }
}