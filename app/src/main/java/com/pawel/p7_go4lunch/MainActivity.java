package com.pawel.p7_go4lunch;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.databinding.ActivityMainBinding;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.viewModels.MainActivityViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TESTING_MAPS";
    private MainActivityViewModel mMainActivityViewModel;
    private ActivityMainBinding binding;
    private View view;
    //private SharedPreferences mPrefs;
    private LocalAppSettings mPrefs;
    public static boolean mLocationPermissionGranted = false;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.FacebookBuilder().build());
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);
        setNavigationDrawer();
        // retrieve the settings from Settings Activity
        mPrefs = new LocalAppSettings(this);
        mMainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        mMainActivityViewModel.init();
        if (isCurrentUserLogged()){
            getLocationPermission();
            if (isMapsServiceOk() && mLocationPermissionGranted) {
                startMainActivity();
            } else {
                ViewWidgets.showSnackBar(1,view,getString(R.string.localisation_not_available));
            }
        } else {
            startSignInActivity();
        }
    }

//    private void loadSettings() {
//        mPrefs = new LocalAppSettings(MainActivity.this);
//    }

    // Check if service Google Maps is available
    public boolean isMapsServiceOk() {
        boolean isLocalisationSet = mPrefs.isLocalisation();
        Log.i(TAG, "isMapsServiceOk: prefs LOCALISATION "+isLocalisationSet);
        if (isLocalisationSet) {
            int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
            if (available == ConnectionResult.SUCCESS) {
                return true;
            } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, Const.ERROR_DIALOG_REQUEST);
                dialog.show();
            } else {
                ViewWidgets.showSnackBar(1,view,getString(R.string.google_maps_not_available));
            }
        }
        return false;
    }



    // ____________ Main Activity _____________________
    private void startMainActivity() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map_view, R.id.navigation_list_view, R.id.navigation_workmates)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    // ____________ Toolbar search _____________________
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String searchHint = getString(R.string.search_hint);
        //MenuInflater inf = getMenuInflater();
        getMenuInflater().inflate(R.menu.toolbar_search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.toolbar_search_icon);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(searchHint);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //TODO "search" get value and do actions with
                ViewWidgets.showSnackBar(0,view, "submit research");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //TODO "search" get value and do actions with
                Toast.makeText(MainActivity.this,"searching",Toast.LENGTH_LONG).show();
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_search_icon) {
            //TODO add action
            ViewWidgets.showSnackBar(0, view, "Search");
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNavigationDrawer() {
        binding.navDrawerView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.nav_app_bar_open_drawer_description,
                R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.about_the_restaurant_dest) {
            initOtherActivity(AboutRestaurantActivity.class);
        } else if (itemId == R.id.settings_activity) {
            initOtherActivity(SettingsActivity.class);
        } else {
            logOutUser();
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initOtherActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void startSignInActivity() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.login_firebase)
                .setGoogleButtonId(R.id.google_btn)
                .setFacebookButtonId(R.id.facebook_btn)
                .setEmailButtonId(R.id.email_btn)
                .build();

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setAuthMethodPickerLayout(customLayout)
                        .setTheme(R.style.LoginTheme)
                        .build(),
                Const.RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                assert firebaseUser != null;
                String uid = firebaseUser.getUid();
                String name = firebaseUser.getDisplayName() == null ? "" : firebaseUser.getDisplayName();
                String email = firebaseUser.getEmail() == null ? "" : firebaseUser.getEmail();
                String urlImage = firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString();
                mMainActivityViewModel.createUser(uid,name,email,urlImage);
                ViewWidgets.showSnackBar(0, view, getString(R.string.login_succeed)+uid+"_"+name+"_"+email+"_"+urlImage);
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back background_facebook_btn. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                assert response != null;
                ViewWidgets.showSnackBar(0,view,response.getEmail());
            }
        }
    }

    private void logOutUser() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(this, aVoid ->
        {
            if (FirebaseAuth.getInstance().getCurrentUser() == null)
            {
                ViewWidgets.showSnackBar(0, view,"Action to logout");
//                Intent intent = new Intent(MainActivity.this);
//                startActivity(intent);
            }
        });

    }

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    protected Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Const.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),Const.COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,permissions,Const.LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,permissions,Const.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == Const.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = false;
                        return;
                    }
                    mLocationPermissionGranted = true;
                }
            }
        }
    }
}