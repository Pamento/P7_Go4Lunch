package com.pawel.p7_go4lunch.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentMapViewBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.ui.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.WasCalled;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.RestaurantsViewModel;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapViewFragment extends Fragment
        implements OnMapReadyCallback,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener {

    private RestaurantsViewModel mRestaurantsVM;
    private FragmentMapViewBinding mBinding;
    private com.pawel.p7_go4lunch.databinding.WifiOffBinding mWifiOffBinding;
    private View view;
    private GoogleMap mGoogleMaps;
    private FragmentActivity mFragmentActivity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocalAppSettings mAppSettings;
    private Activity mActivity;
    private String currentLocation;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private AutoSearchEvents autoEvent = AutoSearchEvents.AUTO_NULL;
    //private String autocompleteStatus;
    private static final String TAG = "AUTO_COM";
    private static final String TAG2 = "ASK_LOCATION";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "MVF__ onCreateView: ");
        initViewModel();
        mBinding = FragmentMapViewBinding.inflate(inflater, container, false);
        mWifiOffBinding = mBinding.msWifiOff;
        view = mBinding.getRoot();
        mActivity = getActivity();
        //mainActivity = (MainActivity) getParentFragment().getActivity();
        if ((mActivity != null) && (mAppSettings == null)) getLocalAppSettings(mActivity);
        initMap();
        return view;
    }

    private void initViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mRestaurantsVM = new ViewModelProvider(requireActivity(), vmf).get(RestaurantsViewModel.class);
        mRestaurantsVM.init();
    }

    // TODO cache:: in Which moment we decide when reuse the restos from cache for Map & List ?

    private void initMap() {
        if (LocationUtils.isWifiOn()) mWifiOffBinding.mapWifiOff.setVisibility(View.VISIBLE);
        else {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (supportMapFragment != null) {
                supportMapFragment.getMapAsync(MapViewFragment.this);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMaps = googleMap;
        mGoogleMaps.setOnMarkerClickListener(this);
        mRestaurantsVM.setGoogleMap(googleMap);
        Permissions.check(requireContext(), Const.PERMISSIONS, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Log.i(TAG, "MVF__ onGranted: PERMISSIONS");
                initMapRestaurant();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "MVF__ onViewCreated ");
        setAutocompleteEventObserver();
        observeGetRestaurants();
        // TODO cache:: where is the best place to call restaurant
//        mRestaurantsVM.getRestosFromCacheOrNetwork(autoEvent);
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(getActivity(), Html.fromHtml("<font color='#FF5721' ><b>" + msg + "</b></font>"), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    private void setAutocompleteEventObserver() {
        mRestaurantsVM.getAutoSearchEvent().observe(getViewLifecycleOwner(), autoSearchEvents -> {
            autoEvent = autoSearchEvents;
            Log.i(TAG, "MVF__ .EventObserver: AutoSearchEvent::::__ " + autoSearchEvents);
            switch (autoSearchEvents) {
                case AUTO_START:
                case AUTO_SEARCH_EMPTY:
                    Log.i(TAG, "MVM__ setAutocompleteEventObserver: " + autoSearchEvents);
                    mRestaurants.clear();
                    setRestaurantMarksOnMap();
                    break;
                case AUTO_ZERO_RESULT:
                    Log.i(TAG, "MVM__ setAutocompleteEventObserver: SNACK_BAR // SNACK_BAR ..//.. SNACK_BAR autoEvent:::: " + autoSearchEvents);
                    //ViewWidgets.showSnackBar(0, view, getString(R.string.search_no_resto_ms));
                    String msg = getString(R.string.search_no_resto_ms);
                    showToast(msg);
                    break;
                case AUTO_ERROR:
                    Log.e(TAG, "MVM__ setAutocompleteEventObserver: SNACK_BAR // SNACK_BAR ..//.. SNACK_BAR autoEvent:::: " + autoSearchEvents);
                    //ViewWidgets.showSnackBar(0, view, getString(R.string.fetching_data_error_ms));
                    String msgE = getString(R.string.fetching_data_error_ms);
                    showToast(msgE);
                    break;
                case AUTO_STOP:
                    mRestaurants.clear();
                    mRestaurantsVM.getRestosFromCacheOrNetwork(autoSearchEvents);
                    break;
                default:
                    break;
            }
        });
    }

    private void showToastRestosNr() {
        Log.i(TAG, "MVF__ showToastRestosNr.setAutocompleteEventObserver: " + mRestaurants.size());
        String restoFound = view.getResources().getString(R.string.number_resto_found, mRestaurants.size());
        showToast(restoFound);
    }

    final Observer<Location> getLocation = new Observer<Location>() {
        @Override
        public void onChanged(Location location) {
            if (location != null) {
                // TODO Add if for AutoEventStatus
                Log.i(TAG, "MVF__ m_initMapRestaurant.getLocation: " + location);
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                // TODO cache:: how to get appSettings from SharedPreferences in ViewModel ?
                mRestaurantsVM.setUpCurrentLocation(location, ll, mAppSettings.getRadius());
                Log.i(TAG, "onChanged: AutoSearchEvent::: " + autoEvent);
                if (autoEvent.equals(AutoSearchEvents.AUTO_NULL)) {
                    getRestaurantFromSource();
                } else {
                    removeGetLocationObserver();
                }
                moveCamera(location, mAppSettings.getPerimeter());
                // TODO cache:: test if the map will display the restaurants Markers on map
                Log.i(TAG, "MVF__ m_observerLocation.onChanged: BEFORE: setRestaurantMarksOnMap(); mRestaurants.size()::: " + mRestaurants.size());
                setRestaurantMarksOnMap();
                // FAB of functionality: "Back of camera upon user position"
                onViewModelReadySetObservers();
            }
        }
    };

    private void getRestaurantFromSource() {
        Log.i(TAG, "MVF__ getRestaurantFromSource: && REMOVE___OBSERVER-LOCATION");
        mRestaurantsVM.getRestosFromCacheOrNetwork(autoEvent);
        removeGetLocationObserver();
    }

    private void removeGetLocationObserver() {
        Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).removeObserver(getLocation);
    }

    final Observer<Location> observeLocation = new Observer<Location>() {
        @Override
        public void onChanged(Location location) {
            if (location == null) {
                ViewWidgets.showSnackBar(0, view, getResources().getString(R.string.current_location_not_found));
                LocationUtils.LocationDisabledDialog.newInstance().show(mFragmentActivity.getSupportFragmentManager(), "dialog");
            } else {
                // TODO Add if for AutoEventStatus
                Log.i(TAG, "MVF__ m_onViewModelReadySetObservers.observeLocation " + location);
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                mRestaurantsVM.setUpCurrentLocation(location, ll, mAppSettings.getRadius());
                // moveCamera to go back on User current position, without any other action
                moveCamera(location, mAppSettings.getPerimeter());
            }
        }
    };

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * Override
     * public void onMapReady(GoogleMap map) {
     * map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
     * }
     */
    private void initMapRestaurant() {
        //if (mGoogleMaps == null) mGoogleMaps = mRestaurantsVM.getGoogleMap();
        if (mAppSettings.isLocalisation()) {
            Log.i(TAG, "MVF__ START initMapRestaurant: :else ");
            Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).observe(getViewLifecycleOwner(), getLocation);
        } else {
            ViewWidgets.showSnackBar(1, view, getString(R.string.ask_location_local_settings_message));
        }
        // get automatically & unrepentantly of user will the position of device
        // Set blue point (mark) of user position. "true" is visible; "false" is hidden.
        //mMap.setMyLocationEnabled(true);
        // Disable icon of the center location
        /*
        // TODO on move map on screen of the device we start new request
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnCameraMoveListener(() -> {
            Log.i(TAG, "initMapRestaurant: CAMERA MOVED");
            // TODO run the function which get new restaurants in new area.
            mMap.setOnCameraMoveCanceledListener(() -> {
                Log.i(TAG, "initMapRestaurant: CAMERA _STOPPED");
                //TODO calculate difference in location like in onLocationChanged listener
                // and do the actions;
                });
            });
         */
        //            Log.i(TAG, "onMapReady: mMap " + mMap);
//            LatLng testPosition = new LatLng(37, -121);
//            LatLng sydney = new LatLng(34, -118);
//            mMap.addMarker(new MarkerOptions()
//                    .position(sydney)
//                    .title("Marker in Sydney"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//
//            mMap.setOnMarkerClickListener(marker -> {
//                Log.i(TAG, "onMarkerClick: " + marker.toString());
//                return true;
//            });

    }

    private void onViewModelReadySetObservers() {
        mBinding.fabCurrentLocation.setVisibility(View.VISIBLE);
        mBinding.fabCurrentLocation.setOnClickListener(v -> {
            Log.i(TAG, "MVF__ initMapRestaurant: FAB_OnClick");
            Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).observe(getViewLifecycleOwner(), observeLocation);
        });
    }

    Observer<List<Restaurant>> mObserverRestos = (Observer<List<Restaurant>>) restaurants -> {
        Log.i(TAG, "MVF__ .OOOOOOOOOOOOOOOOOO.observeRestaurantAPIResponse");
        if (restaurants != null) {
            Log.i(TAG, "MVF__ .OOOOOOOOOOOOOOOOOO.observeRestaurantAPIResponse: resto.size() " + restaurants.size());
            Log.i(TAG, "MVF__ .OOOOOOOOOOOOOOOOOO.observeRestaurantAPIResponse: autoEvent::: " + autoEvent);
            mRestaurants = restaurants;
            if (autoEvent.equals(AutoSearchEvents.AUTO_OK)) showToastRestosNr();
            setRestaurantMarksOnMap();
        }
    };

    // Observe restos in RestaurantViewModel
    private void observeGetRestaurants() {
        Log.i(TAG, "MVF__ observeGetRestaurants: OOOBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        mRestaurantsVM.getRestaurantWithUsers.observe(getViewLifecycleOwner(), mObserverRestos);
    }

//    private void removeObserverOnRestoAPIResponse() {
//        mRestaurantsVM.getRestaurants().removeObserver(mObserverRestos);
//    }

    private void setRestaurantMarksOnMap() {
        if (mGoogleMaps == null) mGoogleMaps = mRestaurantsVM.getGoogleMap();
        if (mRestaurants.isEmpty()) {
            Log.i(TAG, "MVF__ setRestaurantMarksOnMap:  isEmpty:::::::: ");
            mGoogleMaps.clear();
        } else {
            Log.i(TAG, "MVF__ setRestaurantMarksOnMap: MARKERS ___google mRestaurants[].size() " + mRestaurants.size());
            mGoogleMaps.clear();
            for (Restaurant rst : mRestaurants) {
                if (rst != null) {
                    Log.i(TAG, "MVF__ setRestaurantMarksOnMap: MARKERS ___google mRestaurants[].size() " + rst.getName());
                    LatLng latLng = new LatLng(rst.getLocation().getLat(), rst.getLocation().getLng());
                    Marker marker;
                    if (rst.getUserList() != null) {
                        marker = mGoogleMaps.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(rst.getName())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange)));
                    } else {
                        marker = mGoogleMaps.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(rst.getName())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green)));
                    }
                    marker.setTag(rst.getPlaceId());
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTag() != null) {
            String placeId = marker.getTag().toString();
            Log.i(TAG, "START onMarkerClick: " + placeId);

            for (Restaurant rst : mRestaurants) {
                if (rst.getPlaceId().equals(placeId)) {
                    Log.i(TAG, "onMarkerClick: foreach placeId " + rst.toString());
                    Intent intent = new Intent(getActivity(), AboutRestaurantActivity.class);
                    intent.putExtra(Const.EXTRA_KEY_RESTAURANT, placeId);
                    startActivity(intent);
                }
            }
        }
        return false;
    }

    private void moveCamera(Location loc, float zoom) {
        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        mGoogleMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocalAppSettings(Activity activity) {
        Log.i(TAG, "MVF__ getLocalAppSettings: FIRED");
        mAppSettings = new LocalAppSettings(activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFragmentActivity = getActivity();
        mAppSettings = new LocalAppSettings(mActivity);
        Log.i(TAG, "MVF__ onStart: mPrefs ? " + mAppSettings);
    }

    protected void createLocationRequest() {
        Log.i(TAG, "MVF__ createLocationRequest: ");
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            startLocationUpdates();
            Log.i(TAG, "MVF__ onSuccess: response " + locationSettingsResponse.toString());
        });
        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(requireActivity(),
                            Const.REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                    Log.e(TAG, "MapViewModel.createLocationRequest.ERROR: ", sendEx);
                }
            }
        });
    }

    // Catch result of {task.addOnFailureListener} of resolvable of createLocationRequest();
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.REQUEST_CHECK_SETTINGS) {
            Log.i(TAG, "MVF__ onActivityResult: " + data);
        }
        ViewWidgets.showSnackBar(1, view, getString(R.string.fail_ask_gps_signal));
    }

    private void startLocationUpdates() {
        Log.i(TAG, "MVF__ START startLocationUpdates: ");
        if (fusedLocationProviderClient == null) setFusedLocationProviderClient();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), Const.PERMISSIONS, Const.LOCATION_PERMISSION_REQUEST_CODE);
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient == null) setFusedLocationProviderClient();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void setFusedLocationProviderClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if need more check if/else see https://developer.android.com/training/location/request-updates#save-state
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i(TAG2, "MVF__ onLocationResult: " + location);
                    currentLocation = location.getLatitude() + "," + location.getLongitude();
                    // Update UI with location data
                    // ...
                }
            }
        };
    }

    @Override
    public void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onLocationChanged(@NotNull Location location) {
        Log.i(TAG, "MVF__ onLocationChanged: USER HAS MOVED !____!_____!_____!_____ _________________!!");
        // TODO if currentLocation is set by RequestLocation then the if/else below is never true.
        // TODO : need to set type initialLocation.
        LatLng initialLatLng = mRestaurantsVM.getInitialLatLng();
        Location oldLocation = new Location("");
        oldLocation.setLatitude(initialLatLng.latitude);
        oldLocation.setLongitude(initialLatLng.longitude);
        if (oldLocation.distanceTo(location) >= 300) {
            // We reset the limit guard of initial location
            if (WasCalled.resetLocationWasCalled()) {
                // TODO something if necessary
            }
            // TODO run new restaurantRequest
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "MVF__ onDestroy: is");
        mRestaurantsVM.disposeDisposable();
        super.onDestroy();
    }
}