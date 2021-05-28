package com.pawel.p7_go4lunch.utils.helpers;

import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.autocomplete.Predictions;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Photo;
import com.pawel.p7_go4lunch.model.googleApiPlaces.Result;
import com.pawel.p7_go4lunch.utils.Tools;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.*;

public class RestaurantsHelperTest {

    private static final String placeId = "ChIJkeO_AquEmsRUpGQn1ZK7Ta";
    private static final List<String> types = new ArrayList<>();
    private static final String internationalPhoneNumber = "+61 2 9280 2029";
    private static final String name = "Harbour Bar & Kitchen";
    private static final Photo sPhoto = new Photo();
    private static final List<Photo> photos = new ArrayList<>();
    private static final Double rating = 4.7;
    private static final String reference = "ATtYBwLNS7HI7bKPhNR70P8k3JJrTGfUSKGCtoKfETvu_S";
    private static final String vicinity = "447 Harbourside shopping centre, Level 2, 2-10 Darling Drive, Darling Harbour";
    private static final String website = "https://www.harbourbarandkitchen.com.au/?utm_source=google&utm_medium=organic&utm_campaign=mybusiness";

    private RestaurantsHelper mRestaurantHelper;
    private static final Result mResult = new Result();
    private static Restaurant mRestaurant = new Restaurant();
    private static final List<Predictions> mPredictions = new ArrayList<>();

    @BeforeClass
    public static void setData() {
        sPhoto.setPhotoReference(reference);
        photos.add(sPhoto);
        types.add("establishment");
        types.add("restaurant");

        for (int i = 0; i<2; i++) {
            Predictions mPrediction = new Predictions();
            Integer distanceMeters = 300;
            mPrediction.setDistanceMeters(distanceMeters);
            mPrediction.setPlaceId(placeId + i);
            mPrediction.setTypes(types);
            mPredictions.add(mPrediction);
        }

        mResult.setPlaceId(placeId);
        mResult.setName(name);
        mResult.setVicinity(vicinity);
        mResult.setGeometry(null);
        mResult.setOpeningHours(null);
        mResult.setPhotos(photos);
        mResult.setRating(rating);
        mResult.setInternationalPhoneNumber(internationalPhoneNumber);
        mResult.setWebsite(website);
    }

    @Before
    public void setUp() {
        mRestaurantHelper = new RestaurantsHelper();
        mRestaurant = new Restaurant();
    }

    @Test
    public void create_restaurant() {
        Restaurant resto = mRestaurantHelper.createRestaurant(mResult,null);
        int ratingI = Tools.intRating(rating);
        assertThat(resto.getPlaceId()).isEqualTo("ChIJkeO_AquEmsRUpGQn1ZK7Ta");
        assertThat(resto.getName()).isEqualTo("Harbour Bar & Kitchen");
        assertThat(resto.getAddress()).isEqualTo("447 Harbourside shopping centre, Level 2, 2-10 Darling Drive, Darling Harbour");
        assertNull(resto.getLocation());
        assertNull(resto.getOpeningHours());
        assertNotNull(resto.getImage());
        assertThat(resto.getRating()).isEqualTo(ratingI);
        assertNotNull(resto.getDateCreated());
        assertNotNull(resto.getUserList());
        assertThat(resto.getPhoneNumber()).isEqualTo("+61 2 9280 2029");
        assertThat(resto.getWebsite()).isEqualTo("https://www.harbourbarandkitchen.com.au/?utm_source=google&utm_medium=organic&utm_campaign=mybusiness");
    }

    @Test
    public void set_resto_from_predictions() {
        List<Restaurant> mRestos = mRestaurantHelper.setRestoFromPredictions(mPredictions);
        assertThat(mRestos.size()).isEqualTo(2);
    }

    @Test
    public void update_with_detail() {
        Restaurant emptyResto = new Restaurant();
        assertThat(mRestaurant.toString()).isEqualTo(emptyResto.toString());
        Restaurant resto = mRestaurantHelper.updateWithDetail(mResult, mRestaurant);
        int ratingI = Tools.intRating(rating);
        assertThat(resto.getName()).isEqualTo("Harbour Bar & Kitchen");
        assertThat(resto.getAddress()).isEqualTo("447 Harbourside shopping centre, Level 2, 2-10 Darling Drive, Darling Harbour");
        assertNull(resto.getLocation());
        assertNull(resto.getOpeningHours());
        assertNotNull(resto.getImage());
        assertThat(resto.getRating()).isEqualTo(ratingI);
        assertNotNull(resto.getDateCreated());
        assertNotNull(resto.getUserList());
    }

    @Test
    public void update_resto_with_contact() {
        Restaurant emptyResto = new Restaurant();
        assertThat(mRestaurant.toString()).isEqualTo(emptyResto.toString());
        Restaurant resto = mRestaurantHelper.updateRestoWithContact(mResult,mRestaurant);

        assertThat(resto.getPhoneNumber()).isEqualTo("+61 2 9280 2029");
        assertThat(resto.getWebsite()).isEqualTo("https://www.harbourbarandkitchen.com.au/?utm_source=google&utm_medium=organic&utm_campaign=mybusiness");
    }

    @Test
    public void get_photo() {
        String imageUrl= mRestaurantHelper.getPhoto(reference);
        assertNotNull(imageUrl);
    }
}