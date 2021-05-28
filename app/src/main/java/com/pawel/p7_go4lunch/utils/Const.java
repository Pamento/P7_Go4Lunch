package com.pawel.p7_go4lunch.utils;

import android.Manifest;

public class Const {

    // Permissions
    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 9003;

    // GOOGLE __Maps
    public static final float DEFAULT_ZOOM = 10f;
    public static final int REQUEST_CHECK_SETTINGS = 9004;

    // GOOGLE __Service apiPlace
    public static final String GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/place/";


    // FIREBASE login request id
    public static final int RC_SIGN_IN = 697;

    // FIREBASE __Service
    public static final String COLLECTION_USERS = "users";

    // DIALOGS
    /**
     * google maps dialog error link{isMapsServiceOk()}
     */
    public static final int ERROR_DIALOG_REQUEST = 9001;
    // delete dialog
    public static final String DELETE_ALERT_DIALOG = "delete_alert_dialog";

    // AboutRestaurantActivity
    public static final String EXTRA_KEY_RESTAURANT = "extra_key_restaurant";

    // Notification
    public static final int NOTIF_PENDING_ID = 9006;
    public static final CharSequence VERBOSE_NOTIF_CHANNEL_NAME = "resto_place_remainder";
    public static final String VERBOSE_NOTIF_CHANNEL_DESCRIPT = "Shows_notifications_remainder_for_time_set";
    public static final String CHANNEL_ID = "NOTIF_CHANNEL_01";
    public static final int NOTIF_ID = 9005;
    public static final String ALARM_ID = "alarm_id";
    public static final int ALARM_MULTIPLE = 1009;
    public static final int ALARM_SINGLE = 1001;
    public static final int ONE_DAY_IN_MILLIS = 86400000;
}
