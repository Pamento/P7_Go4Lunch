package com.pawel.p7_go4lunch.dataServices.repositorys;

import android.util.Log;

import com.pawel.p7_go4lunch.dataServices.GooglePlaceAPI;
import com.pawel.p7_go4lunch.dataServices.RetrofitClient;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.googleApiPlaces.RestaurantResult;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GooglePlaceRepository {

    private static volatile GooglePlaceRepository instance;
    private final GooglePlaceAPI mGooglePlaceAPIService;
    private final List<Restaurant> mRestaurants = new ArrayList<>();

    public GooglePlaceRepository() {
        mGooglePlaceAPIService = getGooglePlaceApiService();
    }

    public static GooglePlaceRepository getInstance() {
        if (instance == null) instance = new GooglePlaceRepository();
        return instance;
    }

    private GooglePlaceAPI getGooglePlaceApiService() {
        return RetrofitClient.getRequestApi();
    }

    public List<Restaurant> getRestaurants() {
        return mRestaurants;
    }
    public Observable<RestaurantResult> streamFetchRestaurantsPlaces(String location, int radius, String key) {
        return mGooglePlaceAPIService.getNearbyRestaurants(location, radius, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public Observable<RestaurantResult> streamFetchRestaurantsDetails(String placeId, String key) {
        return mGooglePlaceAPIService.getDetailsOfRestaurant(placeId, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public ObservableSource<List<RestaurantResult>> streamCombinePlacesAndDetails(String location, int radius, String key) {
        return streamFetchRestaurantsPlaces(location, radius, key)
                .map(RestaurantResult::getResults)
                .concatMap(results -> {
                    fitRestaurantsList(results);
                    return Observable.fromIterable(results)
                            .concatMap(result -> streamFetchRestaurantsDetails(result.getPlaceId(),key));
                }).toList().toObservable();
    }

    private void fitRestaurantsList(List<Result> results) {
        boolean isEmpty = false;
        if (mRestaurants.isEmpty()) isEmpty = true;
        Log.i("REQUEST", "fitRestaurantsList: ");
        if (results != null) {
            for (Result result: results) {
                Restaurant restaurant = new Restaurant();
                restaurant.setPlaceId(result.getPlaceId());
                restaurant.setName(result.getName());
                restaurant.setAddress(result.getVicinity());
                restaurant.setLocation(result.getGeometry().getLocation());
                restaurant.setOpeningHours(result.getOpeningHours());
                restaurant.setImage(result.getPhotos().get(0).getPhotoReference());
                restaurant.setRating(result.getRating());
                restaurant.setPhoneNumber(result.getFormattedPhoneNumber());
                restaurant.setWebsite(result.getWebsite());
                if (isEmpty) {
                    mRestaurants.add(restaurant);
                } else {
                    for (Restaurant restau : mRestaurants) {
                        if (restau.getPlaceId().equals(restaurant.getPlaceId())) {
                            mRestaurants.set(mRestaurants.indexOf(restaurant), restaurant);
                        }
                    }
                }
            }
        }
    }
}
