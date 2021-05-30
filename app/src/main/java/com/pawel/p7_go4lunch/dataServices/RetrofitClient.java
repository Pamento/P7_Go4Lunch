package com.pawel.p7_go4lunch.dataServices;

import com.pawel.p7_go4lunch.utils.Const;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    .baseUrl(Const.GOOGLE_BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());

     private static final Retrofit retrofit = retrofitBuilder.build();

    private static final GooglePlaceAPI requestApi = retrofit.create(GooglePlaceAPI.class);

    public static GooglePlaceAPI getRequestApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Const.GOOGLE_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(GooglePlaceAPI.class);
    }
}
