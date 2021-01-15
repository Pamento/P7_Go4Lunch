package com.pawel.p7_go4lunch.viewModels;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;

import java.util.ArrayList;
import java.util.List;

public class AboutRestaurantViewModel extends ViewModel {

    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseUserRepository mFirebaseUserRepository;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private FirebaseUser mFirebaseUser = null;
    private User mUser = null;

    public AboutRestaurantViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseUserRepository firebaseUserRepository) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseUserRepository = firebaseUserRepository;
    }

    public void init() {
        mRestaurants = mGooglePlaceRepository.getRestaurants();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) getUserFromFirestore(mFirebaseUser.getUid());
    }

    // .......................................................... GETTERS
    public Query getAllUsersFromCollection() {
        return mFirebaseUserRepository.getAllUsersFromCollection();
    }

    public void getUserFromFirestore(String uid) {
        mFirebaseUserRepository.getUser(uid)
                .addOnSuccessListener(documentSnapshot -> mUser = documentSnapshot.toObject(User.class));
    }

    public Query getSelectedUsersFromCollection(String restaurantName) {
        return mFirebaseUserRepository.getSelectedUsersFromCollection(restaurantName);
    }

    public List<Restaurant> getRestaurants() {
        if (mRestaurants.isEmpty()) {
            this.mRestaurants = mGooglePlaceRepository.getRestaurants();
        }
        return mRestaurants;
    }

    public Restaurant getRestaurant(String placeID) {
        if (mRestaurants.isEmpty()) init();
        Restaurant restaurant = null;
        for (Restaurant rst: mRestaurants) {
            if (rst.getPlaceId().equals(placeID)) {
                restaurant = rst;
            }
        }
        return restaurant;
    }

    public FirebaseUser getFirebaseUser() {
        return mFirebaseUser;
    }

    public User getUser() {
        return mUser;
    }

    // .......................................................... UPDATES

    public void updateUserRestaurant(String uid, Restaurant restaurant) {
        mFirebaseUserRepository.updateUserRestaurant(uid, restaurant);
    }

    public void updateUserFavoriteRestaurantsList(String uid, List<String> favoriteRestaurants) {
        mFirebaseUserRepository.updateUserFavoritesRestaurant(uid, favoriteRestaurants);
    }

    // .......................................................... UTILS
}
