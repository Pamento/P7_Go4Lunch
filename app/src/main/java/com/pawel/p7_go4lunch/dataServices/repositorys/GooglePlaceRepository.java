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
    private static final String TAG = "AUTO_COM";
    private static volatile GooglePlaceRepository instance;
    private static String mCurrentLocation;
    private static LatLng initialLatLng;
    private final GooglePlaceAPI mGooglePlaceAPIService;
    private final List<Restaurant> mRestaurants = new ArrayList<>();
    private List<Restaurant> mRestaurantsAutoCom = new ArrayList<>();
    private final MutableLiveData<List<Restaurant>> mRestaurantLiveData = new MutableLiveData<>();
    private final Location mCntLocation = new Location("");
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final MutableLiveData<AutoSearchEvents> mAutoSearchEvents = new MutableLiveData<>(AutoSearchEvents.AUTO_NULL);

    public GooglePlaceRepository() {
        mGooglePlaceAPIService = getGooglePlaceApiService();
    }

    public void setRestaurantLiveData() {
        mRestaurantLiveData.setValue(mRestaurantsAutoCom);
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

    public MutableLiveData<AutoSearchEvents> getAutoSearchEvent() {
        return mAutoSearchEvents;
    }

    public String getCurrentLocation() {
        return mCurrentLocation;
    }

    public Restaurant getRestoSelected(String placeId) {
        List<Restaurant> rL = mRestaurantLiveData.getValue();
        for (Restaurant r: rL) {
            if (r.getPlaceId().equals(placeId)) return r;
        }
        return null;
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
                    String s = autoResponse.getStatus();
                    Log.i(TAG, "getRestoAutocompleteBy: getStatus( " + autoResponse.getStatus());
                    Log.i(TAG, "getRestoAutocompleteBy: local string == " + s);
                    if (s.equals("OK")) {
                        Log.i(TAG, "getRestoAutocompleteBy: OK");
                        GooglePlaceRepository.this.setAutoSearchEvents(AutoSearchEvents.AUTO_OK);
                    } else if (s.equals("ZERO_RESULTS")) {
                        Log.i(TAG, "getRestoAutocompleteBy: ZERO");
                        GooglePlaceRepository.this.setAutoSearchEvents(AutoSearchEvents.AUTO_ZERO_RESULT);
                    } else {
                        Log.e(TAG, "getRestoAutocompleteBy: ERROR");
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
        Log.i(TAG, "GooglePlaceRepository.getRestaurantDetails: placeId:: " + placeId);
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

    public void findRestoForUpdates(Result result, boolean apiPlace) {
        List<Restaurant> lR;
        Restaurant rcp;
        if (apiPlace) {
            lR = mRestaurants;
        } else {
            lR = mRestaurantsAutoCom;
        }
        if (lR.size() > 0) {
            for (int i = 0; i < lR.size(); i++) {
                if (lR.get(i).getPlaceId().equals(result.getPlaceId())) {
                    rcp = lR.get(i);
                    if (apiPlace) updateRestoWithContact(result, rcp, apiPlace);
                    else updateRestoWithDetails(result, rcp);
                }
            }
        }
    }

    private void updateRestoWithDetails(Result result, Restaurant rcp) {
        Log.i(TAG, "GooglePlaceRepository.updateRestoWithDetails: ");
//        Location l = new Location("");
        if (result != null) {
            Log.i(TAG, "GooglePlaceRepository.updateRestoWithDetails: result " + result.getName());
            rcp.setDateCreated(new Date());
            if (result.getName() != null) rcp.setName(result.getName());
            if (result.getVicinity() != null) rcp.setAddress(result.getVicinity());
            if (result.getGeometry() != null) {
                Log.d(TAG, "GooglePlaceRepository.updateRestoWithDetails.getLocation: Location " + result.getGeometry().getLocation());
                rcp.setLocation(result.getGeometry().getLocation());
//                l.setLatitude(result.getGeometry().getLocation().getLat());
//                l.setLongitude(result.getGeometry().getLocation().getLng());
//                float dt = mCntLocation.distanceTo(l);
//                rcp.setDistance(Math.round(dt));
            }
            if (result.getOpeningHours() != null)
                rcp.setOpeningHours(result.getOpeningHours());
            if (result.getPhotos() != null)
                rcp.setImage(getPhoto(result.getPhotos().get(0).getPhotoReference()));
            if (result.getRating() != null) rcp.setRating(result.getRating());
            rcp.setUserList(new ArrayList<>());
            mRestaurantsAutoCom.set(mRestaurantsAutoCom.indexOf(rcp), rcp);
        }
    }

    private void updateRestoWithContact(Result result, Restaurant rcp, boolean apiPlace) {
        if (rcp != null) {
            rcp.setPhoneNumber(result.getInternationalPhoneNumber());
            rcp.setWebsite(result.getWebsite());
            if (apiPlace) mRestaurants.set(mRestaurants.indexOf(rcp), rcp);
            else mRestaurantsAutoCom.set(mRestaurantsAutoCom.indexOf(rcp), rcp);
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
        mRestaurantsAutoCom = new ArrayList<>();
        if (predictions != null) {
            Log.i(TAG, "setRestoFromPredictions:size N° " + predictions.size());
            for (Predictions prd : predictions) {
                if (prd.getTypes().contains("restaurant")) {
                    Restaurant rst = new Restaurant();
                    rst.setPlaceId(prd.getPlaceId());
                    rst.setDistance(prd.getDistanceMeters());
                    mRestaurantsAutoCom.add(rst);
                }
            }
        }
    }
}
