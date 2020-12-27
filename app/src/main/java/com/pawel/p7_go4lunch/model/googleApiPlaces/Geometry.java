package com.pawel.p7_go4lunch.model.googleApiPlaces;


import android.location.Location;

public class Geometry {
    private Viewport viewport;
    private Location location;

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "ClassGeometry [viewport = " + viewport + ", location = " + location + "]";
    }
}

