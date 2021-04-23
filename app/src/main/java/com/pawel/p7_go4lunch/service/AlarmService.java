package com.pawel.p7_go4lunch.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.pawel.p7_go4lunch.utils.receiver.AlarmReceiver;

import java.util.Calendar;

public abstract class AlarmService {

    private static Calendar mCalendar;

    private static void setCalendar() {
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mCalendar.set(Calendar.HOUR_OF_DAY, 12);
    }

    public static void startAlarm() {
        setCalendar();
        AlarmManager aMgr = (AlarmManager) Go4Lunch.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(Go4Lunch.getContext(), AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(Go4Lunch.getContext(),0,intent,0);
        aMgr.setExact(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(),alarmIntent);
    }

    public static void startRepeatedAlarm() {
        setCalendar();
        AlarmManager aMgr = (AlarmManager) Go4Lunch.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(Go4Lunch.getContext(), AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(Go4Lunch.getContext(),0,intent,0);
        aMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,alarmIntent);
    }

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
