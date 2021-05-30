package com.pawel.p7_go4lunch.utils.helpers;

import android.os.Build;

import com.pawel.p7_go4lunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class FilterRestaurants {

    public static List<Restaurant> byRating(List<Restaurant> restos, int value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return restos.stream()
                    .filter(restaurant -> restaurant.getRating() == value).collect(Collectors.toList());
        } else {
            List<Restaurant> temp = new ArrayList<>();
            int i, ii = restos.size();
            for (i = 0; i < ii; i++) {
                int rating = restos.get(i).getRating();
                if (rating == value) temp.add(restos.get(i));
            }
            return temp;
        }
    }

    public static List<Restaurant> byAZ(List<Restaurant> restos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return restos.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList());
        } else {
            Collections.sort(restos, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            return restos;
        }
    }

    public static List<Restaurant> byDistance(List<Restaurant> restos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return restos.stream().sorted((o1, o2) -> o1.getDistance() - o2.getDistance()).collect(Collectors.toList());
        } else {
            Collections.sort(restos, (o1, o2) -> o1.getDistance() - o2.getDistance());
            return restos;
        }
    }
}
