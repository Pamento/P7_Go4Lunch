package com.pawel.p7_go4lunch.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import com.pawel.p7_go4lunch.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentMapViewBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.WasCalled;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.RestaurantsViewModel;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapViewFragment extends Fragment
        implements OnMapReadyCallback,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener {

    private RestaurantsViewModel mMapViewVM;
    private FragmentMapViewBinding mBinding;
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
    private static final String TAG = "SEARCH";
    private static final String TAG2 = "ASK_LOCATION";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        initMapViewModel();
        mBinding = FragmentMapViewBinding.inflate(inflater, container, false);
        view = mBinding.getRoot();
        mActivity = getActivity();
        //mainActivity = (MainActivity) getParentFragment().getActivity();
        if ((mActivity != null) && (mAppSettings == null)) getLocalAppSettings(mActivity);
        initMap();
        return view;
    }

    private void initMapViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mMapViewVM = new ViewModelProvider(requireActivity(), vmf).get(RestaurantsViewModel.class);
        mMapViewVM.init();
    }

    private void onViewModelReadySetObservers() {
        Log.i(TAG, "FIRED setUpViewWithViewModel:");
        mBinding.fabCurrentLocation.setVisibility(View.VISIBLE);
        mBinding.fabCurrentLocation.setOnClickListener(v -> {
            Log.i(TAG, "initMapRestaurant: FAB_OnClick");
            Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).observe(getViewLifecycleOwner(), location -> {
                if (location == null) {
                    ViewWidgets.showSnackBar(0, view, getResources().getString(R.string.current_location_not_found));
                    LocationUtils.LocationDisabledDialog.newInstance().show(mFragmentActivity.getSupportFragmentManager(), "dialog");
                }
            });
        });
    }

    private void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(MapViewFragment.this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMaps = googleMap;
        mGoogleMaps.setOnMarkerClickListener(this);
        mMapViewVM.setGoogleMap(googleMap);
        Permissions.check(requireContext(), Const.PERMISSIONS, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Log.i(TAG, "onGranted: PERMISSIONS");
                initMapRestaurant();
            }
        });
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * Override
     * public void onMapReady(GoogleMap map) {
     * map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
     * }
     */
    private void initMapRestaurant() {
        // TODO for save Google Map in case of device rotation need use onActivityCreate
        if (mGoogleMaps != null && mAppSettings.isLocalisation()) {
            Log.i(TAG, "START initMapRestaurant: :else ");
            Objects.requireNonNull(LocationUtils.getCurrentDeviceLocation()).observe(getViewLifecycleOwner(), location -> {
                if (location != null) {
                    Log.i(TAG, "initMapRestaurant: " + location);
                    LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                    mMapViewVM.setUpCurrentLocation(location, ll);
                    if (mMapViewVM.getRestaurantsCache().isEmpty()) {
                        fetchRestaurants();
                        onFetchRestaurants();
                        Log.i(TAG, "initMapRestaurant: fetchRestaurants ");
                    } else {
                        mRestaurants = mMapViewVM.getRestaurantsCache();
                        if (mRestaurants != null) setRestaurantMarksOnMap();
                    }

                    moveCamera(location, mAppSettings.getPerimeter());
                    onViewModelReadySetObservers();
                }
            });
        } else {
            if (!mAppSettings.isLocalisation()) {
                ViewWidgets.showSnackBar(1, view, getString(R.string.ask_location_local_settings_message));
            }
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

    private void fetchRestaurants() {
        mMapViewVM.fetchRestaurants(mAppSettings.getRadius());
    }

    private void onFetchRestaurants() {
        mMapViewVM.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            mRestaurants = restaurants;
            setRestaurantMarksOnMap();
        });
    }

    private void setRestaurantMarksOnMap() {
        if (mRestaurants.isEmpty()) Log.i(TAG, "setRestaurantMarksOnMap:  isEmpty:::::::: " );
        if (!mRestaurants.isEmpty()) {
            Log.i(TAG, "setRestaurantMarksOnMap: MARKERS ___google mRestaurants[].size() " + mRestaurants.size());
            mGoogleMaps.clear();
            for (Restaurant rst : mRestaurants) {
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
                //mGoogleMaps.setOnMarkerClickListener(this);
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
        Log.i(TAG, "moveCamera: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + loc);
        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        mGoogleMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocalAppSettings(Activity activity) {
        Log.i(TAG, "getLocalAppSettings: FIRED");
        mAppSettings = new LocalAppSettings(activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFragmentActivity = getActivity();
        mAppSettings = new LocalAppSettings(mActivity);
        Log.i(TAG, "onStart: mPrefs ? " + mAppSettings);
    }

    protected void createLocationRequest() {
        Log.i(TAG, "createLocationRequest: ");
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
            Log.i(TAG, "onSuccess: response " + locationSettingsResponse.toString());
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
                }
            }
        });
    }

    // Catch result of {task.addOnFailureListener} of resolvable of createLocationRequest();
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.REQUEST_CHECK_SETTINGS) {
            Log.i(TAG, "onActivityResult: " + data);
        }
        ViewWidgets.showSnackBar(1, view, getString(R.string.fail_ask_gps_signal));
    }

    private void startLocationUpdates() {
        Log.i(TAG, "START startLocationUpdates: ");
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
                    Log.i(TAG2, "onLocationResult: " + location);
                    currentLocation = location.getLatitude() + "," + location.getLongitude();
                    // Update UI with location data
                    // ...
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged: USER HAS MOVED !____!_____!_____!_____ _________________!!");
        // TODO if currentLocation is set by RequestLocation then the if/else below is never true.
        // TODO : need to set type initialLocation.
        LatLng initialLatLng = mMapViewVM.getInitialLatLng();
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
        super.onDestroy();
        mMapViewVM.disposeDisposable();
    }
}