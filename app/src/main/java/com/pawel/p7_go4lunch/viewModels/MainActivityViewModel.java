package com.pawel.p7_go4lunch.viewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.model.FavoritesRestaurants;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;

import java.util.List;

public class MainActivityViewModel extends ViewModel {

    private final FirebaseUserRepository mFirebaseUserRepository;
    private final GooglePlaceRepository mGooglePlaceRepository;

    public void init() {
    }

    public MainActivityViewModel(FirebaseUserRepository firebaseUserRepository, GooglePlaceRepository googlePlaceRepository) {
        mFirebaseUserRepository = firebaseUserRepository;
        mGooglePlaceRepository = googlePlaceRepository;
    }

    public void createUser(String uri, String name, String email, String urlImage) {
        mFirebaseUserRepository.createUser(uri, name, email, urlImage).addOnFailureListener(this.onFailureListener());
    }

//    public void sendAutocompleteReq() {
//        mGooglePlaceRepository.
//    }

    public void setAutoSearchEventStatus(AutoSearchEvents eventStatus) {
        mGooglePlaceRepository.setAutoSearchEvents(eventStatus);
    }
    protected OnFailureListener onFailureListener() {
        return e -> Log.e("CREATE_USER", "onFailure: ", e);
    }
}
