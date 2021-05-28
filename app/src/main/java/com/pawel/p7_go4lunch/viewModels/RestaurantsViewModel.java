package com.pawel.p7_go4lunch.viewModels;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pawel.p7_go4lunch.dataServices.cache.InMemoryRestosCache;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;

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
    private final MediatorLiveData<List<Restaurant>> mRestaurantWithUsers;
    private List<Restaurant> tempRestos = new ArrayList<>();
    //
    private MutableLiveData<List<User>> usersGoingToChosenResto;

    public RestaurantsViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseUserRepository firebaseUserRepository) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseUserRepository = firebaseUserRepository;
        mRestaurantWithUsers = new MediatorLiveData<>();
    }

    public void init() {
        mCache = InMemoryRestosCache.getInstance();
        usersGoingToChosenResto = new MutableLiveData<>();
        mergeRestoWithUsers();
    }

    // ................................................................. GETTERS
    public void streamGetRestaurantNearbyAndDetail(String location, int radius) {
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
                    }
                });
    }

    public void getRestosFromCacheOrNetwork() {
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
                    streamGetRestaurantNearbyAndDetail(mCurrentLocS, mRadius);
                } else {
                    Log.i(TAG, "RVM__ onComplete: mGooglePlaceRepository.setRestaurantLiveData(mRestaurants)::: " + mRestaurants.size());
                    mGooglePlaceRepository.setRestaurantLiveData(mRestaurants);
                }
            }
        });
    }

    private void mergeRestoWithUsers() {
        Log.i(TAG, "RVM__ mergeRestoWithUsers: MEDIATOR-liveData");
        mRestaurantWithUsers.postValue(new ArrayList<>());

        mRestaurantWithUsers.addSource(mGooglePlaceRepository.getRestaurants(), restaurants -> {
            Log.i(TAG, "RVM__ mergeRestoWithUsers__ (MEDIATOR-LiveData)__ addSource::1: mGooglePlaceRepository.getRestaurants();");
            if (restaurants != null) {
                Log.i(TAG, "RVM__ mergeRestoWithUsers__ (MEDIATOR-LiveData)__ restaurants.size::: " + restaurants.size());
                Log.i(TAG, "RVM__ mergeRestoWithUsers__ (MEDIATOR-LiveData)__ tempRestos.size::: " + tempRestos.size());
                tempRestos = restaurants;
                getUsersWithChosenRestaurant();
            }
        });
        mRestaurantWithUsers.addSource(usersGoingToChosenResto, new androidx.lifecycle.Observer<List<User>>() {
            final List<Restaurant> tempL = new ArrayList<>();

            @Override
            public void onChanged(List<User> users) {
                Log.i(TAG, "RVM__ mergeRestoWithUsers__ (MEDIATOR-LiveData)__ addSource::2: usersGoingToChosenResto;");
                if (tempL.size() > 0) tempL.clear();
                if (users != null && users.size() > 0) {
                    if (tempRestos.size() > 0) {
                        int itr = tempRestos.size();
                        Log.i(TAG, "RVM__ (MEDIATOR-LiveData)__ onChanged: + tempRestos.size() " + itr);
                        Log.i(TAG, "RVM__ (MEDIATOR-LiveData)__ onChanged: + users.size() " + users.size());
                        for (int i = 0; i < itr; i++) {
                            Restaurant r = tempRestos.get(i);
                            List<String> ids = getRestoIdsFromUsers(r.getPlaceId(), users);
                            Log.i(TAG, "RVM__ (MEDIATOR-LiveData)__ onChanged: ids::: " + ids);
                            if (ids.size() > 0) {
                                r.setUserList(ids);
                            }
                            tempL.add(r);
                        }
                        Log.i(TAG, "RVM__ mergeRestoWithUsers__ (MEDIATOR-LiveData)__ onChanged: + tempL.size() " + tempL.size());
                        mRestaurantWithUsers.setValue(tempL);
                    }
                } else {
                    mRestaurantWithUsers.setValue(tempRestos);
                }
            }
        });
    }

    public void unsubscribeRestoWithUsers() {
        mRestaurantWithUsers.removeSource(mGooglePlaceRepository.getRestaurants());
        mRestaurantWithUsers.removeSource(usersGoingToChosenResto);
    }

    public LiveData<List<Restaurant>> getRestaurantWithUsers() {
        Log.i(TAG, "RVM__ getRestaurantWithUsers: call --> MEDIATOR-LiveData");
        return mRestaurantWithUsers;
    }

    // This method get all users whom has chosen already his restaurant for lunch
    public void getUsersWithChosenRestaurant() {
        mFirebaseUserRepository.getUsersWithChosenRestaurant().get().addOnSuccessListener(queryDocumentSnapshots -> {
            String email = "";
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null) email = fUser.getEmail();
            List<User> us = queryDocumentSnapshots.toObjects(User.class);
            for (Iterator<User> itr = us.iterator(); itr.hasNext(); ) {
                User user = itr.next();
                if (email != null && email.equals(user.getEmail())) itr.remove();
            }
            usersGoingToChosenResto.setValue(us);
        });
    }

    public void getRestosFromCache(int radius) {
        mCache.getRestos(radius).subscribe(new Observer<List<Restaurant>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable.add(d);
            }

            @Override
            public void onNext(@NonNull List<Restaurant> restaurants) {
                mRestaurants = restaurants;
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e("ERROR", "onError: get restaurants from cache: ", e);
            }

            @Override
            public void onComplete() {
                mGooglePlaceRepository.setRestaurantLiveData(mRestaurants);
            }
        });
    }

    public int getRadius() {
        return mRadius;
    }

    public String getCurrentLocStr() {
        return mCurrentLocS;
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
    public void setUpCurrentLocation(Location currentLocation, int radius) {
        mGooglePlaceRepository.setCurrentLocation(currentLocation);
        mCurrentLocation.setValue(currentLocation);
        mCurrentLocS = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        mRadius = radius;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    private List<String> getRestoIdsFromUsers(String placeId, List<User> users) {
        Log.i(TAG, "RVM__ getRestoIdsFromUsers: RUN");
        int sizeL = users.size();
        List<String> ids = new ArrayList<>();
        if (sizeL > 0) {
            for (int i = 0; i < sizeL; i++) {
                User us = users.get(i);
                if (us.getUserRestaurant() != null && us.getUserRestaurant().getPlaceId().equals(placeId)) {
                    Log.i(TAG, "RVM__ getRestoIdsFromUsers: user.resto.Id::: " + us.getUserRestaurant().getPlaceId() + " placeId::: " + placeId);
                    ids.add(us.getUserRestaurant().getPlaceId());
                }
            }
        }
        return ids;
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }

    public void disposeDisposable() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mGooglePlaceRepository.disposeDisposable();
    }
}