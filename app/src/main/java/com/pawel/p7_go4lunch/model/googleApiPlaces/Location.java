package com.pawel.p7_go4lunch.model.googleApiPlaces;

public class Location {
    private String lng;
    private String lat;

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "ClassLocation [lng = " + lng + ", lat = " + lat + "]";
    }
}
