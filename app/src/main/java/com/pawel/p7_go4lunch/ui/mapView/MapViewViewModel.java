package com.pawel.p7_go4lunch.ui.mapView;

import android.location.Location;

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
    private MutableLiveData<List<Restaurant>> mMiddleRestaurants = new MutableLiveData<>();

    private final MutableLiveData<Location> mCurrentLocation = new MutableLiveData<>();
    private String mCurrentLocS;
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
        if (mCurrentLocS != null && mGooglePlaceRepository.getRestaurants().isEmpty()) {
            fetchRestaurants(mCurrentLocS, radius, key);
        }
        return mRestaurants;
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

    public MutableLiveData<List<Restaurant>> getMiddleRestaurants() {
        return mMiddleRestaurants;
    }

    public void setMiddleRestaurants(MutableLiveData<List<Restaurant>> middleRestaurants) {
        mMiddleRestaurants = middleRestaurants;
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
                    mRestaurants.add(rst);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @SuppressWarnings("unchecked")
            @Override
            public void onComplete() {
                // TODO send data to complainant
                if (mGooglePlaceRepository.getRestaurants() != null) {
                    setMiddleRestaurants((MutableLiveData<List<Restaurant>>) mGooglePlaceRepository.getRestaurants());
                }
            }
        };
    }

    public void disposeDisposable() {
        if (mDisposable != null && mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}