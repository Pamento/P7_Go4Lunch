package com.pawel.p7_go4lunch.utils;

import org.junit.Test;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

public class ToolsTest {

    @Test
    public void intRating() {
        double googleRating1 = 4.7;
        double googleRating2 = 3.7;
        double googleRating3 = 2.7;
        double googleRating4 = 1.7;
        double googleRating5 = 0.7;
        double googleRating6 = 0.0;

        int restoRating1 = Tools.intRating(googleRating1);
        int restoRating2 = Tools.intRating(googleRating2);
        int restoRating3 = Tools.intRating(googleRating3);
        int restoRating4 = Tools.intRating(googleRating4);
        int restoRating5 = Tools.intRating(googleRating5);
        int restoRating6 = Tools.intRating(googleRating6);

        assertThat(restoRating1).isLessThan(4);
        assertThat(restoRating1).isEqualTo(3);
        assertThat(restoRating2).isLessThan(4);
        assertThat(restoRating2).isEqualTo(2);
        assertThat(restoRating3).isEqualTo(2);
        assertThat(restoRating4).isEqualTo(1);
        assertThat(restoRating5).isEqualTo(0);
        assertThat(restoRating1).isIn(Arrays.asList(1, 2, 3));
        assertThat(restoRating2).isIn(Arrays.asList(1, 2, 3));
        assertThat(restoRating3).isIn(Arrays.asList(1, 2, 3));
        assertThat(restoRating4).isIn(Arrays.asList(1, 2, 3));
        assertThat(restoRating5).isIn(Arrays.asList(0, 1, 2, 3));
        assertThat(restoRating6).isIn(Arrays.asList(0, 1, 2, 3));
    }

    @Test
    public void isTimeGreaterThan() {
        long bigTime = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 8);
        long smallTime = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 6);
        boolean big = Tools.isTimeGreaterThan(bigTime);
        boolean small = Tools.isTimeGreaterThan(smallTime);
        assertThat(big).isTrue();
        assertThat(small).isFalse();
    }
}