package com.pawel.p7_go4lunch.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.pawel.p7_go4lunch.R;

public abstract class LocationUtils {
    private static final String TAG = "SEARCH";
//    public static Location getCurrentDeviceLocation(Context context) {
//        Log.i(TAG, "LOCATION _getCurrentDeviceLocation: " + context);
//        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
//        final Location[] currentLocation = new Location[1];
//        if (ActivityCompat.checkSelfPermission(context, Const.PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(context, Const.PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED) {
//            // TODO check if device has location & network enabled (Kitkat & above)
//            Task<Location> getLocation = fusedLocationProviderClient.getLastLocation();
//            getLocation.addOnCompleteListener(task -> {
//                if (task.isSuccessful() && (task.getResult() != null)) {
//                    currentLocation[0] = (Location) task.getResult();
//                    Log.i(TAG, "LOCATION _getCurrentDeviceLocation: task.successful " + currentLocation[0]);
//                    //LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                }
//            });
//        }
//        return currentLocation[0];
//    }

    public static boolean isDeviceLocationEnabled(Context context) {
        //LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
