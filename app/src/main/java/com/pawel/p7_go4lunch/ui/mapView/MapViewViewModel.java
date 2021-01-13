package com.pawel.p7_go4lunch.ui.mapView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseChosenRestaurants;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;


public class MapViewViewModel extends ViewModel {

    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseChosenRestaurants mFirebaseChosenRestaurants;

    private MutableLiveData<LatLng> mCurrentLocation;
    private MutableLiveData<GoogleMap> mGoogleMap;

    public MapViewViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseChosenRestaurants firebaseChosenRestaurants) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseChosenRestaurants = firebaseChosenRestaurants;
    }

    public void init() {
        mCurrentLocation = new MutableLiveData<>();
        mGoogleMap = new MutableLiveData<>();
    }

    public LiveData<LatLng> getLatLng() { return mCurrentLocation; }
    public LiveData<GoogleMap> getGoogleMap() { return mGoogleMap; }

    // Set Data
    public void setUpCurrentLocation(LatLng latLng) {
        mCurrentLocation.setValue(latLng);
    }
    public void setGoogleMap(GoogleMap googleMap) {
        mGoogleMap.setValue(googleMap);
    }

}