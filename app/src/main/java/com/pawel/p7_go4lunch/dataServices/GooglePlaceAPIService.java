package com.pawel.p7_go4lunch.dataServices;

import com.pawel.p7_go4lunch.utils.Const;

public class GooglePlaceAPIService {
    public static GooglePlaceAPI getGooglePlaceAPI() {
        return RetrofitClient.getRetrofit(Const.GOOGLE_BASE_URL).create(GooglePlaceAPI.class);
    }
}
