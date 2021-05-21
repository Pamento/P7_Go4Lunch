package com.pawel.p7_go4lunch.utils;

import android.os.Build;
import android.util.Log;

import com.pawel.p7_go4lunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FilterRestaurants {
    private static final String TAG = "AUTO_COM";
    public static List<Restaurant> byRating(List<Restaurant> restos, int value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i(TAG, "byRating: 24+ __restos.size() " + restos.size());
            return restos.stream()
                    .filter(restaurant -> restaurant.getRating() == value).collect(Collectors.toList());
        } else {
            Log.i(TAG, "byRating: 19 - 24 __restos.size() " + restos.size());
            List<Restaurant> temp = new ArrayList<>();
            int i, ii = restos.size();
            for (i = 0; i < ii; i++) {
                long rating = Math.round(restos.get(i).getRating() * 3 / 5);
                Log.i(TAG, "byRating: name: :: : " + restos.get(i).getName());
                Log.i(TAG, "byRating: rating::::: " + rating + " == " + value);
                if (rating == value) temp.add(restos.get(i));
            }
            Log.i(TAG, "byRating: temp.size() before return;  ::::  " + temp.size());
            return temp;
        }
    }

    public static List<Restaurant> byAZ(List<Restaurant> restos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i(TAG, "byAZ: 24+  __restos.size() " + restos.size());
            return restos.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList());
        } else {
            Log.i(TAG, "byAZ: 19 - 24  __restos.size() " + restos.size());
            Collections.sort(restos, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            Log.i(TAG, "byAZ: before return; restos.size() " + restos.size());
            return restos;
        }
    }
}
