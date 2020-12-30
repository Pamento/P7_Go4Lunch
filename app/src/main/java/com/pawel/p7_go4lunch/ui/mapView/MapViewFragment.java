package com.pawel.p7_go4lunch.ui.mapView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.pawel.p7_go4lunch.MainActivity;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentMapViewBinding;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.ViewWidgets;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private MapViewViewModel mMapViewViewModel;
    private FragmentMapViewBinding mBinding;
    private View view;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocalAppSettings mPrefs;
    private Activity mActivity;
    private static final String TAG = "TESTING_MAPS";
    private Location currentLocation;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mMapViewViewModel = ViewModelProviders.of(this)
                .get(MapViewViewModel.class);

        try {
            mBinding = FragmentMapViewBinding.inflate(inflater, container, false);
            view = mBinding.getRoot();
            mActivity = getActivity();
            if (mActivity != null) {
                mPrefs = new LocalAppSettings(mActivity);
            }
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (supportMapFragment != null) {
                supportMapFragment.getMapAsync(MapViewFragment.this);
            }
            mBinding.fabCurrentLocation.setOnClickListener(v -> getCurrentDeviceLocation());
            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView", e);
            throw e;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        if (MainActivity.mLocationPermissionGranted) {
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
            Log.i(TAG, "onMapReady: mMap "+mMap);
            LatLng testPosition = new LatLng(37,-121);
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions()
                    .position(sydney)
                    .title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            mMap.setOnMarkerClickListener(marker -> {
                Log.i(TAG, "onMarkerClick: "+marker.toString());
                return true;
            });
        }
    }

    private void getCurrentDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);
        try {
            if (MainActivity.mLocationPermissionGranted) {
                Task<Location> getLocation = mFusedLocationProviderClient.getLastLocation();
                getLocation.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentLocation = task.getResult();
                        float zoom = Float.parseFloat(mPrefs.getPerimeter());
                        Log.i(TAG, "onComplete: location " + currentLocation);
                        moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),zoom);
                    } else {
                        Log.i(TAG, "onComplete: NotFound Location");
                        ViewWidgets.showSnackBar(1,view,getString(R.string.current_location_not_found));
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e(TAG, "getCurrentDeviceLocation: Security Exception " + e.getMessage() );
        } catch (NullPointerException n) {
            Log.e(TAG, "getCurrentDeviceLocation: ",n );
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}