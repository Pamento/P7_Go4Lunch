package com.pawel.p7_go4lunch.dataServices;

import com.pawel.p7_go4lunch.model.googleApiPlaces.ApiPlace;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface GooglePlaceAPI {
    // TODO wite the code
    @GET
    Call<ApiPlace> getNearByPlace(@Url String url);
}
