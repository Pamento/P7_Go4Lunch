package com.pawel.p7_go4lunch.dataServices;

import com.pawel.p7_go4lunch.BuildConfig;
import com.pawel.p7_go4lunch.model.autocomplete.AutoResponse;
import com.pawel.p7_go4lunch.model.googleApiPlaces.RestaurantResult;
import com.pawel.p7_go4lunch.model.googleApiPlaces.SingleRestaurant;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlaceAPI {

    String KEY = BuildConfig.API_KEY;

    /**
     * Request HTTP in Json nearbySearch for list of Restaurants in defined area
     * <p>
     * http example: https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&type=restaurant&keyword=cruise&key=YOUR_API_KEY
     *f
     * @param location String with current location of User
     * @param radius   double to define the area of research of the restaurant
     * @param key      String API key
     * @return an Observable<RestaurantResult>
     */
    @GET("nearbysearch/json?type=restaurant")
    Observable<RestaurantResult> getNearbyRestaurants(@Query("location") String location,
                                                      @Query("radius") int radius,
                                                      @Query("key") String key);


    /**
     * Request HTTP in Json to have one restaurant details
     * <p>
     * http example: https://maps.googleapis.com/maps/api/place/details/json?place_id=ChIJN1t_tDeuEmsRUsoyG83frY4&key=
     *
     * @param placeId String provide by API Google nearbysearch
     * @param key     String API key
     * @return an Observable<RestaurantResult>
     */

    @GET("details/json?fields=place_id,international_phone_number,website")
    Observable<SingleRestaurant> getDetailsOfRestaurant(@Query("place_id") String placeId,
                                                        @Query("key") String key);

    @GET("details/json?fields=vicinity,name,geometry,opening_hours,international_phone_number,website,rating,photo&key=" + KEY)
    Observable<RestaurantResult> getFullDetailRestaurant(@Query("place_id") String placeId);

    /**
     * Request HTTP in Json (autocomplete) for Restaurants
     * <p>
     * example:   https://maps.googleapis.com/maps/api/place/autocomplete/json?key=YOUR_API_KEY&language=fr&input=pizza+near%20par
     * test: https://maps.googleapis.com/maps/api/place/autocomplete/json?input=Pizza&types=establishment&language=fr&location=37.76999,-122.44696&radius=500&origin=37.76999,-122.44696&strictbounds&key=YOUR_API_KEY
     *
     * KEY String API key
     * @param input String from EditText
     * @return an Observable<AutoResponse>
     */
    @GET("autocomplete/json?types=establishment&strictbounds&key=" + KEY)
    Observable<AutoResponse> getAutocompleteRestaurants(@Query("input") String input,
                                                        @Query("language") String language,
                                                        @Query("radius") int radius,
                                                        @Query("location") String location,
                                                        @Query("origin") String origin);
}
