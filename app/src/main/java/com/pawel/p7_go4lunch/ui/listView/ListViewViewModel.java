package com.pawel.p7_go4lunch.ui.listView;

import androidx.lifecycle.ViewModel;

import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;

public class ListViewViewModel extends ViewModel {

    private FirebaseUserRepository mFirebaseUserRepository;
    public void init() {
        mFirebaseUserRepository = FirebaseUserRepository.getInstance();
    }
}