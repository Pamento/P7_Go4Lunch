package com.pawel.p7_go4lunch.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pawel.p7_go4lunch.utils.receiver.AlarmReceiver;

import java.util.Calendar;

public abstract class AlarmService {

    private static final String TAG = "NOTIF";
    private static Calendar mCalendar;

    private static void setCalendar(String hour) {
        int h;
        int m = 0;
        if (hour.length() == 2) {
            h = sToInt(hour);
        } else {
            String[] sps = hour.split("_");
            h = sToInt(sps[0]);
            m = sToInt(sps[1]);
        }
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mCalendar.set(Calendar.HOUR_OF_DAY, 20);
        mCalendar.set(Calendar.MINUTE, 46);
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

    public static void startAlarm(String hour) {
        Log.i(TAG, "startAlarm ___at: " + hour);
        setCalendar(hour);
        AlarmManager aMgr = (AlarmManager) Go4Lunch.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(Go4Lunch.getContext(), AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(Go4Lunch.getContext(), 0, intent, 0);
        aMgr.setExact(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), alarmIntent);
    }

//    public static void startRepeatedAlarm(String hour) {
//        Log.i(TAG, "startRepeatedAlarm ___at:" + hour);
//        setCalendar(hour);
//        AlarmManager aMgr = (AlarmManager) Go4Lunch.getContext().getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(Go4Lunch.getContext(), AlarmReceiver.class);
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(Go4Lunch.getContext(), 0, intent, 0);
//        aMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
//    }

    public static void cancelAlarm() {
        AlarmManager aMgr = (AlarmManager) Go4Lunch.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(Go4Lunch.getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(Go4Lunch.getContext(), 0, intent,
                        PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null && aMgr != null) {
            aMgr.cancel(pendingIntent);
        }
    }
}
