package com.pawel.p7_go4lunch.dataServices.cache;

import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.Tools;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class InMemoryRestosCache implements Cloneable {

    private static volatile InMemoryRestosCache instance;
    private List<Restaurant> mRestaurants = new ArrayList<>();
    private long mCachedAt = 0;
    private Location mLocation;
    private int mRadius = 0;

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
            emitter.onNext(sendRestaurantsIfNotToOld());
            emitter.onComplete();
        });
    }

    public Observable<List<Restaurant>> getRestos(int radius) {
        return Observable.create(emitter -> {
            emitter.onNext(selectedRestaurant(radius));
            emitter.onComplete();
        });
    }

    public void cacheRestoInMemory(List<Restaurant> restosCache) {
        if (restosCache.size() > 0) {
            mRestaurants = restosCache;
            mCachedAt = System.currentTimeMillis();
        }
    }

    private List<Restaurant> sendRestaurantsIfNotToOld() {
        // In case the user live app active in background for days,
        // the cache keep the data in memory max 7 days
        if (Tools.isTimeGreaterThan(mCachedAt)) {
            mRestaurants.clear();
            return new ArrayList<>();
        } else return mRestaurants;
    }

    private List<Restaurant> selectedRestaurant(int radius) {
        List<Restaurant> temp = new ArrayList<>();
        int i, ii = mRestaurants.size();
        for (i = 0; i < ii; i++) {
            if (mRestaurants.get(i).getDistance() <= radius) temp.add(mRestaurants.get(i));
        }
        return temp;
    }

    // location
    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }
}
