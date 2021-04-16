package com.pawel.p7_go4lunch.dataServices;

import com.pawel.p7_go4lunch.utils.Const;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
//    private static Retrofit sRetrofit = null;
//    public static Retrofit getRetrofit(String baseUrl) {
//        if (sRetrofit == null) {
//            sRetrofit = new Retrofit.Builder()
//                    .baseUrl(baseUrl)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .build();
//        }
//        return sRetrofit;
//    }

    private static final Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    .baseUrl(Const.GOOGLE_BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());

     private static final Retrofit retrofit = retrofitBuilder.build();

    private static final GooglePlaceAPI requestApi = retrofit.create(GooglePlaceAPI.class);

    //    public static GooglePlaceAPI getRequestApi(){
//        return requestApi;
//    }
    public static GooglePlaceAPI getRequestApi() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Const.GOOGLE_BASE_URL)
                .client(new OkHttpClient.Builder().addInterceptor(interceptor).build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(GooglePlaceAPI.class);
    }
}
