package com.pawel.p7_go4lunch.viewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;

public class MainActivityViewModel extends ViewModel {
    private static final String TAG = "TESTING_MAPS";

    private FirebaseUserRepository mFirebaseUserRepository;
    public void init() {
        mFirebaseUserRepository = FirebaseUserRepository.getInstance();
    }

    public void createUser(String uri, String name, String email, String urlImage) {
        mFirebaseUserRepository.createUser(uri,name,email,urlImage).addOnFailureListener(this.onFailureListener());
    }

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ",e );
            }
        };
    }
}
