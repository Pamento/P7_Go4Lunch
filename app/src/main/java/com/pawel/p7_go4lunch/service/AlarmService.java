package com.pawel.p7_go4lunch.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.pawel.p7_go4lunch.utils.TimeUtils;
import com.pawel.p7_go4lunch.utils.receiver.AlarmReceiver;

import java.util.Calendar;

import static com.pawel.p7_go4lunch.utils.Const.ALARM_ID;
import static com.pawel.p7_go4lunch.utils.Const.ALARM_MULTIPLE;
import static com.pawel.p7_go4lunch.utils.Const.ALARM_SINGLE;
import static com.pawel.p7_go4lunch.utils.Const.NOTIF_PENDING_ID;
import static com.pawel.p7_go4lunch.utils.Const.ONE_DAY_IN_MILLIS;

public abstract class AlarmService {

    private static final String TAG = "NOTIF";
    private static Calendar mCalendar;

    private static void setCalendar(String hour) {
        int[] time = TimeUtils.timeToInt(hour);
        // For test purpose
//        mCalendar.set(Calendar.HOUR_OF_DAY, 8);
//        mCalendar.set(Calendar.MINUTE, 15);

        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mCalendar.set(Calendar.HOUR_OF_DAY, time[0]);
        mCalendar.set(Calendar.MINUTE, time[1]);
        Log.i(TAG, "setCalendar: before " + Calendar.getInstance().before(mCalendar));
        Log.i(TAG, "setCalendar: after  " + Calendar.getInstance().after(mCalendar));
        if (Calendar.getInstance().after(mCalendar)) {
            Log.i(TAG, "AlarmService.setCalendar: ");
            mCalendar.add(Calendar.MILLISECOND, ONE_DAY_IN_MILLIS);
            //mCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public static void startAlarm(String hour) {
        Log.i(TAG, "startAlarm: system.Millis " + System.currentTimeMillis());
        Log.i(TAG, "startAlarm ___at: " + hour);
        setCalendar(hour);
        if (mCalendar != null) {
            AlarmManager aMgr = (AlarmManager) Go4Lunch.getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(Go4Lunch.getContext(), AlarmReceiver.class);
            intent.putExtra(ALARM_ID, ALARM_SINGLE);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(Go4Lunch.getContext(), NOTIF_PENDING_ID, intent, 0);
            assert aMgr != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                aMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), alarmIntent);
            } else {
                aMgr.setExact(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), alarmIntent);
            }
        }
    }

    public static void startRepeatedAlarm(String hour) {
        Log.i(TAG, "startRepeatedAlarm ___at:" + hour);
        setCalendar(hour);
        if (mCalendar != null) {
            AlarmManager aMgr = (AlarmManager) Go4Lunch.getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(Go4Lunch.getContext(), AlarmReceiver.class);
            intent.putExtra(ALARM_ID, ALARM_MULTIPLE);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(Go4Lunch.getContext(), NOTIF_PENDING_ID, intent, 0);
            assert aMgr != null;
            aMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public static PendingIntent isAlarmSet() {
        Log.i(TAG, "isAlarmSet: ");
        Intent intent = new Intent(Go4Lunch.getContext(), AlarmReceiver.class);
        return PendingIntent.getBroadcast(Go4Lunch.getContext(), NOTIF_PENDING_ID, intent,
                        PendingIntent.FLAG_NO_CREATE);
    }

    public static void cancelAlarm() {
        Log.i(TAG, "AlarmService:::/cancelAlarm: ");
        PendingIntent pI = isAlarmSet();
        AlarmManager aMgr = (AlarmManager) Go4Lunch.getContext().getSystemService(Context.ALARM_SERVICE);
        if (pI != null && aMgr != null) {
            aMgr.cancel(pI);
        }
    }
}
