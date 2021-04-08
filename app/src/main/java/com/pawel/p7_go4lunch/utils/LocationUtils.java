package com.pawel.p7_go4lunch.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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

public abstract class LocationUtils {

    private static final MutableLiveData<Location> data = new MutableLiveData<>();
    private static final String TAG = "SEARCH";

    /**
     * @fun getCurrentDeviceLocation use FusedLocationProviderClient to get lastLocation and if
     * this is not available the function start LocationRequest.
     * More info on https://developer.android.com/training/location/request-updates
     */
    public static LiveData<Location> getCurrentDeviceLocation(Context context) {
        Log.i(TAG, "LOCATION _getCurrentDeviceLocation: " + context);
        if (isDeviceLocationEnabled(context)) {
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            try {
                Permissions.check(context, Const.PERMISSIONS, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        // TODO check if device has location & network enabled (Kitkat & above)
                        Task<android.location.Location> getLocation = fusedLocationProviderClient.getLastLocation();
                        getLocation.addOnCompleteListener(task -> {
                            if (task.isSuccessful() && (task.getResult() != null)) {
                                data.setValue(task.getResult());
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

    public static boolean isDeviceLocationEnabled(Context context) {
        //LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //Log.i(TAG, "isDeviceLocationEnabled: _____________________________" + Build.VERSION.SDK_INT);
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
}
