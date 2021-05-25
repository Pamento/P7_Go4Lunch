package com.pawel.p7_go4lunch.utils.helpers;

import android.location.Location;
import android.util.Log;

import com.pawel.p7_go4lunch.BuildConfig;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.autocomplete.Predictions;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.utils.Tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RestaurantsHelper {

    public Restaurant createRestaurant(Result result, Location location) {
        Restaurant r = new Restaurant();
        Location l = new Location("");
        if (result != null) {
            if (result.getPlaceId() != null) r.setPlaceId(result.getPlaceId());
            r.setDateCreated(new Date());
            if (result.getName() != null) r.setName(result.getName());
            if (result.getVicinity() != null) r.setAddress(result.getVicinity());
            if (result.getGeometry() != null) {
                r.setLocation(result.getGeometry().getLocation());
                l.setLatitude(result.getGeometry().getLocation().getLat());
                l.setLongitude(result.getGeometry().getLocation().getLng());
                float dt = location.distanceTo(l);
                r.setDistance(Math.round(dt));
            }
            if (result.getOpeningHours() != null)
                r.setOpeningHours(result.getOpeningHours());
            if (result.getPhotos() != null)
                r.setImage(getPhoto(result.getPhotos().get(0).getPhotoReference()));
            if (result.getRating() != null) r.setRating(Tools.intRating(result.getRating()));
            if (result.getInternationalPhoneNumber() != null)
                r.setPhoneNumber(result.getInternationalPhoneNumber());
            if (result.getWebsite() != null) r.setWebsite(result.getWebsite());
            r.setUserList(new ArrayList<>());
            return r;
        }
        return null;
    }

    public List<Restaurant> setRestoFromPredictions(List<Predictions> predictions) {
        List<Restaurant> restos = new ArrayList<>();
        if (predictions != null) {
            for (Predictions p : predictions) {
                if (p.getTypes().contains("restaurant")) {
                    Restaurant rst = new Restaurant();
                    rst.setPlaceId(p.getPlaceId());
                    rst.setDistance(p.getDistanceMeters());
                    restos.add(rst);
                }
            }
        }
        return restos;
    }

    public Restaurant updateWithDetail(Result result, Restaurant rcp) {
        rcp.setDateCreated(new Date());
        if (result.getName() != null) rcp.setName(result.getName());
        if (result.getVicinity() != null) rcp.setAddress(result.getVicinity());
        if (result.getGeometry() != null) {
            rcp.setLocation(result.getGeometry().getLocation());
        }
        if (result.getOpeningHours() != null)
            rcp.setOpeningHours(result.getOpeningHours());
        if (result.getPhotos() != null)
            rcp.setImage(getPhoto(result.getPhotos().get(0).getPhotoReference()));
        if (result.getRating() != null) rcp.setRating(Tools.intRating(result.getRating()));
        rcp.setUserList(new ArrayList<>());
        return rcp;
    }

    public Restaurant updateRestoWithContact(Result result, Restaurant rcp) {
        rcp.setPhoneNumber(result.getInternationalPhoneNumber());
        rcp.setWebsite(result.getWebsite());
        return rcp;
    }

    public String getPhoto(String photoReference) {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + BuildConfig.API_KEY;
    }
}
