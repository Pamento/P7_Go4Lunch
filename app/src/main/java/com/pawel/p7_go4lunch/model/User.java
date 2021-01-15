package com.pawel.p7_go4lunch.model;

import java.util.List;

import javax.annotation.Nullable;

public class User {

    private String uid;
    private String name;
    private String email;
    @Nullable
    private String UrlImage;
    @Nullable
    private Restaurant userRestaurant;
    @Nullable
    private List<String> favoritesRestaurants;

    /**
     * Firebase instance need empty constructor
     */
    public User() {
    }

    public User(String uid, String name, String email, @Nullable String urlImage) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        UrlImage = urlImage;
    }

    public User(String uid, String name, String email, @Nullable String urlImage, @Nullable Restaurant userRestaurant, @Nullable List<String> favoritesRestaurants) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        UrlImage = urlImage;
        this.userRestaurant = userRestaurant;
        this.favoritesRestaurants = favoritesRestaurants;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Nullable
    public String getUrlImage() {
        return UrlImage;
    }

    public void setUrlImage(@Nullable String urlImage) {
        UrlImage = urlImage;
    }

    @Nullable
    public Restaurant getUserRestaurant() {
        return userRestaurant;
    }

    public void setUserRestaurant(@Nullable Restaurant userRestaurant) {
        this.userRestaurant = userRestaurant;
    }

    @Nullable
    public List<String> getFavoritesRestaurants() {
        return favoritesRestaurants;
    }

    public void setFavoritesRestaurants(@Nullable List<String> favoritesRestaurants) {
        this.favoritesRestaurants = favoritesRestaurants;
    }
}
