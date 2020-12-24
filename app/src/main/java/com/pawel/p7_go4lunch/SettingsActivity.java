package com.pawel.p7_go4lunch;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.pawel.p7_go4lunch.databinding.SettingsActivityBinding;
import com.pawel.p7_go4lunch.tools.ViewWidgets;

public class SettingsActivity extends AppCompatActivity {

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

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final String TAG = "SETTINGS_ACTIVITY";
        View view;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            //view = getView();
            Log.d(TAG, "onCreatePreferences: SETTINGS WORK ");
//            Context context = getPreferenceManager().getContext();
//            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

            Preference test = SettingsFragment.this.findPreference("delete_account");
            if (test != null) {
                test.setLayoutResource(R.layout.delete_btn_layout);
                test.setOnPreferenceClickListener(preference -> {
                    ViewWidgets.showSnackBar(1,getView(),"delete ?");
                    Log.i(TAG, "onPreferenceClick: O!O!O!O!O!O!O!O!O!");
                    return true;
                });
            } else {
                ViewWidgets.showSnackBar(1,getView(),"DELETE_btn == null !");
            }
        }

//        @Override
//        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//            Log.i(TAG, "onCreateView: IN");
//            if (container != null) {
//                Log.i(TAG, "onCreateView: IN__CONTAINER_view "+container.findViewById(R.id.delete_account_btn));
//
//                for(int index = 0; index < container.getChildCount(); index++) {
//                    View nextChild = container.getChildAt(index);
//                    Log.i(TAG, "onCreateView: " + index + " __ " + nextChild);
//                }
//            }
//            return super.onCreateView(inflater, container, savedInstanceState);
//        }
//
//        @Override
//        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//            super.onViewCreated(view, savedInstanceState);
//            this.view = view;
//            FrameLayout fl = requireView().findViewById(R.id.settings);
//            getPreferences();
//            Log.d(TAG, "on__View__Created: SETTINGS WORK " + view + "\n frameLayout " + fl);
//        }
//
        @Override
        public void onStart() {
            super.onStart();
//            LinearLayout lit = this.view.findViewById(R.id.delete_account_linear_layout);
//            Context context = requireView().getContext();
//            //Button btn2 = this.view.findViewById(R.id.delete_account_btn);
//            Log.i(TAG, "onStart: button__layout " + lit );
            Preference button = findPreference("delete_account");
            if (button != null) {
                button.setOnPreferenceClickListener(preference -> {
                    ViewWidgets.showSnackBar(1,getView(),"ON __Start");
                    return true;
                });
            }
        }

        private void getPreferences() {
            //View lly = this.view.findViewById(R.id.delete_account_linear_layout);
            Log.d(TAG, "getPreferences: LinearLayout " + this.view);
            //Preference deleteAccount = findPreference("delete_account");
            Preference button = findPreference("delete_account");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ViewWidgets.showSnackBar(1,getView(),"delete ?");
                    return false;
                }
            });
            Log.i(TAG, "getPreferences: delete_account_btn " + button);

//            if (this.view != null) {
//
//                Log.i(TAG, "getPreferences: inside " + this.view);
//                Button btn = (Button) this.view.findViewById(R.id.delete_account_btn);
//
//                if (btn != null) {
//                    btn.setOnClickListener(v -> {
//                        Log.i(TAG, "getPreferences: onClick " + btn);
//                        ViewWidgets.showSnackBar(1,getView(),"Delete your account, Really ?");
//                    });
//                }
//            }
//            if (deleteAccount != null) {
//                deleteAccount.setOnPreferenceClickListener(preference -> {
//                    Log.i(TAG, "getPreferences: onClick");
//                    ViewWidgets.showSnackBar(1,getView(),"Are you sur to delete your account ?");
//                    return false;
//                });
////                deleteAccount.setOnPreferenceChangeListener((preference, newValue) -> {
////                    return false;
////                });
//            }
        }

//        private void deleteUserAccount() {
//            AuthUI.getInstance().delete();
//        }
    }
}