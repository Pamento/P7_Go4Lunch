package com.pawel.p7_go4lunch.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.pawel.p7_go4lunch.AboutRestaurantActivity;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseChosenRestaurants;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.ui.listView.ListViewViewModel;
import com.pawel.p7_go4lunch.ui.mapView.MapViewViewModel;
import com.pawel.p7_go4lunch.ui.workmates.WorkmatesViewModel;

import java.lang.reflect.InvocationTargetException;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final GooglePlaceRepository mGooglePlaceRepository;
    private final FirebaseUserRepository mFirebaseUserRepository;
    private final FirebaseChosenRestaurants mFirebaseChosenRestaurants;

    public ViewModelFactory(GooglePlaceRepository googlePlaceRepository, FirebaseUserRepository firebaseUserRepository, FirebaseChosenRestaurants firebaseChosenRestaurants) {
        mGooglePlaceRepository = googlePlaceRepository;
        mFirebaseUserRepository = firebaseUserRepository;
        mFirebaseChosenRestaurants = firebaseChosenRestaurants;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
            return (T) new MainActivityViewModel(mFirebaseUserRepository);
        }
        if (modelClass.isAssignableFrom(MapViewViewModel.class)) {
            return (T) new MapViewViewModel(mGooglePlaceRepository, mFirebaseChosenRestaurants);
        }
        if (modelClass.isAssignableFrom(ListViewViewModel.class)) {
            return (T) new ListViewViewModel(mGooglePlaceRepository, mFirebaseChosenRestaurants);
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
