package com.pawel.p7_go4lunch.dataServices.repositorys;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.pawel.p7_go4lunch.BuildConfig;
import com.pawel.p7_go4lunch.dataServices.GooglePlaceAPI;
import com.pawel.p7_go4lunch.dataServices.RetrofitClient;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.autocomplete.AutoResponse;
import com.pawel.p7_go4lunch.model.autocomplete.Predictions;
import com.pawel.p7_go4lunch.model.googleApiPlaces.RestaurantResult;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.model.googleApiPlaces.SingleRestaurant;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class GooglePlaceRepository {
    private static final String TAG = "SEARCH";
    private static volatile GooglePlaceRepository instance;
    private static String mCurrentLocation;
    private static LatLng initialLatLng;
    private final GooglePlaceAPI mGooglePlaceAPIService;
    private final List<Restaurant> mRestaurants = new ArrayList<>();
    private final List<Restaurant> mRestaurantsAutoCom = new ArrayList<>();
    private final MutableLiveData<List<Restaurant>> mRestaurantLiveData = new MutableLiveData<>();
    private final Location mCntLocation = new Location("");
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final MutableLiveData<AutoSearchEvents> mAutoSearchEvents = new MutableLiveData<>(AutoSearchEvents.AUTO_NULL);

    public GooglePlaceRepository() {
        mGooglePlaceAPIService = getGooglePlaceApiService();
    }

    public void setRestaurantLiveData() {
        mRestaurantLiveData.setValue(mRestaurants);
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

    public MutableLiveData<List<Restaurant>> getRestaurants() {
        return mRestaurantLiveData;
    }

    public List<Restaurant> getRestaurantsCache() {
        return mRestaurants;
    }

    public LatLng getInitialLatLng() {
        return initialLatLng;
    }

    public MutableLiveData<AutoSearchEvents> getAutoSearchEvents() {
        return mAutoSearchEvents;
    }

    // .........................................................................SETTERS
    public void setCurrentLocation(Location cLoc) {
        GooglePlaceRepository.mCurrentLocation = cLoc.getLatitude() + "," + cLoc.getLongitude();
        mCntLocation.setLatitude(cLoc.getLatitude());
        mCntLocation.setLongitude(cLoc.getLongitude());
    }

    public void setInitialLatLng(LatLng latLng) {
        GooglePlaceRepository.initialLatLng = latLng;
    }

    // .........................................................................STREAMS
    public Observable<RestaurantResult> streamFetchRestaurantsPlaces(String location, int radius) {
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
        return streamFetchRestaurantsPlaces(location, radius)
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
                    if (autoResponse.getStatus().equals("OK")) {
                        GooglePlaceRepository.this.setAutoSearchEvents(AutoSearchEvents.AUTO_OK);
                    } else if (autoResponse.getStatus().equals("ZERO_RESULT")) {
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

    public String getPhoto(String photoReference) {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + BuildConfig.API_KEY;
    }

    public void disposeDisposable() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    // ................................................................. UTILS FUNCTIONS
    private void setRestaurantsNearby(List<Result> results) {
        Log.i("SEARCH", "setRestaurantsNearby: " + results.size());
        for (Result r : results) {
            Restaurant res = createRestaurant(r);
            if (mRestaurants.contains(res)) {
                Log.i(TAG, "setRestaurantsNearby: the same. Id::: " + res.getPlaceId());
                mRestaurants.set(mRestaurants.indexOf(res), res);
            } else {
                mRestaurants.add(res);
            }
        }
    }

    public void upDateRestaurantsWithContact(Result result) {
        Restaurant mRcp;
        for (int i = 0; i < mRestaurants.size(); i++) {
            if (mRestaurants.get(i).getPlaceId().equals(result.getPlaceId())) {
                mRcp = mRestaurants.get(i);
                updateAndReplaceResto(result, mRcp);
            }
        }
    }

    private void updateAndReplaceResto(Result result, Restaurant mRcp) {
        if (mRcp != null) {
            mRcp.setPhoneNumber(result.getInternationalPhoneNumber());
            mRcp.setWebsite(result.getWebsite());
            mRestaurants.set(mRestaurants.indexOf(mRcp), mRcp);
        }
    }

    public Restaurant createRestaurant(Result result) {
        Restaurant r = new Restaurant();
        Location l = new Location("");
        if (result != null) {
            if (result.getPlaceId() != null) r.setPlaceId(result.getPlaceId());
            r.setDateCreated(new Date());
            if (result.getName() != null) r.setName(result.getName());
            if (result.getVicinity() != null) r.setAddress(result.getVicinity());
            if (result.getGeometry() != null) {
                r.setLocation(result.getGeometry().getLocation());
                l.setLatitude(result.getGeometry().getLocation().getLat());
                l.setLongitude(result.getGeometry().getLocation().getLng());
                float dt = mCntLocation.distanceTo(l);
                r.setDistance(Math.round(dt));
            }
            if (result.getOpeningHours() != null)
                r.setOpeningHours(result.getOpeningHours());
            if (result.getPhotos() != null)
                r.setImage(getPhoto(result.getPhotos().get(0).getPhotoReference()));
            if (result.getRating() != null) r.setRating(result.getRating());
            if (result.getInternationalPhoneNumber() != null)
                r.setPhoneNumber(result.getInternationalPhoneNumber());
            if (result.getWebsite() != null) r.setWebsite(result.getWebsite());
            r.setUserList(new ArrayList<>());
            return r;
        }
        return null;
    }

    private void setRestoFromPredictions(List<Predictions> predictions) {
        Log.i(TAG, "setRestoFromPredictions: ");
        if (predictions != null) {
            Log.i(TAG, "setRestoFromPredictions: N° " +predictions.size());
            for (Predictions prd: predictions) {
                Restaurant rst = new Restaurant();
                rst.setPlaceId(prd.getPlaceId());
                rst.setDistance(prd.getDistanceMeters());
                mRestaurantsAutoCom.add(rst);
            }
        }
    }
}
