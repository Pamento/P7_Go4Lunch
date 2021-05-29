package com.pawel.p7_go4lunch.utils;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class TimeUtilsTest {

    @Test
    public void time_to_int() {
        String time1 = "12_15";
        String time2 = "12";
        int[] time1converted = TimeUtils.timeToInt(time1);
        int[] time2converted = TimeUtils.timeToInt(time2);
        assertEquals(12, time1converted[0]);
        assertEquals(15, time1converted[1]);
        assertEquals(12, time2converted[0]);
        assertEquals(0, time2converted[1]);
    }

    @Test
    public void s_to_int() {
        int test1 = TimeUtils.sToInt("12");
        int test2 = TimeUtils.sToInt("15");
        int test3 = TimeUtils.sToInt("12_15");
        int test4 = TimeUtils.sToInt("str");
        assertEquals(12,test1);
        assertEquals(15,test2);
        assertEquals(0,test3);
        assertEquals(0,test4);
    }

//    @Test
//    public void string_to_int() {
//        try {
//            Object timeUtils = TimeUtils.class.newInstance(); // abstract declaration donne:
//            // java.lang.InstantiationException
//            //	at sun.reflect.InstantiationExceptionConstructorAccessorImpl.newInstance(InstantiationExceptionConstructorAccessorImpl.java:48)
//            //	at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
//            //	at java.lang.Class.newInstance(Class.java:442)
//            //	at com.pawel.p7_go4lunch.utils.TimeUtilsTest.string_to_int(TimeUtilsTest.java:28)
//
//            Method method = timeUtils.getClass().getDeclaredMethod("sToInt", String.class);
//            method.setAccessible(true);
//            int test1 = (int) method.invoke(timeUtils, "12");
//            int test2 = (int) method.invoke(timeUtils, "13");
//            assertEquals(12, test1);
//            assertEquals(13, test2);
//        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
}