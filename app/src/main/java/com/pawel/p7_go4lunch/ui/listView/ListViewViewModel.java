package com.pawel.p7_go4lunch.ui.listView;

import androidx.lifecycle.ViewModel;

import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseChosenRestaurants;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;

public class ListViewViewModel extends ViewModel {

    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseChosenRestaurants mFirebaseChosenRestaurants;

    public ListViewViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseChosenRestaurants firebaseChosenRestaurants) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseChosenRestaurants = firebaseChosenRestaurants;
    }

    public void init() {
    }
}