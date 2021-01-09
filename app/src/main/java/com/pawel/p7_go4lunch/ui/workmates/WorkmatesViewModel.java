package com.pawel.p7_go4lunch.ui.workmates;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;

public class WorkmatesViewModel extends ViewModel {

    private FirebaseUserRepository mFirebaseUserRepository;
    public void init() {
        mFirebaseUserRepository = FirebaseUserRepository.getInstance();
    }

    public Query getAllUsersFromCollection() {
        return mFirebaseUserRepository.getAllUsersFromCollection();
    }
}