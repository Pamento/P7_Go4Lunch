package com.pawel.p7_go4lunch.model;

import com.google.firebase.firestore.ServerTimestamp;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Location;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class Restaurant {

    private String placeId;
    private Date dateCreated;
    private Location location;
    private String name;
    private String address;
    @Nullable
    private String image;
    private double rating;
    @Nullable
    private String phoneNumber;
    @Nullable
    private String website;
    @Nullable
    private List<User> userList;

    public Restaurant() {}

    public Restaurant(String placeId, Location location, String name, String address,
                      @Nullable String image, double rating, @Nullable String phoneNumber, @Nullable String website,
                      @Nullable List<User> userList) {
        this.placeId = placeId;
        this.location = location;
        this.name = name;
        this.address = address;
        this.image = image;
        this.rating = rating;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.userList = userList;
    }

    // .........................Getters
    public String getPlaceId() {
        return placeId;
    }

    @ServerTimestamp
    public Date getDateCreated() {
        return dateCreated;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    @Nullable
    public String getImage() {
        return image;
    }

    public double getRating() {
        return rating;
    }

    @Nullable
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Nullable
    public String getWebsite() {
        return website;
    }

    @Nullable
    public List<User> getUserList() {
        return userList;
    }

    // .....................................Setters
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setImage(@Nullable String image) {
        this.image = image;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setPhoneNumber(@Nullable String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setWebsite(@Nullable String website) {
        this.website = website;
    }

    public void setUserList(@Nullable List<User> userList) {
        this.userList = userList;
    }
}