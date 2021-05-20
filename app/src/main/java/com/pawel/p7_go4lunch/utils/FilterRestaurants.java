package com.pawel.p7_go4lunch.utils;

import android.os.Build;

import com.pawel.p7_go4lunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FilterRestaurants {

    public static List<Restaurant> byRating(List<Restaurant> restos, double value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return restos.stream()
                    .filter(restaurant -> restaurant.getRating() >= value).collect(Collectors.toList());
        } else {
            List<Restaurant> temp = new ArrayList<>();
            int i, ii = restos.size();
            for (i= 0; i<ii; i++) {
                if (restos.get(i).getRating() > value) temp.add(restos.get(i));
            }
            return temp;
        }
    }

    private static List<Restaurant> byAZ(List<Restaurant> restos, String filterValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return restos.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList());
        } else {
            Collections.sort(restos, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            return restos;
        }
    }
}
