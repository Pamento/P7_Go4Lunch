package com.pawel.p7_go4lunch.viewModels;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;

public class AboutRestaurantViewModel extends ViewModel {

    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseUserRepository mFirebaseUserRepository;

    public AboutRestaurantViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseUserRepository firebaseUserRepository) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseUserRepository = firebaseUserRepository;
    }

    public void init() {
    }

    public Query getAllUsersFromCollection() {
        return mFirebaseUserRepository.getAllUsersFromCollection();
    }

    public Query getSelectedUsersFromCollection(String restaurantName) {
        return mFirebaseUserRepository.getSelectedUsersFromCollection(restaurantName);
    }
}
