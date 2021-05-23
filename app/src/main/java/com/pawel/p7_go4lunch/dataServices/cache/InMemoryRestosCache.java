package com.pawel.p7_go4lunch.dataServices.cache;

import android.util.Log;

import com.pawel.p7_go4lunch.model.Restaurant;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class InMemoryRestosCache implements Cloneable {

    private static final String TAG = "AUTO_COM";
    private static volatile InMemoryRestosCache instance;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    //private final List<String> mRestosID = new ArrayList<>();
    private Location mLocation;

    private InMemoryRestosCache() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of InMemoryRestosCache class.");
        }
    }

    public static InMemoryRestosCache getInstance() {
        if (instance == null) {
            synchronized (InMemoryRestosCache.class) {
                if (instance == null) instance = new InMemoryRestosCache();
            }
        }
        return instance;
    }

    public Observable<List<Restaurant>> getRestos() {
        return Observable.create(emitter -> {
            emitter.onNext(mRestaurants);
            emitter.onComplete();
        });
    }

    public void cacheRestoInMemory(List<Restaurant> restosCache) {
        Log.i(TAG, "cacheRestoInMemory: HOW MANY ? ::: " + restosCache.size());
        if (restosCache.size() > 0) {
            mRestaurants = restosCache;
        }
    }

//    public void setRestosID(String placeID) {
//        Log.i(TAG, "CACHE__ setRestosID: " + placeID);
//        int ii = mRestaurants.size();
//        for (int i = 0; i < ii; i++) {
//            String id = mRestaurants.get(i).getPlaceId();
//            if (!mRestosID.contains(id)) mRestosID.add(id);
//        }
//    }

    // location
    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    // clear
//    public void clearRestosCache() {
//        mRestaurants.clear();
//        mRestosID.clear();
//    }

}
