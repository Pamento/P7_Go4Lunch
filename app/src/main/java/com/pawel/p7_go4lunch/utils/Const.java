package com.pawel.p7_go4lunch.utils;

import android.Manifest;

public class Const {

    public static final int RC_SIGN_IN = 697;
    public static final int ERROR_DIALOG_REQUEST = 9001;

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 9003;

    public static final float DEFAULT_ZOOM = 15f;

    // GOOGLE __Service
    public static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    // FIREBASE __Service
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_FAVORITES_RESTAURANTS = "restaurants";
    public static final String COLLECTION_CHOSEN_RESTAURANTS = "restaurants";
}
