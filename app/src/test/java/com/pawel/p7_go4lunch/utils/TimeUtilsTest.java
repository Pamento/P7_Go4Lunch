package com.pawel.p7_go4lunch.utils;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class TimeUtilsTest {

    @Test
    public void timeToInt() {
        String time1 = "12_15";
        String time2 = "12";
        int[] time1converted = TimeUtils.timeToInt(time1);
        int[] time2converted = TimeUtils.timeToInt(time2);
        assertEquals(12, time1converted[0]);
        assertEquals(15, time1converted[1]);
        assertEquals(12, time2converted[0]);
    }
}