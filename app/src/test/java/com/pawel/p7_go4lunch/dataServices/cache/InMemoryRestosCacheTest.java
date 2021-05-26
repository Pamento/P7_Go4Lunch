package com.pawel.p7_go4lunch.dataServices.cache;

import com.pawel.p7_go4lunch.model.Restaurant;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static org.junit.Assert.*;

public class InMemoryRestosCacheTest {

    private InMemoryRestosCache mCache;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private static final List<Restaurant> mRestaurants = new ArrayList<>();



    @BeforeClass
    public static void setCache() {
        int i, ii = 5, ij = 5;
        for (i = 0; i<ii; i ++) {
            Restaurant r = new Restaurant();
            r.setDistance((int)(Math.random() * (500 - 50)) + 50);
            mRestaurants.add(r);
        }
        for (i = 0; i<ij; i++) {
            Restaurant r = new Restaurant();
            r.setDistance((int)(Math.random() * (1000 - 501)) + 501);
            mRestaurants.add(r);
        }
    }

    @Before
    public void setUp() {
        mCache = InMemoryRestosCache.getInstance();
        mCache.cacheRestoInMemory(mRestaurants);
    }

    @After
    public void tearDown() {
        if (mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Test
    public void get_all_restos_from_memory_test() {
        List<Restaurant> mRestos = new ArrayList<>();
        mCache.getRestos().subscribe(new Observer<List<Restaurant>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable.add(d);
            }

            @Override
            public void onNext(@NonNull List<Restaurant> restaurants) {
                mRestos.addAll(restaurants);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        assertEquals(10,mRestos.size());
    }

    @Test
    public void get_restos_from_memory_by_radius() {
        List<Restaurant> mRestos = new ArrayList<>();
        mCache.getRestos(500).subscribe(new Observer<List<Restaurant>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable.add(d);
            }

            @Override
            public void onNext(@NonNull List<Restaurant> restaurants) {
                mRestos.addAll(restaurants);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        assertEquals(5,mRestos.size());
    }

    @Test
    public void get_and_set_radius_test() {
        assertEquals(0, mCache.getRadius());
        mCache.setRadius(500);
        assertEquals(500, mCache.getRadius());
        mCache.setRadius(1000);
        assertEquals(1000, mCache.getRadius());
    }
}