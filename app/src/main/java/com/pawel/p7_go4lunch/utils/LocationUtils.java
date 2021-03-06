package com.pawel.p7_go4lunch.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.service.Go4Lunch;

public abstract class LocationUtils {

    private static final MutableLiveData<Location> data = new MutableLiveData<>();

    /**
     * @fun getCurrentDeviceLocation use FusedLocationProviderClient to get lastLocation and if
     * this is not available the function start LocationRequest.
     * More info on https://developer.android.com/training/location/request-updates
     */
    public static LiveData<Location> getCurrentDeviceLocation() {
        if (isDeviceLocationEnabled()) {
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Go4Lunch.getContext());
            try {
                Permissions.check(Go4Lunch.getContext(), Const.PERMISSIONS, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        Task<android.location.Location> getLocation = fusedLocationProviderClient.getLastLocation();
                        getLocation.addOnCompleteListener(task -> {
                            if (task.isSuccessful() && (task.getResult() != null)) {
                                data.setValue(task.getResult());
                            } else {
                                data.setValue(null);
                            }
                        });
                    }
                });
            } catch (SecurityException e) {
                e.getMessage();
            }
        } else {
            return null;
        }
        return data;
    }

    public static boolean isDeviceLocationEnabled() {
        LocationManager lm = (LocationManager) Go4Lunch.getContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            assert lm != null;
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.getStackTrace();
        }
        return false;
    }

    /**
     * A dialog that displays a location denied message.
     */
    public static class LocationDisabledDialog extends DialogFragment {

        public static LocationDisabledDialog newInstance() {
            return new LocationDisabledDialog();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.ask_location_account_title)
                    .setMessage(R.string.ask_location_account_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) Go4Lunch.getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
