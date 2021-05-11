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
    private MutableLiveData<GoogleMap> mGoogleMap;
    private final MutableLiveData<Location> mCurrentLocation = new MutableLiveData<>();
    private String mCurrentLocS;
    //private List<Restaurant> mRestaurants = new ArrayList<>();
    private List<User> usersGoingToChosenResto = new ArrayList<>();

    public RestaurantsViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseUserRepository firebaseUserRepository) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseUserRepository = firebaseUserRepository;
    }

    public void init() {
        mGoogleMap = new MutableLiveData<>();
//        if (!mGooglePlaceRepository.getRestaurantsCache().isEmpty()) {
//            mRestaurants = mGooglePlaceRepository.getRestaurantsCache();
//        }
        getUsersWithChosenRestaurant();
    }

    // ................................................................. GETTERS
    public void streamCombinedNearbyAndDetailPlace(String location, int radius) {
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
                        mGooglePlaceRepository.setRestaurantLiveData();
//                        updateUserRestaurants();
                        Log.i("SEARCH", "RestaurantsVM.onComplete");
                    }
                });
    }


    public MutableLiveData<List<Restaurant>> getRestaurants() {
        Log.i(TAG, "RestaurantsVM.getRestaurants: ");
        return mGooglePlaceRepository.getRestaurants();
    }

    public void getUsersWithChosenRestaurant() {
        mFirebaseUserRepository.getUsersWithChosenRestaurant().get().addOnSuccessListener(queryDocumentSnapshots -> {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            //List<User> tem = queryDocumentSnapshots.toObjects(User.class);
            usersGoingToChosenResto = queryDocumentSnapshots.toObjects(User.class);
            for (int i = 0; i < usersGoingToChosenResto.size(); i++) {
                if (u != null && usersGoingToChosenResto.get(i).getEmail().equals(u.getEmail()))
                    usersGoingToChosenResto.remove(i);
                //usersRestaurants.add(tem.get(i));
            }
        });
    }

    public LiveData<Location> getCurrentLocation() {
        return mCurrentLocation;
    }

    public LatLng getInitialLatLng() {
        return mGooglePlaceRepository.getInitialLatLng();
    }

    public LiveData<GoogleMap> getGoogleMap() {
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

    public void setUpCurrentLocation(Location currentLocation, LatLng ll) {
        mGooglePlaceRepository.setCurrentLocation(currentLocation);
        mCurrentLocation.setValue(currentLocation);
        mCurrentLocS = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        if (ll != null) setUpCurrentLatLng(ll);
    }

    public void setGoogleMap(GoogleMap googleMap) {
        mGoogleMap.setValue(googleMap);
    }

    public void fetchRestaurants(int radius) {
        Log.i(TAG, "fFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFetchRestaurants: " + mCurrentLocS);
        streamCombinedNearbyAndDetailPlace(mCurrentLocS, radius);
        getUsersWithChosenRestaurant();
    }

    // TODO !!!! Is this function is set for Autocomplete ?
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