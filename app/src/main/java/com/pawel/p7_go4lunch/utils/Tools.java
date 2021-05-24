package com.pawel.p7_go4lunch.utils;

public abstract class Tools {

    public static int intRating(double rating) {
        long r = Math.round(rating * 3 / 5);
        return (int) r;
    }
}
