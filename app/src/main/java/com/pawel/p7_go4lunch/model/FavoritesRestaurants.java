package com.pawel.p7_go4lunch.model;

public class FavoritesRestaurants {
    private String placeId;

    public FavoritesRestaurants() {
    }

    public FavoritesRestaurants(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
