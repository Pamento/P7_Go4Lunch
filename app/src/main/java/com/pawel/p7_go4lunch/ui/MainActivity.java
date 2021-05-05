package com.pawel.p7_go4lunch.ui;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.pawel.p7_go4lunch.BuildConfig;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.ActivityMainBinding;
import com.pawel.p7_go4lunch.databinding.NavigationDrawerHeaderBinding;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.GlideApp;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.MainActivityViewModel;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LocationListener {

    private static final String TAG = "AUTO_COM";
    private MainActivityViewModel mMainActivityViewModel;
    private ActivityMainBinding binding;
    private View view;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.FacebookBuilder().build());
    protected FirebaseUser mFirebaseUser;
    protected Location mCurrentLocation;
    protected String mCurrentLocationStr;
    protected double mLatitude, mLongitude;
    protected LatLng mLatLng;
    private LocalAppSettings mAppSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMainViewModel();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);
        setNavigationDrawer();
        if (isCurrentUserLogged()) {
            if (isMapsServiceOk()) {
                startMainActivity();
            } else {
                ViewWidgets.showSnackBar(1, view, getString(R.string.localisation_not_available));
            }
        } else {
            startSignInActivity();
        }
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        }
        getLocalAppSettings();
        //PlacesClient placesClient = Places.createClient(this);
    }

    private void initMainViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mMainActivityViewModel = new ViewModelProvider(this, vmf).get(MainActivityViewModel.class);
        mMainActivityViewModel.init();
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    // ____________ Toolbar search _____________________
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_search_menu, menu);
        setSearch(menu);
        return super.onCreateOptionsMenu(menu);
    }

    // ____________ Toolbar search on result _____________________
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toolbar_search_icon) {
            //TODO add action
            Log.i(TAG, "onOptionsItemSelected: ");
            ViewWidgets.showSnackBar(0, view, "Search");
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private void getLocalAppSettings() {
        mAppSettings = new LocalAppSettings(this);
    }

    private void setSearch(Menu menu) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.toolbar_search_icon).getActionView();
//        final MenuItem searchMenuItem = menu.findItem(R.id.toolbar_search_icon);
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, " __onQueryTextChange: " + newText);
//                if ((newText.length() > 0) && (newText.length() % 3 == 0)) {
//                    Log.i(TAG, "onQueryTextChange: ___________________________________________ " + newText.length());
//                }
                if ((newText.length() >= 3)) {
                    Log.i(TAG, "onQueryTextChange: ___________________________________________ " + newText.length());
                    mMainActivityViewModel
                            .streamCombinedAutocompleteDetailsPlace(
                                    newText,getResources().getString(R.string.app_language),
                                    mAppSettings.getRadius(),mCurrentLocationStr,mCurrentLocationStr);
                }
                return true; // signal that we consumed this event
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, " ...onQueryTextSubmit: " + query);

                return true; // signal that we consumed this event
            }
        });
        // Listen focus on SearchView for clean Map or List of Restaurants if true or rebuild NearBy search if false
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            mCurrentLocationStr = mMainActivityViewModel.getCurrentLocation();
            if (hasFocus) {
                mMainActivityViewModel.setAutoSearchEventStatus(AutoSearchEvents.AUTO_START);
            } else {
                mMainActivityViewModel.setAutoSearchEventStatus(AutoSearchEvents.AUTO_STOP);
            }
            Log.e(TAG, "onFocusChange: FOCUS__OF__SEARCH_VIEW__WAS__CHANGED with boolean:  " + hasFocus);
        });
        //Log.i(TAG, "setSearchWidget: START ");
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        //Log.i(TAG, "setSearchWidget: intent.getAction(): " + intent.getAction());
        //Log.i(TAG, "setSearchWidget: intent.ACTION: " + Intent.ACTION_SEARCH);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //Log.i(TAG, "setSearchWidget: SEARCH_QUERY: " + query);
            //doMySearch(query);
        }
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
            updateUiNavigationDrawerMenu(getCurrentUser());
        }
    }

    private void updateUiNavigationDrawerMenu(FirebaseUser user) {
        String name = getResources().getString(R.string.drawer_head_name);
        if (user.getDisplayName() != null) name = user.getDisplayName();
        View drawerHeader = binding.navDrawerView.getHeaderView(0);
        NavigationDrawerHeaderBinding headerBinding = NavigationDrawerHeaderBinding.bind(drawerHeader);
        GlideApp.with(this)
                .load(user.getPhotoUrl())
                .error(R.drawable.ic_persona_placeholder)
                .placeholder(R.drawable.ic_persona_placeholder)
                .fitCenter()
                .circleCrop()
                .into(headerBinding.navDrawerUserImage);
        headerBinding.navDrawerUserEmail.setText(user.getEmail());
        headerBinding.navDrawerUserFullName.setText(name);
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
        // TODO pass extra to retrieve data for AboutRestaurantActivity
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
                if (firebaseUser != null) mFirebaseUser = firebaseUser;
                FirebaseUserMetadata userMetadata = firebaseUser.getMetadata();
                if (userMetadata != null && isSignInFirstTime(userMetadata)) {
                    saveNewUser(firebaseUser);
                }
                ViewWidgets.showSnackBar(0, view, getString(R.string.login_succeed));
                updateUiNavigationDrawerMenu(firebaseUser);
                // TODO If it's correct to cal startMainActivity here ?
                startMainActivity();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back background_facebook_btn. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response != null && response.getError() == null) {
                    ViewWidgets.showSnackBar(1, view, getString(R.string.login_filed));
                }
            }
        }
    }

    private boolean isSignInFirstTime(FirebaseUserMetadata firebaseUserMetadata) {
        return firebaseUserMetadata.getCreationTimestamp() == firebaseUserMetadata.getLastSignInTimestamp();
    }

    private void saveNewUser(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String name = TextUtils.isEmpty(firebaseUser.getDisplayName()) ? "" : firebaseUser.getDisplayName();
        String email = TextUtils.isEmpty(firebaseUser.getEmail()) ? "" : firebaseUser.getEmail();
        String urlImage = Uri.EMPTY.equals(firebaseUser.getPhotoUrl()) ? "" : firebaseUser.getPhotoUrl().toString();
        mMainActivityViewModel.createUser(uid, name, email, urlImage);
    }

    private void logOutUser() {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(this, aVoid ->
        {
            if (getCurrentUser() == null) {
                ViewWidgets.showSnackBar(0, view, getString(R.string.logout_successful));
                // TODO manage action
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
        return this.getCurrentUser() != null;
    }

    @Override
    public void onLocationChanged(Location location) {
        /*
         * TODO if not used, delete this function
         *  think how this override fun works and how to used
         */
        mCurrentLocation = location;
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        mLatLng = new LatLng(mLatitude, mLongitude);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.i(TAG, "MAIN_ACTIVITY onStart is");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.i(TAG, "MAIN_ACTIVITY onResume is");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.i(TAG, "MAIN_ACTIVITY onPause is");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.i(TAG, "MAIN_ACTIVITY onStop is ");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.i(TAG, "MAIN_ACTIVITY onDestroy is");
//    }
}