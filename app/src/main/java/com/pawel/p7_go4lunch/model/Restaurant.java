package com.pawel.p7_go4lunch.model;

import com.google.firebase.firestore.ServerTimestamp;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Location;
import com.pawel.p7_go4lunch.model.googleApiPlaces.OpeningHours;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class Restaurant {

    private String placeId;
    private Date dateCreated;
    private Location location;
    private Float distance;
    private String name;
    private String address;
    @Nullable
    private OpeningHours openingHours;
    @Nullable
    private String image;
    private double rating;
    @Nullable
    private String phoneNumber;
    @Nullable
    private String website;
    @Nullable
    private List<String> userList;

    public Restaurant() {}

    public Restaurant(String placeId, Location location, String name, String address,
                      @Nullable OpeningHours openingHours,
                      @Nullable String image, double rating, @Nullable String phoneNumber, @Nullable String website,
                      @Nullable List<String> userList) {
        this.placeId = placeId;
        this.location = location;
        this.name = name;
        this.address = address;
        this.openingHours = openingHours;
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

    public Float getDistance() {
        return distance;
    }
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    @Nullable
    public OpeningHours getOpeningHours() {
        return openingHours;
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
    public List<String> getUserList() {
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

    public void setDistance(Float distance) { this.distance = distance; }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setOpeningHours(@Nullable OpeningHours openingHours) {
        this.openingHours = openingHours;
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

    public void setUserList(@Nullable List<String> userList) {
        this.userList = userList;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "placeId='" + placeId + '\'' +
                ", dateCreated=" + dateCreated +
                ", location=" + location +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", openingHours='" + openingHours + '\'' +
                ", image='" + image + '\'' +
                ", rating=" + rating +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", website='" + website + '\'' +
                ", userList=" + userList +
                '}';
    }
}
