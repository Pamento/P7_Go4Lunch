package com.pawel.p7_go4lunch.dataServices;

import com.pawel.p7_go4lunch.model.googleApiPlaces.RestaurantResult;
import com.pawel.p7_go4lunch.model.googleApiPlaces.SingleRestaurant;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlaceAPI {

    // TODO wite the code

    /**
     * Request HTTP in Json nearbySearch for list of Restaurants in defined area
     * <p>
     * http example: https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=1500&type=restaurant&keyword=cruise&key=YOUR_API_KEY
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
//    @GET("details/json?")
//    Observable<RestaurantResult> getDetailRestaurants (@Query("place_id") String placeId,
//                                                 @Query("key") String key);
//    @GET("details/json?fields=vicinity,name,place_id,id,geometry,opening_hours,international_phone_number,website,rating,utc_offset,photos")
    @GET("details/json?fields=place_id,international_phone_number,website")
    Observable<SingleRestaurant> getDetailsOfRestaurant(@Query("place_id") String placeId,
                                                        @Query("key") String key);


    /**
     * Request HTTP in Json (autocomplete) for Restaurants
     * <p>
     * example:   https://maps.googleapis.com/maps/api/place/queryautocomplete/json?key=YOUR_API_KEY&language=fr&input=pizza+near%20par
     *
     * @param key   String API key
     * @param input String from EditText
     * @return an Observable<RestaurantPOJO>
     */
    @GET("queryautocomplete/json?")
    Observable<RestaurantResult> getAutocompleteRestaurants(@Query("key") String key,
                                                            @Query("language") String language,
                                                            @Query("input") String input,
                                                            @Query("location") String location,
                                                            @Query("radius") int radius);
}
