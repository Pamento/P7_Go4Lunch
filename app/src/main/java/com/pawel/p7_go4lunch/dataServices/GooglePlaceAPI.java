package com.pawel.p7_go4lunch.dataServices;

import com.pawel.p7_go4lunch.model.googleApiPlaces.RestaurantResult;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface GooglePlaceAPI {
    // TODO wite the code
    @GET
    Call<RestaurantResult> getNearByPlace(@Url String url);
    /**
     * Request HTTP in Json to have nearby Restaurants
     * @return an Observable<RestaurantPOJO>
     * @param location String with latitude and longitude of the current User
     * @param radius double to define the distance around the current User
     * @param type String to matches with Restaurant
     * @param key String API key
     *     example:
     *     https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&types=food&name=harbour&key=
     */
    @GET("nearbysearch/json?")
    Observable<RestaurantResult> getNearbyRestaurants (@Query("location") String location,
                                                       @Query("radius") int radius,
                                                       @Query ("type") String type,
                                                       @Query("opening_hours") Boolean openingHours,
                                                       @Query("key") String key);


    /**
     * Request HTTP in Json to have the Restaurant's Details
     * @return an Observable<DetailPOJO>
     * @param placeId String provide by API Google
     * @param key String API key
     */
    @GET("details/json?")
    Observable<RestaurantResult> getDetailRestaurants (@Query("place_id") String placeId,
                                                 @Query("key") String key);


    /**
     * Request HTTP in Json to have nearby Restaurants
     * @return an Observable<RestaurantPOJO>
     * @param key String API key
     * @param input String from EditText
     */
    @GET("queryautocomplete/json?")
    Observable<RestaurantResult> getAutocompleteRestaurants (@Query("key") String key,
                                                           @Query("input") String input,
                                                           @Query("location") String location,
                                                           @Query("radius") int radius);
}
