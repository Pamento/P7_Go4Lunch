package com.pawel.p7_go4lunch.viewModels;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;

public class WorkmatesViewModel extends ViewModel {

    private final FirebaseUserRepository mFirebaseUserRepository;

    public WorkmatesViewModel(FirebaseUserRepository firebaseUserRepository) {
        mFirebaseUserRepository = firebaseUserRepository;
    }

    public void init() {
    }

    public Query getAllUsersFromCollection(String userId) {
        return mFirebaseUserRepository.getAllUsersFromCollection(userId);
    }
}