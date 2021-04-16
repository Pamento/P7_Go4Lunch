package com.pawel.p7_go4lunch.dataServices.repositorys;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.pawel.p7_go4lunch.BuildConfig;
import com.pawel.p7_go4lunch.dataServices.GooglePlaceAPI;
import com.pawel.p7_go4lunch.dataServices.RetrofitClient;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.googleApiPlaces.RestaurantResult;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.model.googleApiPlaces.SingleRestaurant;

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
    private final GooglePlaceAPI mGooglePlaceAPIService;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private MutableLiveData<List<Restaurant>> mRestaurantLiveData = new MutableLiveData<>();
    private static String mCurrentLocation;
    private static LatLng initialLatLng;
    private final Location mCntLocation = new Location("");
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public GooglePlaceRepository() {
        mGooglePlaceAPIService = getGooglePlaceApiService();
    }


    public MutableLiveData<List<Restaurant>> getRestaurantLiveData() {
        return mRestaurantLiveData;
    }

    public void setRestaurantLiveData() {
        mRestaurantLiveData.setValue(mRestaurants);
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

    public Observable<Result> getRestaurantNearby(String location, int radius) {
        return streamFetchRestaurantsPlaces(location, radius)
                .map(RestaurantResult::getResults)
                .concatMap(results -> {
                    setRestaurantsNearby(results);
                    return Observable.fromIterable(results)
                            .subscribeOn(Schedulers.io());
                });
    }

    public Observable<Result> getRestaurantDetail(String id) {
        return mGooglePlaceAPIService.getDetailsOfRestaurant(id, BuildConfig.API_KEY)
                .map(SingleRestaurant::getResult);
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

    public void upDateRestaurantsWithDetails(Result result) {
        Restaurant mRcp;
        for (int i = 0; i < mRestaurants.size(); i++) {
            if (mRestaurants.get(i).getPlaceId().equals(result.getPlaceId())) {
                mRcp = mRestaurants.get(i);
                updateAndReplace(result, mRcp);
            }
        }
    }

    private void updateAndReplace(Result result, Restaurant mRcp) {
        if (mRcp != null) {
            mRcp.setPhoneNumber(result.getInternationalPhoneNumber());
            mRcp.setWebsite(result.getWebsite());
            mRestaurants.set(mRestaurants.indexOf(mRcp), mRcp);
            Log.i(TAG, "upDateRestaurantsWithDetails::: ");
            Log.i(TAG, "" + mRcp.toString());
        }
    }

    public Restaurant createRestaurant(Result result) {
        Restaurant restaurant = new Restaurant();
        Location l = new Location("");
        if (result != null) {
            if (result.getPlaceId() != null) restaurant.setPlaceId(result.getPlaceId());
            restaurant.setDateCreated(new Date());
            if (result.getName() != null) restaurant.setName(result.getName());
            if (result.getVicinity() != null) restaurant.setAddress(result.getVicinity());
            if (result.getGeometry() != null) {
                restaurant.setLocation(result.getGeometry().getLocation());
                l.setLatitude(result.getGeometry().getLocation().getLat());
                l.setLongitude(result.getGeometry().getLocation().getLng());
                float dt = mCntLocation.distanceTo(l);
                restaurant.setDistance(Math.round(dt));
            }
            if (result.getOpeningHours() != null)
                restaurant.setOpeningHours(result.getOpeningHours());
            if (result.getPhotos() != null)
                restaurant.setImage(getPhoto(result.getPhotos().get(0).getPhotoReference()));
            if (result.getRating() != null) restaurant.setRating(result.getRating());
            if (result.getInternationalPhoneNumber() != null)
                restaurant.setPhoneNumber(result.getInternationalPhoneNumber());
            if (result.getWebsite() != null) restaurant.setWebsite(result.getWebsite());
            restaurant.setUserList(new ArrayList<>());
            return restaurant;
        }
        return null;
    }

    public String getPhoto(String photoReference) {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + BuildConfig.API_KEY;
    }
}
