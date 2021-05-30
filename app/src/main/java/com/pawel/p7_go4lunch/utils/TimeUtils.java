package com.pawel.p7_go4lunch.utils;


import androidx.annotation.VisibleForTesting;

public abstract class TimeUtils {

    public static int[] timeToInt(String hour) {
        int[] t = new int[2];
        if (hour.length() == 2) {
            t[0] = sToInt(hour);
            t[1] = 0; // don't remove this assignment even if Android Studio give warning. Can cause not set of alarm.
        } else {
            String[] sps = hour.split("_");
            t[0] = sToInt(sps[0]);
            t[1] = sToInt(sps[1]);
        }
        return t;
    }

    @VisibleForTesting
    public static int sToInt(String sh) {
        int r = 0;
        try {
            r = Integer.parseInt(sh);
        } catch (NumberFormatException ne) {
            ne.getStackTrace();
        }
        return r;
    }
}
