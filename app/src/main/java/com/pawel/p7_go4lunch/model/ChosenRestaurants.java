package com.pawel.p7_go4lunch.model;

public class ChosenRestaurants {
    private String placeId;
    private String name;

    public ChosenRestaurants() {
    }

    public ChosenRestaurants(String placeId, String name) {
        this.placeId = placeId;
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
