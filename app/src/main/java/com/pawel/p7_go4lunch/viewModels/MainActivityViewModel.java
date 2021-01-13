package com.pawel.p7_go4lunch.viewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;

public class MainActivityViewModel extends ViewModel {

    private final FirebaseUserRepository mFirebaseUserRepository;
    public void init() {
        //mFirebaseUserRepository = FirebaseUserRepository.getInstance();
    }

    public MainActivityViewModel(FirebaseUserRepository firebaseUserRepository) {
        mFirebaseUserRepository = firebaseUserRepository;
    }

    public void createUser(String uri, String name, String email, String urlImage) {
        mFirebaseUserRepository.createUser(uri,name,email,urlImage).addOnFailureListener(this.onFailureListener());
    }

    protected OnFailureListener onFailureListener(){
        return e -> Log.e("TESTING_MAPS", "onFailure: ",e );
    }
}
