package com.pawel.p7_go4lunch.ui.mapView;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.pawel.p7_go4lunch.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.MainActivity;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentMapViewBinding;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.PermissionUtils;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.WasCalled;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

import java.util.List;

public class MapViewFragment extends Fragment
        implements OnMapReadyCallback,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener {

    private MapViewViewModel mMapViewVM;
    private FragmentMapViewBinding mBinding;
    private View view;
    private GoogleMap mMap;
    private FragmentActivity mFragmentActivity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocalAppSettings mPrefs;
    private MainActivity mainActivity;
    private Activity mActivity;
    private String currentLocation;
    private List<Restaurant> mRestaurants;
    private static final String TAG = "SEARCH";
    private static final String TAG2 = "ASK_LOCATION";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        initMapViewModel();
        mBinding = FragmentMapViewBinding.inflate(inflater, container, false);
        view = mBinding.getRoot();
        mActivity = getActivity();
        mainActivity = (MainActivity) getParentFragment().getActivity();
        if ((mActivity != null) && (mPrefs == null)) getLocalAppSettings(mActivity);
        initMap();
        return view;
    }

    private void initMapViewModel() {
        ViewModelFactory vmf = Injection.sViewModelFactory();
        mMapViewVM = new ViewModelProvider(this, vmf).get(MapViewViewModel.class);
        mMapViewVM.init();
    }

    private void onViewModelReadySetObservers() {
        Log.i(TAG, "setUpViewWithViewModel: FIRED");
        mBinding.fabCurrentLocation.setVisibility(View.VISIBLE);
        mBinding.fabCurrentLocation.setOnClickListener(v -> {
            Log.i(TAG, "initMapRestaurant: FAB_OnClick");
            if (getCurrentDeviceLocation()) {
                moveCamera(mMapViewVM.getLatLng(), mPrefs.getPerimeter());
                Log.i(TAG, "initMapRestaurant: FAB : " + currentLocation);
            } else {
                ViewWidgets.showSnackBar(0, view, getResources().getString(R.string.current_location_not_found));
            }
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
        this.mMap = googleMap;
        mMapViewVM.setGoogleMap(googleMap);
        MainActivity.getLocationPermission(mainActivity);
        // if permissionDenied is not denied (= false): initMapRestaurant();
        if (!MainActivity.permissionDenied) {
            initMapRestaurant();
            mRestaurants = mMapViewVM.getRestaurants(mPrefs.getRadius(), getString(R.string.google_api_key));
        } else {
            PermissionUtils.PermissionDeniedDialog.newInstance(false).show(mFragmentActivity.getSupportFragmentManager(), "dialog");
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this case, we just add a marker near Africa.
     * Override
     * public void onMapReady(GoogleMap map) {
     * map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
     * }
     */
    private void initMapRestaurant() {
        Log.i(TAG, "initMapRestaurant: START");
        // for save Google Map in case of device rotation
        if (mMap == null) {
            mMapViewVM.getGoogleMap().observe(getViewLifecycleOwner(), googleMap -> {
                mMap = googleMap;
                if (getCurrentDeviceLocation()) {
                    if (mMapViewVM.getCurrentLocation() != null) moveCamera(mMapViewVM.getLatLng(), mPrefs.getPerimeter());
                    onViewModelReadySetObservers();
                    setRestaurantMarksOnMap();
                }
            });
        }
        // get automatically & unrepentantly of user will the position of device
        //getCurrentDeviceLocation();
        // recheck permissions for settings  for maps below
//            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED
//                    && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
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

    private void setRestaurantMarksOnMap() {
        if (mRestaurants == null) {
            mMapViewVM.getMiddleRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
                mRestaurants = restaurants;
                setRestaurantMarksOnMap();
            });
        } else {
            for (Restaurant rst : mRestaurants) {
                mMap.clear();
                LatLng latLng = new LatLng(rst.getLocation().getLat(),rst.getLocation().getLng());
                Marker marker;
                if (rst.getUserList().isEmpty()) {
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(rst.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange)));
                } else {
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(rst.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green)));
                }
                marker.setTag(rst.getPlaceId());
                mMap.setOnMarkerClickListener(this);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String placeId = marker.getTag().toString();
        for (Restaurant rst : mRestaurants) {
            if (rst.getPlaceId().equals(placeId)) {
                Intent intent = new Intent(getActivity(), AboutRestaurantActivity.class);
                intent.putExtra(Const.EXTRA_KEY_RESTAURANT, placeId);
                startActivity(intent);
            }
        }
        return false;
    }

    /**
     * @fun getCurrentDeviceLocation use FusedLocationProviderClient to get lastLocation and if
     * this is not available the function start LocationRequest.
     * More info on https://developer.android.com/training/location/request-updates
     */
    //@TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean getCurrentDeviceLocation() {
        final boolean[] res = {false};
        Log.i(TAG, "getCurrentDeviceLocation: FIRED ");
        if (LocationUtils.isDeviceLocationEnabled(requireContext())) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);
            try {
                // TODO check if device has location & network enabled (Kitkat & above)
                Task<Location> getLocation = fusedLocationProviderClient.getLastLocation();
                getLocation.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        currentLocation = location.getLatitude() + "," + location.getLongitude();
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.i(TAG, "getCurrentDeviceLocation: " + task.getResult());
                        mMapViewVM.setUpCurrentLocation(currentLocation);
                        mMapViewVM.setUpCurrentLatLng(latLng);
                        res[0] = true;
                    } else {
                        createLocationRequest();
                    }
                });
            } catch (SecurityException e) {
                e.getMessage();
            }
            res[0] = true;
        } else {
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            //getFragmentManager is deprecated
//                LocationUtils.LocationDisabledDialog.newInstance().show(getFragmentManager(), "dialog");
//            } else {
            LocationUtils.LocationDisabledDialog.newInstance().show(mFragmentActivity.getSupportFragmentManager(), "dialog");
            //}
            res[0] = false;
        }
        return res[0];
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.i(TAG, "moveCamera: FIRED");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    private void getLocalAppSettings(Activity activity) {
        Log.i(TAG, "getLocalAppSettings: FIRED");
        mPrefs = new LocalAppSettings(activity);
    }

    @Override
    public void onStart() {
        mFragmentActivity = getActivity();
        mPrefs = new LocalAppSettings(mActivity);
        Log.i(TAG, "onStart: mPrefs ? " + mPrefs);
        super.onStart();
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
        Log.i(TAG, "startLocationUpdates: START ");
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
        Location oldLocation = null;
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