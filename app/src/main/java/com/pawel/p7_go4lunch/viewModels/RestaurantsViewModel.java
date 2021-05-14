package com.pawel.p7_go4lunch.viewModels;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pawel.p7_go4lunch.dataServices.cache.InMemoryRestosCache;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;
import com.pawel.p7_go4lunch.utils.WasCalled;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RestaurantsViewModel extends ViewModel {

    private static final String TAG = "AUTO_COM";
    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseUserRepository mFirebaseUserRepository;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private GoogleMap mGoogleMap;
    private final MutableLiveData<Location> mCurrentLocation = new MutableLiveData<>();
    private String mCurrentLocS;
    private int mRadius = 500;

    // TODO cache:: add line below
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private InMemoryRestosCache mCache;
    //
    private List<User> usersGoingToChosenResto = new ArrayList<>();

    public RestaurantsViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseUserRepository firebaseUserRepository) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseUserRepository = firebaseUserRepository;
    }

    public void init() {
        getUsersWithChosenRestaurant();
        mCache = InMemoryRestosCache.getInstance();
        Log.i(TAG, "RVM__ init: CACHE.Restaurants.IDs::: " + mCache.getRestosID().size());
    }

    // ................................................................. GETTERS
    public void streamCombinedNearbyAndDetailPlace(String location, int radius) {
        Log.i(TAG, "RVM__ streamCombinedNearbyAndDetailPlace: self_CAll");
        mGooglePlaceRepository.getRestaurantNearby(location, radius)
                .subscribeOn(Schedulers.io())
                .concatMap((Function<Result, ObservableSource<Result>>) result -> mGooglePlaceRepository.getRestaurantContact(result.getPlaceId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull Result result) {
                        // TODO cache:: this work must by done here in VM ?
                        // NOT, because the GoogleRepo.getRestaurantsNearBy.setRestaurantsNearby(results);
                        // TODO cache::.
                        mGooglePlaceRepository.findRestoForUpdates(result, true);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("ERROR:", "API Google Place Nearby search ERROR: ", e);
                    }

                    @Override
                    public void onComplete() {
                        // TODO cache:: on complete set RestaurantsViewModel.this.mRestaurants
                        // TODO cache:: mCache.saveInCache( mRestaurants );
                        mGooglePlaceRepository.setRestaurantLiveData(null);
                        // TODO cache:: updateUserRestaurants(); should by added ind Transformations.map in
                        //  RestoVM.GetRestaurants from GoogleRepo
//                        updateUserRestaurants();
                        Log.i("SEARCH", "RestaurantsVM.onComplete");
                    }
                });
    }

    public void getRestosFromCacheOrNetwork(AutoSearchEvents events) {
        Log.i(TAG, "RVM__ XXXXXXXXXXXXX.getRestosFromCacheOrNetwork: autoEvent " + events);
        Log.i(TAG, "RVM__ XXXXXXXXXXXXX.getRestosFromCacheOrNetwork: mRestaurants.size( " + mRestaurants.size());
        mCache.getRestos().subscribe(new Observer<List<Restaurant>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable.add(d);
            }

            @Override
            public void onNext(@NonNull List<Restaurant> restaurants) {
                Log.i(TAG, "onNext: restaurants param::: " + restaurants.size());
                mRestaurants = restaurants;
                if (mRestaurants.size() == 0) Log.i(TAG, "onNext_???_ size(): ZERO");
                else Log.i(TAG, "onNext_???_ size(): resto.size(" + mRestaurants.size());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e(TAG, "RestaurantsViewModel.getRestosFromCacheOrNetwork.onError: ", e);
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "RVM__RestaurantsVM.getRestaurants.onComplete: ");
                if (mRestaurants.size() == 0) {
                    Log.i(TAG, "RVM__ onComplete: ");
                    Log.i(TAG, "RVM__ onComplete: mCurrentLocS::: " + mCurrentLocS);
                    Log.i(TAG, "RVM__ onComplete: mRadius::: " + mRadius);
                    streamCombinedNearbyAndDetailPlace(mCurrentLocS, mRadius);
                } else {
                    Log.i(TAG, "RVM__ onComplete: mGooglePlaceRepository.setRestaurantLiveData(mRestaurants)::: " + mRestaurants.size());
                    mGooglePlaceRepository.setRestaurantLiveData(mRestaurants);
                }
                Log.i(TAG, "RVM__ onComplete: BEFORE .clear() _" + mRestaurants.size());
                mRestaurants.clear();
                Log.i(TAG, "RVM__ onComplete: AFTER .clear() _" + mRestaurants.size());
            }
        });
    }


    // TODO cache:: modify in Transforms.switchMap ???
    public MutableLiveData<List<Restaurant>> getRestaurants() {

        // TODO cache:: in case when user go back from other activity we need decide here or elsewhere
        //  how we manage get back of restaurant from cache if is there or restart NearBy
        if (mRestaurants.size() == 0) {
            Log.i(TAG, "RVM__ getRestaurants");
            getRestosFromCacheOrNetwork(AutoSearchEvents.AUTO_NULL);
        }
        return mGooglePlaceRepository.getRestaurants();
    }

    // This method get all users whom has chosen already his restaurant for lunch
    public void getUsersWithChosenRestaurant() {
        mFirebaseUserRepository.getUsersWithChosenRestaurant().get().addOnSuccessListener(queryDocumentSnapshots -> {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            usersGoingToChosenResto = queryDocumentSnapshots.toObjects(User.class);
            for (int i = 0; i < usersGoingToChosenResto.size(); i++) {
                if (u != null && usersGoingToChosenResto.get(i).getEmail().equals(u.getEmail()))
                    usersGoingToChosenResto.remove(i);
            }
        });
    }

    public LiveData<Location> getCurrentLocation() {
        return mCurrentLocation;
    }

    public LatLng getInitialLatLng() {
        return mGooglePlaceRepository.getInitialLatLng();
    }

    public GoogleMap getGoogleMap() {
        return mGoogleMap;
    }

    public LiveData<AutoSearchEvents> getAutoSearchEvent() {
        return mGooglePlaceRepository.getAutoSearchEvents();
    }

    public LiveData<AutoSearchEvents> getAutoSearchEventList() {
        return mGooglePlaceRepository.getAutoSearchEvent();
    }

    // ................................................................. SETTERS
    public void setUpCurrentLatLng(LatLng latLng) {
        if (!WasCalled.isLocationWasCalled()) {
            mGooglePlaceRepository.setInitialLatLng(latLng);
        }
    }

    public void setUpCurrentLocation(Location currentLocation, LatLng ll, int radius) {
        mGooglePlaceRepository.setCurrentLocation(currentLocation);
        mCurrentLocation.setValue(currentLocation);
        mCurrentLocS = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        if (ll != null) setUpCurrentLatLng(ll);
        mRadius = radius;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    public void fetchRestaurants(int radius) {
        Log.i(TAG, "fFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFetchRestaurants: " + mCurrentLocS);
        streamCombinedNearbyAndDetailPlace(mCurrentLocS, radius);
        getUsersWithChosenRestaurant();
    }

    // TODO cache:: !!!! Is this function is set for Autocomplete ?
//    private void updateUserRestaurants() {
//        // TODO UpdateRestoWithUserRestarants with Transformations.map ?
//        int itr = mRestaurants.size();
//        for (int i = 0; i < itr; i++) {
//            Restaurant r = mRestaurants.get(i);
//            List<String> ids = getRestoIdsFromUsers(r.getPlaceId());
//            if (ids.size() > 0) {
//                r.setUserList(ids);
//                mRestaurants.set(i, r);
//            }
//        }
//    }

    private List<String> getRestoIdsFromUsers(String placeId) {
        int itr = usersGoingToChosenResto.size();
        List<String> id = new ArrayList<>();
        if (itr > 0) {
            for (int i = 0; i < itr; i++) {
                User us = usersGoingToChosenResto.get(i);
                if (us.getUserRestaurant() != null && us.getUserRestaurant().getPlaceId().equals(placeId)) {
                    id.add(us.getUserRestaurant().getPlaceId());
                }
            }
        }
        return id;
    }

    public void disposeDisposable() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mGooglePlaceRepository.disposeDisposable();
    }
}