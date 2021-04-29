package com.pawel.p7_go4lunch.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private List<Restaurant> mRestaurantsList = new ArrayList<>();
    private final MutableLiveData<Restaurant> mRestaurant = new MutableLiveData<>();
    private final MutableLiveData<User> mUser = new MutableLiveData<>();

    public AboutRestaurantViewModel(GooglePlaceRepository googlePlaceRepository, FirebaseUserRepository firebaseUserRepository) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseUserRepository = firebaseUserRepository;
    }

    public void init() {
        mRestaurantsList = mGooglePlaceRepository.getRestaurantsCache();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) getUserFromFirestore(firebaseUser.getUid());
    }

    // .......................................................... GETTERS
    public void getUserFromFirestore(String uid) {
        mFirebaseUserRepository.getUser(uid)
                .addOnSuccessListener(documentSnapshot -> mUser.setValue(documentSnapshot.toObject(User.class)));
    }

    public Query getUsersWithTheSameRestaurant(String restoId) {
        return mFirebaseUserRepository.getUsersWithTheSameRestaurant(restoId);
    }

    public void getRestaurantsList() {
        if (mRestaurantsList.isEmpty()) {
            this.mRestaurantsList = mGooglePlaceRepository.getRestaurantsCache();
        }
    }

    public LiveData<Restaurant> getRestaurant(String placeID) {
        if (mRestaurantsList.isEmpty()) getRestaurantsList();
        for (Restaurant rst: mRestaurantsList) {
            if (rst.getPlaceId().equals(placeID)) {
                mRestaurant.setValue(rst);
            }
        }
        return mRestaurant;
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    // .......................................................... UPDATES
    public void updateUserRestaurant(String uid, Restaurant restaurant) {
        mFirebaseUserRepository.updateUserRestaurant(uid, restaurant);
    }

    public void updateUserFavoriteRestaurantsList(String uid, List<String> favoriteRestaurants) {
        mFirebaseUserRepository.updateUserFavoritesRestaurant(uid, favoriteRestaurants);
    }
}
