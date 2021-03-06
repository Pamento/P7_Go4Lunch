package com.pawel.p7_go4lunch.dataServices.repositorys;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pawel.p7_go4lunch.BuildConfig;
import com.pawel.p7_go4lunch.dataServices.GooglePlaceAPI;
import com.pawel.p7_go4lunch.dataServices.RetrofitClient;
import com.pawel.p7_go4lunch.dataServices.cache.InMemoryRestosCache;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.autocomplete.AutoResponse;
import com.pawel.p7_go4lunch.model.autocomplete.Predictions;
import com.pawel.p7_go4lunch.model.googleApiPlaces.RestaurantResult;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.model.googleApiPlaces.SingleRestaurant;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;
import com.pawel.p7_go4lunch.utils.helpers.RestaurantsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class GooglePlaceRepository {

    private static volatile GooglePlaceRepository instance;
    private static String mCurrentLocation;
    private final GooglePlaceAPI mGooglePlaceAPIService;
    private final RestaurantsHelper mRestaurantsHelper;
    private final List<Restaurant> mRestaurants = new ArrayList<>();
    private List<Restaurant> mRestaurantsAutoCom = new ArrayList<>();
    private final MutableLiveData<List<Restaurant>> mRestaurantLiveData = new MutableLiveData<>();
    private final Location mCrntLocation = new Location("");
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final MutableLiveData<AutoSearchEvents> mAutoSearchEvents = new MutableLiveData<>(AutoSearchEvents.AUTO_NULL);
    private final InMemoryRestosCache mCache = InMemoryRestosCache.getInstance();

    public GooglePlaceRepository() {
        mRestaurantsHelper = new RestaurantsHelper();
        mGooglePlaceAPIService = getGooglePlaceApiService();
    }

    public void setRestaurantLiveData(List<Restaurant> restaurants) {
        mRestaurantLiveData.setValue(restaurants);
    }

    public void setRestaurantLiveData(boolean isNearby) {
        if (isNearby) {
            mRestaurantLiveData.setValue(mRestaurants);
            mCache.cacheRestoInMemory(mRestaurants);
            mCache.setLocation(mCrntLocation);
        } else {
            mRestaurantLiveData.setValue(mRestaurantsAutoCom);
        }
    }

    public void setAutoSearchEvents(AutoSearchEvents autoSearchEvents) {
        mAutoSearchEvents.setValue(autoSearchEvents);
    }

    // ................................................................. GETTERS
    public static GooglePlaceRepository getInstance() {
        if (instance == null) instance = new GooglePlaceRepository();
        return instance;
    }

    private GooglePlaceAPI getGooglePlaceApiService() {
        return RetrofitClient.getRequestApi();
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return mRestaurantLiveData;
    }

    public LiveData<AutoSearchEvents> getAutoSearchEvents() {
        return mAutoSearchEvents;
    }

    public LiveData<AutoSearchEvents> getAutoSearchEvent() {
        return mAutoSearchEvents;
    }

    public String getCurrentLocation() {
        return mCurrentLocation;
    }

    public Restaurant getRestoSelected(String placeId) {
        List<Restaurant> rL = mRestaurantLiveData.getValue();
        if (rL != null) {
            for (Restaurant r : rL) {
                if (r.getPlaceId().equals(placeId)) return r;
            }
        }
        return null;
    }

    // .........................................................................SETTERS
    public void setCurrentLocation(Location cLoc) {
        GooglePlaceRepository.mCurrentLocation = cLoc.getLatitude() + "," + cLoc.getLongitude();
        mCrntLocation.setLatitude(cLoc.getLatitude());
        mCrntLocation.setLongitude(cLoc.getLongitude());
    }

    // .........................................................................STREAMS
    public Observable<RestaurantResult> streamGetRestaurantsNearby(String location, int radius) {
        return mGooglePlaceAPIService.getNearbyRestaurants(location, radius, BuildConfig.API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public Observable<AutoResponse> streamAutocompletePlaces(String input, String lang, int radius, String location, String origin) {
        return mGooglePlaceAPIService.getAutocompleteRestos(input, lang, radius, location, origin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    public Observable<Result> getRestaurantNearby(String location, int radius) {
        return streamGetRestaurantsNearby(location, radius)
                .map(RestaurantResult::getResults)
                .concatMap(results -> {
                    setRestaurantsNearby(results);
                    return Observable.fromIterable(results)
                            .subscribeOn(Schedulers.io());
                });
    }

    public Observable<Predictions> getRestoAutocompleteBy(String input, String lang, int radius, String location, String origin) {
        return streamAutocompletePlaces(input, lang, radius, location, origin)
                .map(autoResponse -> {
                    String s = autoResponse.getStatus();
                    if (s.equals("OK")) {
                        GooglePlaceRepository.this.setAutoSearchEvents(AutoSearchEvents.AUTO_OK);
                    } else if (s.equals("ZERO_RESULTS")) {
                        GooglePlaceRepository.this.setAutoSearchEvents(AutoSearchEvents.AUTO_ZERO_RESULT);
                    } else {
                        GooglePlaceRepository.this.setAutoSearchEvents(AutoSearchEvents.AUTO_ERROR);
                    }
                    return autoResponse.getPredictions();
                }).flatMap(predictions -> {
                    GooglePlaceRepository.this.setRestoFromPredictions(predictions);
                    return Observable.fromIterable(predictions)
                            .subscribeOn(Schedulers.io());
                });
    }

    public Observable<Result> getRestaurantContact(String placeId) {
        return mGooglePlaceAPIService.getContactOfResto(placeId, BuildConfig.API_KEY)
                .map(SingleRestaurant::getResult);
    }

    public Observable<Result> getRestaurantDetails(String placeId) {
        return mGooglePlaceAPIService.getDetailsOfResto(placeId)
                .map(SingleRestaurant::getResult);
    }

    public void disposeDisposable() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    // ................................................................. UTILS FUNCTIONS
    private void setRestaurantsNearby(List<Result> results) {
        for (Result r : results) {
            Restaurant res = createRestaurant(r);
            if (res != null) {
                if (mRestaurants.contains(res)) {
                    mRestaurants.set(mRestaurants.indexOf(res), res);
                } else {
                    mRestaurants.add(res);
                }
            }
        }
    }

    public void findRestoForUpdates(Result result, boolean isNearByAPI) {
        List<Restaurant> lR;
        Restaurant rcp;
        if (isNearByAPI) {
            lR = mRestaurants;
        } else {
            lR = mRestaurantsAutoCom;
        }
        if (lR.size() > 0) {
            for (int i = 0; i < lR.size(); i++) {
                if (lR.get(i).getPlaceId().equals(result.getPlaceId())) {
                    rcp = lR.get(i);
                    if (isNearByAPI) updateRestoWithContact(result, rcp);
                    else updateRestoWithDetails(result, rcp);
                }
            }
        }
    }

    private void updateRestoWithDetails(Result result, Restaurant rcp) {
        if (result != null) {
            Restaurant updated = mRestaurantsHelper.updateWithDetail(result, rcp);
            mRestaurantsAutoCom.set(mRestaurantsAutoCom.indexOf(rcp), updated);
        }
    }

    private void updateRestoWithContact(Result result, Restaurant rcp) {
        if (rcp != null) {
            Restaurant updated = mRestaurantsHelper.updateRestoWithContact(result, rcp);
            mRestaurants.set(mRestaurants.indexOf(rcp), updated);
        }
    }

    public Restaurant createRestaurant(Result result) {
        if (result != null) {
            return mRestaurantsHelper.createRestaurant(result, mCrntLocation);
        }
        return null;
    }

    private void setRestoFromPredictions(List<Predictions> predictions) {
        // First, reset previous value of mRestaurantsAutoCom.
        mRestaurantsAutoCom = new ArrayList<>();
        if (predictions != null) {
            mRestaurantsAutoCom.addAll(mRestaurantsHelper.setRestoFromPredictions(predictions));
        }
    }
}
