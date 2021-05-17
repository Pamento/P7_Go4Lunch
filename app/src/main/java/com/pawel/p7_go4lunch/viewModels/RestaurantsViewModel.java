package com.pawel.p7_go4lunch.viewModels;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
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
import java.util.Iterator;
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
    private GooglePlaceRepository mGooglePlaceRepository = GooglePlaceRepository.getInstance();
    private final FirebaseUserRepository mFirebaseUserRepository;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private GoogleMap mGoogleMap;
    private final MutableLiveData<Location> mCurrentLocation = new MutableLiveData<>();
    private String mCurrentLocS;
    private int mRadius = 500;
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
                        mGooglePlaceRepository.findRestoForUpdates(result, true);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("ERROR:", "API Google Place Nearby search ERROR: ", e);
                    }

                    @Override
                    public void onComplete() {
                        mGooglePlaceRepository.setRestaurantLiveData(true);
                        Log.i("SEARCH", "RestaurantsVM.onComplete");
                    }
                });
    }

    public void getRestosFromCacheOrNetwork(AutoSearchEvents events) {
        Log.i(TAG, "RVM__ XXXXXXXXXXXXX.getRestosFromCacheOrNetwork: autoEvent " + events);
        Log.i(TAG, "RVM__ XXXXXXXXXXXXX.getRestosFromCacheOrNetwork: mRestaurants.size( " + mRestaurants.size() + " )");
        mCache.getRestos().subscribe(new Observer<List<Restaurant>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable.add(d);
            }

            @Override
            public void onNext(@NonNull List<Restaurant> restaurants) {
                Log.i(TAG, "onNext: restaurants param::: " + restaurants.size());
                Log.i(TAG, "onNext: mRestaurants param::: " + mRestaurants.size());
                mRestaurants = restaurants;
                if (mRestaurants.size() == 0) Log.i(TAG, "onNext_???_ size(): ZERO");
                else {
                    Log.i(TAG, "onNext: __???__resto:::" + mRestaurants.get(0).getName());
                    Log.i(TAG, "onNext_???_ size(): resto.size(" + mRestaurants.size());
                }
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
            }
        });
    }

    public LiveData<List<Restaurant>> getRestaurantWithUsers = Transformations.map(mGooglePlaceRepository.getRestaurants(), input -> {
        List<Restaurant> tempL = new ArrayList<>();
        Log.i(TAG, "RVM__ getRestaurants: _in: TRANSFORMATIONS.MAP():  usersGoingToChosenResto.size()::n°: " + usersGoingToChosenResto.size());
        Log.i(TAG, "RVM__ getRestaurants: _in: TRANSFORMATIONS.MAP().input::n°: " + input.size());
        if (input.size() > 0) {
            //Log.i(TAG, "RVM__ getRestaurants: _in: TRANSFORMATIONS.MAP().input.get(0)::: " + input.get(0).toString());
            int itr = input.size();
            for (int i = 0; i < itr; i++) {
                Restaurant r = input.get(i);
                List<String> ids = getRestoIdsFromUsers(r.getPlaceId());
                if (ids.size() > 0) {
                    Log.i(TAG, "RVM__ getRestaurants: _in: TRANSFORMATIONS.MAP() _in: for -> if ( ids > 0 ) " + ids.size());
                    r.setUserList(ids);
                }
                tempL.add(r);
            }
        }
        Log.i(TAG, "RVM__ getRestaurants: _in: TRANSFORMATIONS.MAP().tempL.size()::n°: " + tempL.size());
        return tempL;
    });

    // This method get all users whom has chosen already his restaurant for lunch
    public void getUsersWithChosenRestaurant() {
        mFirebaseUserRepository.getUsersWithChosenRestaurant().get().addOnSuccessListener(queryDocumentSnapshots -> {
            String email = "";
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null) email = fUser.getEmail();
            usersGoingToChosenResto = queryDocumentSnapshots.toObjects(User.class);
            for (Iterator<User> itr = usersGoingToChosenResto.iterator(); itr.hasNext(); ) {
                User user = itr.next();
                if (email != null && email.equals(user.getEmail())) itr.remove();
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

    private List<String> getRestoIdsFromUsers(String placeId) {
        int sizeL = usersGoingToChosenResto.size();
        List<String> ids = new ArrayList<>();
        if (sizeL > 0) {
            for (int i = 0; i < sizeL; i++) {
                User us = usersGoingToChosenResto.get(i);
                if (us.getUserRestaurant() != null && us.getUserRestaurant().getPlaceId().equals(placeId)) {
                    ids.add(us.getUserRestaurant().getPlaceId());
                }
            }
        }
        return ids;
    }

    public void disposeDisposable() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mGooglePlaceRepository.disposeDisposable();
    }
}