package com.pawel.p7_go4lunch.model.googleApiPlaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Geometry {

    @SerializedName("location")
    @Expose
    private RestaurantResult.Location location;
    @SerializedName("viewport")
    @Expose
    private RestaurantResult.Viewport viewport;

    public RestaurantResult.Location getLocation() {
        return location;
    }

    public void setLocation(RestaurantResult.Location location) {
        this.location = location;
    }

    public RestaurantResult.Viewport getViewport() {
        return viewport;
    }

    public void setViewport(RestaurantResult.Viewport viewport) {
        this.viewport = viewport;
    }
    @Override
    public String toString() {
        return "ClassGeometry [viewport = " + viewport + ", location = " + location + "]";
    }
}

