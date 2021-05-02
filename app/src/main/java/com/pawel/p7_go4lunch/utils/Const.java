package com.pawel.p7_go4lunch.utils;

import android.Manifest;

public class Const {

    // Permissions
    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 9003;
    private static final int REQUEST_LOCATION = 9002;

    // GOOGLE __Maps
    public static final float DEFAULT_ZOOM = 10f;
    public static final int REQUEST_CHECK_SETTINGS = 9004;

    // GOOGLE __Service apiPlace
    public static final String GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/place/";


    // FIREBASE login request id
    public static final int RC_SIGN_IN = 697;

    // FIREBASE __Service
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_FAVORITES_RESTAURANTS = "restaurants";
    public static final String COLLECTION_CHOSEN_RESTAURANTS = "restaurants";
    public static final String FIREBASE_ADAPTER_QUERY_EMAIL = "email";
    public static final String FIREBASE_ADAPTER_QUERY_RESTAURANT = "userRestaurant";

    // DIALOGS
    /**
     * google maps dialog error link{isMapsServiceOk()}
     */
    public static final int ERROR_DIALOG_REQUEST = 9001;
    // delete dialog
    public static final String DELETE_ALERT_DIALOG = "delete_alert_dialog";
    // ask location permission dialog
    public static final String PERMISSIONS_ALERT_DIALOG = "permissions_alert_dialog";

    // AboutRestaurantActivity
    public static final String EXTRA_KEY_RESTAURANT = "extra_key_restaurant";
    // final variable for makePhoneCall() in AboutRestaurantActivity
    public static final int REQUEST_CALL = 1;

    // Notification
    public static final int NOTIF_PENDING_ID = 9006;
    public static final CharSequence VERBOSE_NOTIF_CHANNEL_NAME = "resto place remainder";
    public static final String VERBOSE_NOTIF_CHANNEL_DESCRIPT = "Shows notifications remainder for 12 pm";
    public static final String CHANNEL_ID = "REMAINDER_NOTIFICATION";
    public static final int NOTIF_ID = 9005;
    public static final String KEY_USER_ID = "KEY_USER_ID";
    public static final String KEY_RESTO_ID = "KEY_RESTO_ID";
    public static final String ALARM_ID = "alarm_id";
    public static final int ALARM_MULTIPLE = 1009;
    public static final int ALARM_SINGLE = 1001;
    public static final int ONE_DAY_IN_MILLIS = 8640000;
}
