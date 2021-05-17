package com.pawel.p7_go4lunch.dataServices.cache;

import android.util.Log;

import com.pawel.p7_go4lunch.model.Restaurant;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class InMemoryRestosCache implements Cloneable {
    private static final String TAG = "AUTO_COM";
    // TODO cache:: implement check the validity of cache for case where user don't SwitchOff his phone.
    private static volatile InMemoryRestosCache instance;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private final List<String> mRestosID = new ArrayList<>();
    private Location mLocation;
    private Long mRestoTimeCreation;

    // Constructor
    private InMemoryRestosCache() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of InMemoryRestosCache class.");
        }
    }

    // instance
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

    // cache restos
//    public void cacheRestoInMemory(List<Restaurant> restosCache) {
//        Log.i("AUTO_COM", "cacheRestoInMemory: HOW MANY ? ::: " + restosCache.size());
//        if (restosCache.size() > 0) {
//            Log.i("AUTO_COM", "CACHE.InMemoryCache.cacheRestoInMemory: ");
//            try {
//                Restaurant clone = restosCache.get(0).clone();
//                Log.i(TAG, "cacheRestoInMemory: 1_resto::: " + clone.toString());
//            } catch (CloneNotSupportedException e) {
//                Log.e("CACHE:", "CloneNotSupportedException: ", e);
//                return;
//            }
//            int ii = restosCache.size();
//            for (int i=0; i<ii; i++) {
//                try {
//                    mRestaurants.add(restosCache.get(i).clone());
//                    setRestosID(restosCache.get(i).getPlaceId());
//                    Log.i(TAG, "CACHE__ cacheRestoInMemory: mRestaurants.add["+i+"]");
//                } catch (CloneNotSupportedException e) {
//                    Log.e("CACHE:", "CloneNotSupportedException: ", e);
//                    return;
//                }
////            if (i == (ii - 1)) {
////                setRestosID();
////            }
//            }
//            //this.mRestaurants = restosCache;
//        } else Log.i("AUTO_COM", "CACHE.InMemoryCache.cacheRestoInMemory: mResto is empty");
//        this.mRestoTimeCreation = System.currentTimeMillis();
//    }

    public void cacheRestoInMemory(List<Restaurant> restosCache) {
        Log.i("AUTO_COM", "cacheRestoInMemory: HOW MANY ? ::: " + restosCache.size());
        if (restosCache.size() > 0) {
            Log.i("AUTO_COM", "CACHE.InMemoryCache.cacheRestoInMemory: ");
            Restaurant clone = restosCache.get(0);
            //Log.i(TAG, "cacheRestoInMemory: 1_resto::: " + clone.toString());
            mRestaurants = restosCache;
        } else Log.i("AUTO_COM", "CACHE.InMemoryCache.cacheRestoInMemory: mResto is empty");
        this.mRestoTimeCreation = System.currentTimeMillis();
    }


    public List<String> getRestosID() {
        return mRestosID;
    }

    public void setRestosID(String placeID) {
        Log.i(TAG, "CACHE__ setRestosID: " + placeID);
        //if (!mRestosID.contains(placeID)) mRestosID.add(placeID);
        int ii = mRestaurants.size();
        for (int i=0; i<ii; i++) {
            String id = mRestaurants.get(i).getPlaceId();
            if (!mRestosID.contains(id)) mRestosID.add(id);
        }
    }

    // location
    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    // time
    public Long getRestoTimeCreation() {
        return mRestoTimeCreation;
    }

    public void setRestoTimeCreation(Long restoTimeCreation) {
        mRestoTimeCreation = restoTimeCreation;
    }

    // clear
    public void clearRestosCache() {
        mRestaurants.clear();
        mRestosID.clear();
    }

}
