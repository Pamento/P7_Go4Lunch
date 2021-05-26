package com.pawel.p7_go4lunch.utils.helpers;

import com.pawel.p7_go4lunch.model.Restaurant;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class FilterRestaurantsTest {

    private static final List<Restaurant> mRestaurants = new ArrayList<>();
    private static final String[] namesMIX = {"Zuchotto","Vanilla beans","Chili con carne","Pizza","Tacos","Steck","Barbecue","Mussaca","Octopus","Aramis",};
    private static final String[] namesAZ = {"Aramis","Barbecue","Chili con carne","Mussaca","Octopus","Pizza","Steck","Tacos","Vanilla beans","Zuchotto"};

    @BeforeClass
    public static void setCache() {
        int i, ii = 5,ij = 10;
        for (i = 0; i<ii; i++) {
            Restaurant r = new Restaurant();
            r.setName(namesMIX[i]);
            r.setRating(3);
            mRestaurants.add(r);
        }
        for (i = 5; i<ij; i++) {
            Restaurant r = new Restaurant();
            r.setName(namesMIX[i]);
            r.setRating(2);
            mRestaurants.add(r);
        }
    }

    @Test
    public void by_rating_2_test() {
        List<Restaurant> mRestos;
        mRestos = FilterRestaurants.byRating(mRestaurants,2);

        assertThat(mRestos.size()).isEqualTo(5);
    }

    @Test
    public void by_rating_3_test() {
        List<Restaurant> mRestos = new ArrayList<>();
        mRestos = FilterRestaurants.byRating(mRestaurants,3);

        assertThat(mRestos.size()).isEqualTo(5);
    }

    @Test
    public void by_AZ_test() {
        List<Restaurant> mRestos = new ArrayList<>();
        mRestos = FilterRestaurants.byAZ(mRestaurants);

        String firstPosition = mRestos.get(0).getName();
        String lastPosition = mRestos.get(9).getName();
        String[] namesAfterFilterAZ = new String[10];
        for (int i = 0; i < mRestos.size(); i++) {
            namesAfterFilterAZ[i] = mRestos.get(i).getName();
        }

        assertThat(firstPosition).isEqualTo("Aramis");
        assertThat(lastPosition).isEqualTo("Zuchotto");
        assertThat(namesAZ).isEqualTo(namesAfterFilterAZ);
    }
}