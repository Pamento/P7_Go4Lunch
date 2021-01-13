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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.pawel.p7_go4lunch.MainActivity;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentMapViewBinding;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.LocationUtils;
import com.pawel.p7_go4lunch.utils.PermissionUtils;
import com.pawel.p7_go4lunch.utils.ViewWidgets;
import com.pawel.p7_go4lunch.utils.di.Injection;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, com.google.android.gms.location.LocationListener {

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
    private Location currentLocation;
    private static final String TAG = "SEARCH";
    private static final String TAG2 = "ASK_LOCATION";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        initMapViewModel();
        mBinding = FragmentMapViewBinding.inflate(inflater, container, false);
        view = mBinding.getRoot();
        onViewModelReady();
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

    private void onViewModelReady() {
        Log.i(TAG, "setUpViewWithViewModel: FIRED");
        mMapViewVM.getLatLng().observe(
                getViewLifecycleOwner(), latLng -> moveCamera(latLng, mPrefs.getPerimeter()));
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
        if (!MainActivity.permissionDenied) initMapRestaurant();
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
        // for save Google Map in case of device rotation
        if (mMap == null) {
            mMapViewVM.getGoogleMap().observe(getViewLifecycleOwner(), googleMap -> mMap = googleMap);
        }
        if (!MainActivity.permissionDenied) {
            getCurrentDeviceLocation();
            // TODO move onClickListener after locationPermission granted
            mBinding.fabCurrentLocation.setOnClickListener(v -> {
                Log.i(TAG, "initMapRestaurant: FAB_OnClick");
                getCurrentDeviceLocation();
                if (currentLocation != null) {
                    Log.i(TAG, "initMapRestaurant: FAB : " + currentLocation);
                } else {
                    ViewWidgets.showSnackBar(0, view, "No Data Location");
                }
            });

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
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
        } else {
            PermissionUtils.PermissionDeniedDialog.newInstance(false).show(mFragmentActivity.getSupportFragmentManager(), "dialog");
        }
    }

    /**
     * @fun getCurrentDeviceLocation use FusedLocationProviderClient to get lastLocation and if
     * this is not available the function start LocationRequest.
     * More info on https://developer.android.com/training/location/request-updates
     */
    //@TargetApi(Build.VERSION_CODES.KITKAT)
    private void getCurrentDeviceLocation() {
        Log.i(TAG, "getCurrentDeviceLocation: FIRED ");
        if (LocationUtils.isDeviceLocationEnabled(requireContext())) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);
            if (ActivityCompat.checkSelfPermission(mActivity, Const.PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mActivity, Const.PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED) {
                // TODO check if device has location & network enabled (Kitkat & above)
                Task<Location> getLocation = fusedLocationProviderClient.getLastLocation();
                getLocation.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        currentLocation = (Location) task.getResult();
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        Log.i(TAG, "getCurrentDeviceLocation: " + task.getResult());
                        ViewWidgets.showSnackBar(0, view, "lat-lng " + currentLocation);
                        mMapViewVM.setUpCurrentLocation(latLng);
                    } else {
                        createLocationRequest();
                    }
                });
            }
        } else {
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            //getFragmentManager is deprecated
//                LocationUtils.LocationDisabledDialog.newInstance().show(getFragmentManager(), "dialog");
//            } else {
            LocationUtils.LocationDisabledDialog.newInstance().show(mFragmentActivity.getSupportFragmentManager(), "dialog");
            //}

        }

    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.i(TAG, "moveCamera: FIRED");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }


    private void getLocalAppSettings(Activity activity) {
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
                    currentLocation = location;
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

    }
}