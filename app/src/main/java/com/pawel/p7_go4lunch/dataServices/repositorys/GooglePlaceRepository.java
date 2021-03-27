package com.pawel.p7_go4lunch.dataServices.repositorys;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.pawel.p7_go4lunch.dataServices.GooglePlaceAPI;
import com.pawel.p7_go4lunch.dataServices.RetrofitClient;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.googleApiPlaces.RestaurantResult;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.model.googleApiPlaces.SingleRestaurant;
import com.pawel.p7_go4lunch.utils.WasCalled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GooglePlaceRepository {

    private static volatile GooglePlaceRepository instance;
    private final GooglePlaceAPI mGooglePlaceAPIService;
    private final List<Restaurant> mRestaurants = new ArrayList<>();
    private static String mCurrentLocation;
    private static LatLng initialLatLng;

    public GooglePlaceRepository() {
        mGooglePlaceAPIService = getGooglePlaceApiService();
    }

    // ................................................................. GETTERS
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

    public LatLng getInitialLatLng() {
        return initialLatLng;
    }

    // .........................................................................SETTERS
    public void setCurrentLocation(Location cLoc) {
        GooglePlaceRepository.mCurrentLocation = cLoc.getLatitude() + "," + cLoc.getLongitude();;
    }

    public void setInitialLatLng(LatLng latLng) {
        GooglePlaceRepository.initialLatLng = latLng;
    }

    // .........................................................................STREAMS
    public Observable<RestaurantResult> streamFetchRestaurantsPlaces(String location, int radius, String key) {
        return mGooglePlaceAPIService.getNearbyRestaurants(location, radius, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public Observable<SingleRestaurant> streamFetchRestaurantsDetails(String placeId, String key) {
        return mGooglePlaceAPIService.getDetailsOfRestaurant(placeId, key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
                .map(singleRestaurant -> {
                    fitRestaurantsList(singleRestaurant.getResult(), key);
                    return singleRestaurant;
                });
    }

    public ObservableSource<List<SingleRestaurant>> streamCombinePlacesAndDetails(String location, int radius, String key) {
        return streamFetchRestaurantsPlaces(location, radius, key)
                .map(RestaurantResult::getResults)
                .concatMap(results -> Observable.fromIterable(results)
                        .concatMap(result -> streamFetchRestaurantsDetails(result.getPlaceId(), key))
                        .toList().toObservable());
    }

    // ................................................................. UTILS FUNCTIONS
    private void fitRestaurantsList(Result result, String key) {
        if (WasCalled.restaurantsList()) {
            for (Restaurant res : mRestaurants) {
                if (res.getPlaceId().equals((result).getPlaceId())) {
                    mRestaurants.set(mRestaurants.indexOf(res), createRestaurant(result,key));
                } else {
                    mRestaurants.add(createRestaurant(result, key));
                }
            }
        } else {
            mRestaurants.add(createRestaurant(result, key));
        }
    }

    public Restaurant createRestaurant(Result result, String key) {
        Restaurant restaurant = new Restaurant();
        if (result != null) {
            restaurant.setPlaceId(result.getPlaceId());
            restaurant.setName(result.getName());
            restaurant.setAddress(result.getVicinity());
            restaurant.setLocation(result.getGeometry().getLocation());
            restaurant.setOpeningHours(result.getOpeningHours());
            restaurant.setImage(getPhoto(result.getPhotos().get(0).getPhotoReference(), key));
            restaurant.setRating(result.getRating());
            restaurant.setPhoneNumber(result.getInternationalPhoneNumber());
            restaurant.setWebsite(result.getWebsite());
            Log.i("REQUEST", "fitRestaurantsList: restaurants(" + mRestaurants.size() + "); 1_restaurant: \n" + restaurant.toString());
            return restaurant;
        }
        return null;
    }

    public String getPhoto(String photoReference, String key) {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + key;
    }
}
