package com.pawel.p7_go4lunch.ui.mapView;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.pawel.p7_go4lunch.MainActivity;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.databinding.FragmentMapViewBinding;
import com.pawel.p7_go4lunch.utils.ViewWidgets;

import java.util.Objects;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private MapViewViewModel mMapViewViewModel;
    private FragmentMapViewBinding mBinding;
    private View view;
    private GoogleMap mMap;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    private SharedPreferences mPrefs;
    private static final String TAG = "PROBLEME";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mMapViewViewModel = ViewModelProviders.of(this)
                .get(MapViewViewModel.class);

        try {
            mBinding = FragmentMapViewBinding.inflate(inflater, container, false);
            view = mBinding.getRoot();
            //Objects.requireNonNull(((AppCompatActivity) requireActivity()).setSupportActionBar());
            Activity activity = getActivity();
            if (activity != null) {
                mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
            }
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (supportMapFragment != null) {
                supportMapFragment.getMapAsync(MapViewFragment.this);
            }
            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView", e);
            throw e;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
    }

    // Check if service Google Maps is available
    public boolean isMapsServiceOk() {
        if (mPrefs.getBoolean("localisation",true)) {
            int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
            if (available == ConnectionResult.SUCCESS) {
                return true;
            } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
                dialog.show();
            } else {
                ViewWidgets.showSnackBar(1,view,getString(R.string.google_maps_not_available));
            }
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}