package com.pawel.p7_go4lunch;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.pawel.p7_go4lunch.databinding.SettingsActivityBinding;
import com.pawel.p7_go4lunch.utils.DialogWidget;
import com.pawel.p7_go4lunch.utils.ViewWidgets;

public class SettingsActivity extends AppCompatActivity implements DialogWidget.DialogWidgetListener {

    public static final String DELETE_ALERT_DIALOG = "delete_alert_dialog";
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
        DialogWidget dialog = new DialogWidget();
        dialog.show(getSupportFragmentManager(), DELETE_ALERT_DIALOG);
    }

    @Override
    public void OnPositiveBtnAlertDialogClick() {
        ViewWidgets.showSnackBar(1,mView,"deletion WORKS");
//        AuthUI.getInstance().delete(this)
//                .addOnCompleteListener(task -> {
//                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//                    navController.navigate(R.id.navigation_map_view);
//                });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference test = SettingsFragment.this.findPreference("delete_account");
            if (test != null) {
                test.setLayoutResource(R.layout.delete_btn_layout);
//                test.setOnPreferenceClickListener(preference -> {
//                    ViewWidgets.showSnackBar(1,getView(),"delete ?");
//                    return true;
//                });
            } else {
                ViewWidgets.showSnackBar(1,getView(),"Indentured error. please retry.");
            }
        }
    }
}