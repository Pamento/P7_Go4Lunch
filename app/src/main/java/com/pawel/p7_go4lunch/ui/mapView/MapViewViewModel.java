package com.pawel.p7_go4lunch.ui.mapView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseChosenRestaurants;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.googleApiPlaces.SingleRestaurant;
import com.pawel.p7_go4lunch.utils.WasCalled;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class MapViewViewModel extends ViewModel {

    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseChosenRestaurants mFirebaseChosenRestaurants;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private MutableLiveData<GoogleMap> mGoogleMap;
    private List<Restaurant> mMiddleRestaurants = new ArrayList<>();

    private LatLng mCurrentLatLng;
    private String mCurrentLocation;
    private List<Restaurant> mRestaurants;

    public MapViewViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseChosenRestaurants firebaseChosenRestaurants) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseChosenRestaurants = firebaseChosenRestaurants;
    }

    public void init() {
        mGoogleMap = new MutableLiveData<>();
        if (mGooglePlaceRepository.getRestaurants().isEmpty()) {
            mRestaurants = null;
        } else {
            mRestaurants = mGooglePlaceRepository.getRestaurants();
        }
    }

    // ................................................................. GETTERS
    public List<Restaurant> getRestaurants(int radius, String key) {
        if (mCurrentLocation != null && mGooglePlaceRepository.getRestaurants().isEmpty()) {
            fetchRestaurants(mCurrentLocation, radius, key);
        }
        return mRestaurants;
    }

    public LatLng getLatLng() {
        return mCurrentLatLng;
    }

    public String getCurrentLocation() {
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
        mCurrentLatLng = latLng;
        if (!WasCalled.isLocationWasCalled()) {
            mGooglePlaceRepository.setInitialLatLng(latLng);
        }
    }

    public void setUpCurrentLocation(String currentLocation) {
        mGooglePlaceRepository.setCurrentLocation(currentLocation);
        mCurrentLocation = currentLocation;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        mGoogleMap.setValue(googleMap);
    }

    public void fetchRestaurants(String location, int radius, String key) {
        if (mGooglePlaceRepository.getRestaurants().isEmpty()) {
            mGooglePlaceRepository.streamCombinePlacesAndDetails(location, radius, key)
                    .subscribe(restaurantsRequestObserver(key));
        }
    }

    // ................................................................. UTILS FUNCTIONS
    private Observer<List<SingleRestaurant>> restaurantsRequestObserver(String key) {
        return new Observer<List<SingleRestaurant>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable.add(d);
            }

            @Override
            public void onNext(@NonNull List<SingleRestaurant> singleRestaurants) {
                for (SingleRestaurant sR : singleRestaurants) {
                    Restaurant rst = mGooglePlaceRepository.createRestaurant(sR.getResult(), key);
                    mMiddleRestaurants.add(rst);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                // TODO send data to complainant
            }
        };
    }

    public void disposeDisposable() {
        if (mDisposable != null && mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}