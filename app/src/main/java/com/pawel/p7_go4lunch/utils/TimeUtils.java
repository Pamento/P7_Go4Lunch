package com.pawel.p7_go4lunch.utils;


public abstract class TimeUtils {

    public static int[] timeToInt(String hour) {
        int[] t = new int[2];
        if (hour.length() == 2) {
            t[0] = sToInt(hour);
            t[1] = 0;
        } else {
            String[] sps = hour.split("_");
            t[0] = sToInt(sps[0]);
            t[1] = sToInt(sps[1]);
        }
        return t;
    }

    private static int sToInt(String sh) {
        int r = 0;
        try {
            r = Integer.parseInt(sh);
        } catch (NumberFormatException ne) {
            ne.getStackTrace();
        }
        return r;
    }

//    public static int[] currentHour() {
//        int[] t = new int[2];
//        Calendar calendar = GregorianCalendar.getInstance();
//        calendar.setTime(new Date());
//        t[0] = calendar.get(Calendar.HOUR_OF_DAY);
//        t[1] = calendar.get(Calendar.MINUTE);
//        return t;
//    }
//
//    public static boolean isGreaterThan(int[] cTime, int[] timeSet) {
//        return cTime[0] > timeSet[0] || (cTime[0] == timeSet[0] && cTime[1] > timeSet[1]);
//    }
}
