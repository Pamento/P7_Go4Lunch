package com.pawel.p7_go4lunch.dataServices.repositorys;

import com.pawel.p7_go4lunch.dataServices.GooglePlaceAPIService;

public class GooglePlaceRepository {

    private static volatile GooglePlaceRepository instance;
    private final GooglePlaceAPIService mGooglePlaceAPIService;

    public GooglePlaceRepository(GooglePlaceAPIService googlePlaceAPIService) {
        mGooglePlaceAPIService = googlePlaceAPIService;
    }

    public static GooglePlaceRepository getInstance(GooglePlaceAPIService googlePlaceAPIService) {
        if (instance == null) instance = new GooglePlaceRepository(googlePlaceAPIService);
        return instance;
    }
}
