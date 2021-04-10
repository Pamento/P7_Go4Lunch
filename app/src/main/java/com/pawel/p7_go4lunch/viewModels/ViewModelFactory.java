package com.pawel.p7_go4lunch.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseUserRepository mFirebaseUserRepository;

    public ViewModelFactory(GooglePlaceRepository googlePlaceRepository, FirebaseUserRepository firebaseUserRepository) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseUserRepository = firebaseUserRepository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
            return (T) new MainActivityViewModel(mFirebaseUserRepository);
        }
        if (modelClass.isAssignableFrom(RestaurantsViewModel.class)) {
            return (T) new RestaurantsViewModel(mGooglePlaceRepository, mFirebaseUserRepository);
        }
        if (modelClass.isAssignableFrom(WorkmatesViewModel.class)) {
            return (T) new WorkmatesViewModel(mFirebaseUserRepository);
        }
        if (modelClass.isAssignableFrom(AboutRestaurantViewModel.class)) {
            return (T) new AboutRestaurantViewModel(mGooglePlaceRepository, mFirebaseUserRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
