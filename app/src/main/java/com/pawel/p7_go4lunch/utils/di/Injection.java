package com.pawel.p7_go4lunch.utils.di;

import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseChosenRestaurants;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.viewModels.ViewModelFactory;

public class Injection {

    public static GooglePlaceRepository sGooglePlaceRepository() {
        return GooglePlaceRepository.getInstance();
    }

    public static FirebaseUserRepository sFirebaseUserRepository() {
        return FirebaseUserRepository.getInstance();
    }

    public static FirebaseChosenRestaurants sFirebaseChosenRestaurants() {
        return FirebaseChosenRestaurants.getInstance();
    }

    public static ViewModelFactory sViewModelFactory() {
        return new ViewModelFactory(sGooglePlaceRepository(),sFirebaseUserRepository(),sFirebaseChosenRestaurants());
    }
}
