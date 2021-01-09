package com.pawel.p7_go4lunch.viewModels;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;

public class AboutRestaurantViewModel extends ViewModel {

    private FirebaseUserRepository mFirebaseUserRepository;
    public void init() {
        mFirebaseUserRepository = FirebaseUserRepository.getInstance();
    }

    public Query getAllUsersFromCollection() {
        return mFirebaseUserRepository.getAllUsersFromCollection();
    }

    public Query getSelectedUsersFromCollection(String restaurantName) {
        return mFirebaseUserRepository.getSelectedUsersFromCollection(restaurantName);
    }
}
