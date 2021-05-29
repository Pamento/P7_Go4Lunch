package com.pawel.p7_go4lunch.ui;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
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
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.MainActivityViewModel;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
            new AuthUI.IdpConfig.FacebookBuilder().build(),
            new AuthUI.IdpConfig.TwitterBuilder().build());
    protected FirebaseUser mFirebaseUser;
    protected Location mCurrentLocation;
    protected String mCurrentLocationStr;
    protected double mLatitude, mLongitude;
    protected LatLng mLatLng;
    private LocalAppSettings mAppSettings;
    private boolean isCollapse = true;
    private boolean isMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMainViewModel();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        view = binding.getRoot();
        setContentView(view);
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            binding.toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_filter_list_24));
        }
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
        if (Build.VERSION.SDK_INT <= 21) {
            Log.i(TAG, "21");
            Log.i(TAG, "21");
            Log.i(TAG, "21");
            Log.i(TAG, "21");
            //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            View deco = getWindow().getDecorView();
            deco.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    v.setAlpha(0.6f);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            });
        }
    }

    private void initMainViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mMainActivityViewModel = new ViewModelProvider(this, vmf).get(MainActivityViewModel.class);
    }

    // Check if service Google Maps is available
    public boolean isMapsServiceOk() {
        Log.i(TAG, "isMapsServiceOk: START");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, Const.ERROR_DIALOG_REQUEST);
            if (dialog != null) dialog.show();
        } else {
            ViewWidgets.showSnackBar(1, view, getString(R.string.google_maps_not_available));
        }
        return false;
    }

    // ____________ Main Activity with 3 fragments _____________________
    private void startMainActivity() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map_view, R.id.navigation_list_view, R.id.navigation_workmates)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Remove filters from Menu if Map Fragment is displayed.

        Log.i(TAG, "onPrepareOptionsMenu: isMapFragment ::: " + isMapFragment);
        Log.i(TAG, "MainA__ onPrepareOptionsMenu: MMMMMMMMMM__ MAKE __MMMMMMMMMMM");
        if (isMapFragment) {
            Log.i(TAG, "MainA__ onPrepareOptionsMenu: MMMMMMMMM__ ReMake __MMMMMMMMM");
            menu.removeItem(R.id.filter_AZ);
            menu.removeItem(R.id.filter_distance);
            menu.removeItem(R.id.filter_rating);
            menu.removeItem(R.id.filter_reset);
        }
        return true;
    }

    // Fun: updateMenuItems is run from fragments to remove filters from Map Fragment and add it to ListRestaurants.
    public void updateMenuItems(boolean isMap) {
        Log.i(TAG, "MainA__ onPrepareOptionsMenu");
        isMapFragment = isMap;
        // invalidateOptionsMenu: internal function of OS Android to rerun onPrepareOptionsMenu
        this.invalidateOptionsMenu();
    }

    // ____________ Toolbar search _____________________
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_search_menu, menu);
        if (LocationUtils.isNetworkAvailable()) {
            setSearch(menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // ____________ Toolbar search on result _____________________
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        int filterType = 0;
        if (itemId != R.id.toolbar_search_icon && itemId != R.id.filter_rating) {
            if (itemId == R.id.filter_AZ) filterType = 1;
            else if (itemId == R.id.filter_rating_2stars) filterType = 2;
            else if (itemId == R.id.filter_rating_3stars) filterType = 3;
            else if (itemId == R.id.filter_distance) filterType = 4;
            applyFilter(filterType);
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private void applyFilter(int filterType) {
        mMainActivityViewModel.filterRestaurantBy(filterType);
    }

    private void getLocalAppSettings() {
        mAppSettings = new LocalAppSettings(this);
    }

    private void setSearch(Menu menu) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.toolbar_search_icon).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        //searchView.setIconifiedByDefault(false);
        // Listen for expand & collapse the SearchView.
        MenuItem item = menu.findItem(R.id.toolbar_search_icon);
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isCollapse = false;
                setAutoSearchEventStatus(false, AutoSearchEvents.AUTO_START);
                Log.i(TAG, "MainA__ setSearch.onMenuItemActionExpand: EXPAND=false ::: " + isCollapse);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Keep this values below to time of implement filter on listViewResto.
                //MainA__ onMenuItemActionExpand. item.id::: 2131296835
                //MainA__ onMenuItemActionCollapse: toolbar_search_icon.id::: 2131296835
                isCollapse = true;
                Log.i(TAG, "MainA__ setSearch.onMenuItemActionCollapse: COLLAPSE=true ::: " + isCollapse);
                setAutoSearchEventStatus(true, AutoSearchEvents.AUTO_STOP);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                mCurrentLocationStr = mMainActivityViewModel.getCurrentLocation();
                Log.i(TAG, " __onQueryTextChange: ___________________________________________ \"" + newText + "\"");
                if (newText.length() == 0 && !isCollapse) {
                    setAutoSearchEventStatus(false, AutoSearchEvents.AUTO_SEARCH_EMPTY);
                }
                if ((newText.length() >= 3)) {
                    mMainActivityViewModel
                            .streamCombinedAutocompleteDetailsPlace(
                                    newText, getResources().getString(R.string.app_language),
                                    mAppSettings.getRadius(), mCurrentLocationStr, mCurrentLocationStr);
                }
                return true; // signal that we consumed this event
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true; // signal that we consumed this event
            }
        });
    }

    private void setAutoSearchEventStatus(boolean isSearchViewCollapse, AutoSearchEvents event) {
        if (isSearchViewCollapse) {
            mMainActivityViewModel.setAutoSearchEventStatus(AutoSearchEvents.AUTO_STOP);
        } else {
            mMainActivityViewModel.setAutoSearchEventStatus(event);
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
        Log.i(TAG, "updateUiNavigationDrawerMenu: USER.email::: " + user.getEmail());
        Log.i(TAG, "updateUiNavigationDrawerMenu: USER.name:::  " + name);
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
                .setTwitterButtonId(R.id.twitter_btn)
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
        FirebaseUserMetadata userMetadata;
        if (requestCode == Const.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser firebaseUser = getCurrentUser();
                if (firebaseUser != null) {
                    mFirebaseUser = firebaseUser;
                    userMetadata = firebaseUser.getMetadata();
                    if (userMetadata != null && isSignInFirstTime(userMetadata)) {
                        saveNewUser(firebaseUser);
                    }
                    updateUiNavigationDrawerMenu(firebaseUser);
                    ViewWidgets.showSnackBar(0, view, getString(R.string.login_succeed));
                }
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
        String uid = "";
        String name = "";
        String email = "";
        String urlImage = "";
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
            name = TextUtils.isEmpty(firebaseUser.getDisplayName()) ? "" : firebaseUser.getDisplayName();
            email = TextUtils.isEmpty(firebaseUser.getEmail()) ? "" : firebaseUser.getEmail();
            if (firebaseUser.getPhotoUrl() != null)
                urlImage = Uri.EMPTY.equals(firebaseUser.getPhotoUrl()) ? "" : Objects.requireNonNull(firebaseUser.getPhotoUrl()).toString();
        }
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
        mCurrentLocation = location;
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        mLatLng = new LatLng(mLatitude, mLongitude);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocalAppSettings();
        Log.i(TAG, "MAIN_ACTIVITY onResume is");
    }

    @Override
    protected void onDestroy() {
        mMainActivityViewModel.disposeDisposable();
        super.onDestroy();
        Log.i(TAG, "MAIN_ACTIVITY onDestroy is");
    }
}