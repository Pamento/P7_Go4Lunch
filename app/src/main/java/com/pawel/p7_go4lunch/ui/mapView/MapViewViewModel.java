package com.pawel.p7_go4lunch.ui.mapView;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseChosenRestaurants;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.WasCalled;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class MapViewViewModel extends ViewModel {

    private static final String TAG = "SEARCH";
    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseChosenRestaurants mFirebaseChosenRestaurants;
    private final FirebaseUserRepository mFirebaseUserRepository;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private MutableLiveData<GoogleMap> mGoogleMap;
    private final MutableLiveData<Location> mCurrentLocation = new MutableLiveData<>();
    private String mCurrentLocS;
    //private MutableLiveData<List<Restaurant>> mRestaurants = new MutableLiveData<>();

    public MapViewViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseChosenRestaurants firebaseChosenRestaurants, FirebaseUserRepository firebaseUserRepository) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseChosenRestaurants = firebaseChosenRestaurants;
        mFirebaseUserRepository = firebaseUserRepository;
    }

    public void init() {
        mGoogleMap = new MutableLiveData<>();
        //getUsersWithChosenRestaurant();
//        if (mGooglePlaceRepository.getRestaurants().isEmpty()) {
//            mRestaurants = new MutableLiveData<>();
//        } else {
//            mRestaurants.setValue(mGooglePlaceRepository.getRestaurants());
//        }
    }

    // ................................................................. GETTERS
    public MutableLiveData<List<Restaurant>> getRestaurants() {
        return mGooglePlaceRepository.getRestaurants();
    }

    public List<Restaurant> getRestaurantsCache() {
        return mGooglePlaceRepository.getRestaurantsCache();
    }

    public void getUsersWithChosenRestaurant() {
        mFirebaseUserRepository.getUsersWithChosenRestaurant().get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.i(TAG, "onSuccess: " + queryDocumentSnapshots.size());
                List<User> users = queryDocumentSnapshots.toObjects(User.class);
                for (int i = 0; i < users.size(); i++) {
                    Log.i(TAG, "onSuccess: " + users.get(i).toString());
                }
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
        mGooglePlaceRepository.streamCombinedNearbyAndDetailPlace(mCurrentLocS, radius);
    }

//    public void fetchRestaurant(int radius) {
//        Log.i(TAG, "fetchRRRRRRRRRRRRRRRRRRRRRRRRRRRR: " + mCurrentLocS);
//        if (mGooglePlaceRepository.getRestaurants().isEmpty())
//            mGooglePlaceRepository.streamFetchRestaurantsPlaces(mCurrentLocS, radius)
//                    .subscribe(new Observer<RestaurantResult>() {
//                        @Override
//                        public void onSubscribe(@NonNull Disposable d) {
//                            mDisposable.add(d);
//                        }
//
//                        @Override
//                        public void onNext(@NonNull RestaurantResult restaurantResult) {
//                            List<Restaurant> lr = new ArrayList<>();
//                            List<Result> res = restaurantResult.getResults();
//                            for (Result rs : res) {
//                                Restaurant rst = mGooglePlaceRepository.createRestaurant(rs);
//                                lr.add(rst);
//                            }
//                            mRestaurants.setValue(lr);
//                        }
//
//                        @Override
//                        public void onError(@NonNull Throwable e) {
//                            Log.e(TAG, "onError: fetchRestaurants :: ", e);
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
//    }

//    public void fetchRestaurantsAndDetail(int radius, String key) {
//        Log.i(TAG, "fetchRRRRRRRRRRRRRRRRRRRRRRRRRRRR: "+mCurrentLocS);
//        if (mGooglePlaceRepository.getRestaurants().isEmpty()) {
//            mGooglePlaceRepository.streamCombinePlacesAndDetails(mCurrentLocS, radius, key)
//                    .subscribe(restaurantsRequestObserver(key));
//        }
//    }

    // ................................................................. UTILS FUNCTIONS
//    private Observer<List<SingleRestaurant>> restaurantsRequestObserver(String key) {
//        return new Observer<List<SingleRestaurant>>() {
//            @Override
//            public void onSubscribe(@NonNull Disposable d) {
//                Log.i(TAG, "____restaurantsRequestObserver: ___onSubscribe: ___disposable: "+d);
//                mDisposable.add(d);
//            }
//
//            @Override
//            public void onNext(@NonNull List<SingleRestaurant> singleRestaurants) {
//                for (SingleRestaurant sR : singleRestaurants) {
//                    Restaurant rst = mGooglePlaceRepository.createRestaurant(sR.getResult());
//                    Log.i(TAG, "onNext: " + rst);
//                    mRestaurants.add(rst);
//                    Log.i(TAG, "onNext: ...mRestaurants.size(" + mRestaurants.size() + ")");
//                }
//            }
//
//            @Override
//            public void onError(@NonNull Throwable e) {
//                Log.e(TAG, "onError:(restaurantsRequestObserver) ", e);
//            }
//
//            @SuppressWarnings("unchecked")
//            @Override
//            public void onComplete() {
//                // TODO send data to complainant
//                if (mGooglePlaceRepository.getRestaurants() != null) {
//                    List<Restaurant> r = mGooglePlaceRepository.getRestaurants();
//                    setMiddleRestaurants(r);
//                } else {
//                    mGooglePlaceRepository.setRestaurants(mRestaurants);
//                }
//            }
//        };
//    }

    public void disposeDisposable() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mGooglePlaceRepository.disposeDisposable();
    }
}