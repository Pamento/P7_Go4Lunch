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

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pawel.p7_go4lunch.databinding.ActivityMainBinding;
import com.pawel.p7_go4lunch.databinding.NavigationDrawerHeaderBinding;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.viewModels.MainActivityViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TESTING_MAPS";
    private static final String TAG2 = "TOOLBAR_SEARCH";
    private MainActivityViewModel mMainActivityViewModel;
    private ActivityMainBinding binding;
    private View view;
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
        mMainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        mMainActivityViewModel.init();
        if (isCurrentUserLogged()) {
            if (isMapsServiceOk()) {
                startMainActivity();
            } else {
                ViewWidgets.showSnackBar(1, view, getString(R.string.localisation_not_available));
            }
        } else {
            startSignInActivity();
        }
    }

    // Check if service Google Maps is available
    public boolean isMapsServiceOk() {
        Log.i(TAG, "isMapsServiceOk: START");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, Const.ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            ViewWidgets.showSnackBar(1, view, getString(R.string.google_maps_not_available));
        }
        return false;
    }

    // ____________ Main Activity with 3 fragments _____________________
    private void startMainActivity() {
        Log.i(TAG, "startMainActivity: START");
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
        getMenuInflater().inflate(R.menu.toolbar_search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.toolbar_search_icon);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(searchHint);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //TODO "search" get value and do actions with
                ViewWidgets.showSnackBar(0, view, "submit research");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //TODO "search" get value and do actions with
                Toast.makeText(MainActivity.this, "searching", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        return true;
    }

    // ____________ Toolbar search on result _____________________
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_search_icon) {
            //TODO add action
            Log.i(TAG2, "onOptionsItemSelected: ");
            ViewWidgets.showSnackBar(0, view, "Search");
            return true;
        } else return super.onOptionsItemSelected(item);
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

        // set information on the user into the drawer header
        if (getCurrentUser() != null) {
            FirebaseUser user = getCurrentUser();
            View drawerHeader = binding.navDrawerView.getHeaderView(0);
            NavigationDrawerHeaderBinding headerBinding = NavigationDrawerHeaderBinding.bind(drawerHeader);
            Glide.with(this)
                    .load(getCurrentUser().getPhotoUrl())
                    .error(R.drawable.ic_persona_placeholder)
                    .placeholder(R.drawable.ic_persona_placeholder)
                    .fitCenter()
                    .circleCrop()
                    .into(headerBinding.navDrawerUserImage);
            headerBinding.navDrawerUserEmail.setText(user.getEmail());
            headerBinding.navDrawerUserFullName.setText(user.getDisplayName());
        }
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

    // ____________ Firebase Authentication builder _____________________
    private void startSignInActivity() {
        Log.i(TAG, "startSignInActivity: START");
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

    // ____________ Firebase Authentication on result _____________________
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser firebaseUser = getCurrentUser();
                assert firebaseUser != null;
                String uid = firebaseUser.getUid();
                String name = firebaseUser.getDisplayName() == null ? "" : firebaseUser.getDisplayName();
                String email = firebaseUser.getEmail() == null ? "" : firebaseUser.getEmail();
                String urlImage = firebaseUser.getPhotoUrl() == null ? "" : firebaseUser.getPhotoUrl().toString();
                mMainActivityViewModel.createUser(uid, name, email, urlImage);
                ViewWidgets.showSnackBar(0, view, getString(R.string.login_succeed) + uid + "_" + name + "_" + email + "_" + urlImage);
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back background_facebook_btn. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                assert response != null;
                ViewWidgets.showSnackBar(0, view, response.getEmail());
            }
        }
    }

    private void logOutUser() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(this, aVoid ->
        {
            if (getCurrentUser() == null) {
                ViewWidgets.showSnackBar(0, view, "Action to logout");
//                Intent intent = new Intent(MainActivity.this);
//                startActivity(intent);
            }
        });

    }

    @Nullable
    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    protected Boolean isCurrentUserLogged() {
        return (this.getCurrentUser() != null);
    }

//    protected Boolean havePermissions() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, Const.PERMISSIONS, Const.LOCATION_PERMISSION_REQUEST_CODE);
//        }
//    }
}