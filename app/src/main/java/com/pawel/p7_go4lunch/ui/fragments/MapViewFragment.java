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
import com.pawel.p7_go4lunch.dataServices.cache.InMemoryRestosCache;
import com.pawel.p7_go4lunch.databinding.FragmentMapViewBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.ui.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.ui.MainActivity;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.RestaurantsViewModel;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapViewFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private RestaurantsViewModel mRestaurantsVM;
    private FragmentMapViewBinding mBinding;
    private com.pawel.p7_go4lunch.databinding.WifiOffBinding mWifiOffBinding;
    private View view;
    private GoogleMap mGoogleMaps;
    private FragmentActivity mFragmentActivity;
    private InMemoryRestosCache mCache;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocalAppSettings mAppSettings;
    private Activity mActivity;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private AutoSearchEvents autoEvent = AutoSearchEvents.AUTO_NULL;
    private static final String TAG = "AUTO_COM";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "MVF__ onCreateView: ");
        initViewModel();
        mBinding = FragmentMapViewBinding.inflate(inflater, container, false);
        mWifiOffBinding = mBinding.msWifiOff;
        view = mBinding.getRoot();
        mActivity = getActivity();
        if ((mActivity != null) && (mAppSettings == null)) getLocalAppSettings(mActivity);
        initMap();
        mCache = InMemoryRestosCache.getInstance();
        Log.i(TAG, "MVF__ onCreateView: updateMenuItems(true);");
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.updateMenuItems(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mRestaurantsVM = new ViewModelProvider(requireActivity(), vmf).get(RestaurantsViewModel.class);
        mRestaurantsVM.init();
    }

    private void initMap() {
        if (!LocationUtils.isNetworkAvailable()) mWifiOffBinding.mapWifiOff.setVisibility(View.VISIBLE);
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
        Log.i(TAG, "MVF__ onMapReady: start listeners for AUTO_COM && getRestaurants() !!!!!!!!!!!!!!!!");
        setAutocompleteEventObserver();
        observeGetRestaurants();
        Permissions.check(requireContext(), Const.PERMISSIONS, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Log.i(TAG, "MVF__ onGranted: PERMISSIONS");
                initMapRestaurant();
            }
        });
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(getActivity(), Html.fromHtml("<font color='#FF5721' ><b>" + msg + "</b></font>"), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private void setAutocompleteEventObserver() {
        mRestaurantsVM.getAutoSearchEvent().observe(getViewLifecycleOwner(), autoSearchEvents -> {
            autoEvent = autoSearchEvents;
            switch (autoSearchEvents) {
                case AUTO_START:
                case AUTO_SEARCH_EMPTY:
                    mRestaurants.clear();
                    setRestaurantMarksOnMap();
                    break;
                case AUTO_ZERO_RESULT:
                    String msg = getString(R.string.search_no_resto_ms);
                    showToast(msg);
                    break;
                case AUTO_ERROR:
                    String msgE = getString(R.string.fetching_data_error_ms);
                    showToast(msgE);
                    break;
                case AUTO_STOP:
                    mRestaurants.clear();
                    mRestaurantsVM.getRestosFromCacheOrNetwork();
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

    // getLocation is called only once at opening moment of App Go4Lunch
    final Observer<Location> getLocation = new Observer<Location>() {
        @Override
        public void onChanged(Location location) {
            if (location != null) {
                updateLocationAndPosition(location, mAppSettings.getRadius());
                Log.i(TAG, "MVF__ m_initMapRestaurant.getLocation: " + location);
                Log.i(TAG, "onChanged: AutoSearchEvent::: " + autoEvent);
                if (autoEvent.equals(AutoSearchEvents.AUTO_NULL)) {
                    getRestaurantFromSource();
                } else {
                    removeGetLocationObserver();
                }
                setRestaurantMarksOnMap();
                // FAB of functionality: "Back of camera upon user position"
                onViewModelReadySetObservers();
            } else {
                if (LocationUtils.isDeviceLocationEnabled()) createLocationRequest();
                else ViewWidgets.showSnackBar(1, view, getString(R.string.fail_ask_gps_signal));
            }
        }
    };

    private void getRestaurantFromSource() {
        Log.i(TAG, "MVF__ getRestaurantFromSource: && REMOVE___OBSERVER-LOCATION");
        mRestaurantsVM.getRestosFromCacheOrNetwork();
        removeGetLocationObserver();
    }

    private void removeGetLocationObserver() {
        Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).removeObserver(getLocation);
    }

    final Observer<Location> observeLocation = new Observer<Location>() {
        @Override
        public void onChanged(Location location) {
            if (location == null) {
                //ViewWidgets.showSnackBar(0, view, getResources().getString(R.string.current_location_not_found));
                LocationUtils.LocationDisabledDialog.newInstance().show(mFragmentActivity.getSupportFragmentManager(), "dialog");
            } else {
                Log.i(TAG, "MVF__ m_onViewModelReadySetObservers.observeLocation " + location);
                // moveCamera to go back on User current position, without any other action
                updateLocationAndPosition(location, mAppSettings.getRadius());
            }
        }
    };

    private void initMapRestaurant() {
        if (mAppSettings.isLocalisation()) {
            Log.i(TAG, "MVF__ START initMapRestaurant: :if ");
            Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).observe(getViewLifecycleOwner(), getLocation);
        } else {
            ViewWidgets.showSnackBar(1, view, getString(R.string.ask_location_local_settings_message));
        }
        // get automatically & unrepentantly of user will the position of device
        // Set blue point (mark) of user position. "true" is visible; "false" is hidden.
        //mMap.setMyLocationEnabled(true);
        // Disable icon of the center location
        //mMap.getUiSettings().setMyLocationButtonEnabled(false);
//        mGoogleMaps.setOnCameraMoveListener(() -> {
//            Log.i(TAG, "initMapRestaurant: CAMERA MOVED");
//            mGoogleMaps.setOnCameraMoveCanceledListener(() -> {
//                Log.i(TAG, "initMapRestaurant: CAMERA _STOPPED");
//            });
//        });
    }

    private void onViewModelReadySetObservers() {
        mBinding.fabCurrentLocation.setVisibility(View.VISIBLE);
        mBinding.fabCurrentLocation.setOnClickListener(v -> {
            Log.i(TAG, "MVF__ initMapRestaurant: FAB_OnClick");
            Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).observe(getViewLifecycleOwner(), observeLocation);
        });
    }

    Observer<List<Restaurant>> mObserverRestos = (Observer<List<Restaurant>>) restaurants -> {
        Log.i(TAG, "MVF__ .OBSERVER.observeRestaurantAPIResponse");
        if (restaurants != null) {
            Log.i(TAG, "MVF__ .OBSERVER.observeRestaurantAPIResponse: resto.size() " + restaurants.size());
            Log.i(TAG, "MVF__ .OBSERVER.observeRestaurantAPIResponse: autoEvent::: " + autoEvent);
            mRestaurants = restaurants;
            if (autoEvent.equals(AutoSearchEvents.AUTO_OK)) showToastRestosNr();
            setRestaurantMarksOnMap();
        }
    };

    // Observe restos in RestaurantViewModel
    private void observeGetRestaurants() {
        Log.i(TAG, "MVF__ observeGetRestaurants: set__ OBSERVER: START");
        mRestaurantsVM.getRestaurantWithUsers().observe(getViewLifecycleOwner(), mObserverRestos);
    }


    private void unsubscribeRestaurants() {
        mRestaurantsVM.getRestaurantWithUsers().removeObserver(mObserverRestos);
    }

    private void unsubscribeLocation() {
        if (Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).hasActiveObservers()) {
            Log.i(TAG, "unsubscribeLocation: OOOOOOOOOOOOOOOOOOOOOOOOOO__ has Active_ observer OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
            Log.i(TAG, "unsubscribeLocation: has ACTIVE observer");
            Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).removeObserver(observeLocation);
        }
        if (Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).hasObservers()) {
            Log.i(TAG, "unsubscribeLocation: OOOOOOOOOOOOOOOOOOOOOOOOOOOO__ hasObserver __OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
            Log.i(TAG, "unsubscribeLocation: has Observer");
        }
    }

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
                    Log.i(TAG, "setRestaurantMarksOnMap: in FOR::>> rst(!null)::: rst.getUserlist().size()::: " + rst.getUserList().size());
                    LatLng latLng = new LatLng(rst.getLocation().getLat(), rst.getLocation().getLng());
                    Marker marker;
                    if (rst.getUserList().size() > 0) {
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
        unsubscribeLocation();
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

        // On back to MapFragment check if radius of search has changed
        if (mCache.getRadius() != 0 && (mCache.getRadius() != mAppSettings.getRadius())) {
            Log.i(TAG, "MVF__ onStart: RUN __if USER HAS CHANGED RADIUS IN settings LOCAL.");
            Log.i(TAG, "MVF__ onStart: old radius:::: " + mCache.getRadius());
            Log.i(TAG, "MVF__ onStart: new radius:::: " + mAppSettings.getRadius());
            updateRestaurantQueryToRadius(mCache.getRadius(), mAppSettings.getRadius());
            mCache.setRadius(mAppSettings.getRadius());
        }
    }

    private void updateRestaurantQueryToRadius(int previousRadius, int currentRadius) {
        // old 500 - new 1000
        if ((previousRadius - currentRadius) < 0) {
            Log.i(TAG, "MVF__ if __updateRestaurantQueryToRadius: previousRadius::: " + previousRadius + ", currentRadius::-:: " + currentRadius);
            mRestaurantsVM.streamGetRestaurantNearbyAndDetail(mRestaurantsVM.getCurrentLocStr(), mAppSettings.getRadius());
        } else {
            Log.i(TAG, "MVF__ else __updateRestaurantQueryToRadius: previousRadius::: " + previousRadius + ", currentRadius::+:: " + currentRadius);
            mRestaurantsVM.getRestosFromCache(currentRadius);
        }
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
                    Log.e("ERROR", "MapViewModel.createLocationRequest.ERROR: ", sendEx);
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
        Log.i(TAG, "onCreate: Start LOCATION____CALLBACK");
        // if need more check if/else see https://developer.android.com/training/location/request-updates#save-state
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationAndPosition(location, mAppSettings.getRadius());
                    Log.i(TAG, "onChanged: AutoSearchEvent::: " + autoEvent);
                    if (autoEvent.equals(AutoSearchEvents.AUTO_NULL)) {
                        getRestaurantFromSource();
                    }
                }
            }
        };
    }

    private void updateLocationAndPosition(Location location, int radius) {
        mRestaurantsVM.setUpCurrentLocation(location, radius);
        moveCamera(location, mAppSettings.getPerimeter());
        mCache.setRadius(radius);
    }

/*    @Override
    public void onLocationChanged(@NotNull Location location) {
    // need implement         com.google.android.gms.location.LocationListener,
        Log.i(TAG, "MVF__ onLocationChanged: USER HAS MOVED !____!_____!_____!_____!!");
        LatLng initialLatLng = mRestaurantsVM.getInitialLatLng();
        Location oldLocation = new Location("");
        oldLocation.setLatitude(initialLatLng.latitude);
        oldLocation.setLongitude(initialLatLng.longitude);
        if (oldLocation.distanceTo(location) >= 300) {
            // We reset the limit guard of initial location
        }
    }*/

    @Override
    public void onPause() {
        Log.i(TAG, "MVF__ onPause");
        stopLocationUpdates();
        mRestaurantsVM.unsubscribeRestoWithUsers();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "MVF__ onStop: UNSUBSCRIBE observable RESTO");
        unsubscribeRestaurants();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mWifiOffBinding = null;
        mBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "MVF__ onDestroy: is");
        mRestaurantsVM.disposeDisposable();
        super.onDestroy();
    }
}