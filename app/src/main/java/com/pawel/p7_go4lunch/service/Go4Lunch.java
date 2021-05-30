package com.pawel.p7_go4lunch.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.multidex.MultiDexApplication;

import com.pawel.p7_go4lunch.utils.Const;

public class Go4Lunch extends MultiDexApplication {

    private static Context sContext;

    public void onCreate() {
        super.onCreate();
        Go4Lunch.sContext = getApplicationContext();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    Const.CHANNEL_ID,
                    Const.VERBOSE_NOTIF_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription(Const.VERBOSE_NOTIF_CHANNEL_DESCRIPT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel1);
        }
    }

    public static Context getContext() {
        return Go4Lunch.sContext;
    }
}
