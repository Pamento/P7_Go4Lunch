package com.pawel.p7_go4lunch.viewModels;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.pawel.p7_go4lunch.dataServices.cache.InMemoryRestosCache;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.dataServices.repositorys.GooglePlaceRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.utils.AutoSearchEvents;
import com.pawel.p7_go4lunch.utils.helpers.FilterRestaurants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityViewModel extends ViewModel {

    private final FirebaseUserRepository mFirebaseUserRepo;
    private final GooglePlaceRepository mGooglePlaceRepository;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private final InMemoryRestosCache mCache;

    public MainActivityViewModel(FirebaseUserRepository firebaseUserRepository, GooglePlaceRepository googlePlaceRepository) {
        mFirebaseUserRepo = firebaseUserRepository;
        mGooglePlaceRepository = googlePlaceRepository;
        mCache = InMemoryRestosCache.getInstance();
    }

    public void createUser(String uri, String name, String email, String urlImage) {
        mFirebaseUserRepo.createUser(uri, name, email, urlImage).addOnFailureListener(this.onFailureListener());
    }

    public void streamCombinedAutocompleteDetailsPlace(String input, String lang, int radius, String location, String origin) {
        mGooglePlaceRepository.getRestoAutocompleteBy(input, lang, radius, location, origin)
                .subscribeOn(Schedulers.io())
                .concatMap(predictions -> mGooglePlaceRepository.getRestaurantDetails(predictions.getPlaceId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        mDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Result result) {
                        mGooglePlaceRepository.findRestoForUpdates(result, false);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("ERROR", "API Google Place Autocomplete search ERROR:", e);
                    }

                    @Override
                    public void onComplete() {
                        mGooglePlaceRepository.setRestaurantLiveData(false);
                    }
                });
    }

    public String getCurrentLocation() {
        return mGooglePlaceRepository.getCurrentLocation();
    }

    public void setAutoSearchEventStatus(AutoSearchEvents eventStatus) {
        mGooglePlaceRepository.setAutoSearchEvents(eventStatus);
    }

    protected OnFailureListener onFailureListener() {
        return e -> Log.e("CREATE_USER", "onFailure: ", e);
    }

    public void filterRestaurantBy(int filterType) {
        mCache.getRestos().subscribe(new Observer<List<Restaurant>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable.add(d);
            }

            @Override
            public void onNext(@NonNull List<Restaurant> restaurants) {
                switch (filterType) {
                    case 1:
                        mRestaurants = FilterRestaurants.byAZ(restaurants);
                    case 2:
                    case 3:
                        mRestaurants = FilterRestaurants.byRating(restaurants, filterType);
                        break;
                    case 4:
                        mRestaurants = FilterRestaurants.byDistance(restaurants);
                        break;
                    default:
                        mRestaurants = restaurants;
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e("ERROR", "onError: at MainActivityViewModel Error: ", e);
            }

            @Override
            public void onComplete() {
                mGooglePlaceRepository.setRestaurantLiveData(mRestaurants);
            }
        });
    }

    public void disposeDisposable() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mGooglePlaceRepository.disposeDisposable();
    }
}
