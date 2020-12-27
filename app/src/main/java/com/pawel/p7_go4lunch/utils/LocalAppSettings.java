package com.pawel.p7_go4lunch.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class LocalAppSettings {

    private boolean notification;
    private String hour;
    private boolean localisation;
    private String perimeter;


    public LocalAppSettings(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        notification = prefs.getBoolean("notification", true);
        hour = prefs.getString("hour","12");
        localisation = prefs.getBoolean("localisation",true);
        perimeter = prefs.getString("perimeter", "max");
    }

    public boolean isNotification() {
        return notification;
    }

    public String getHour() {
        return hour;
    }

    public boolean isLocalisation() {
        return localisation;
    }

    public String getPerimeter() {
        if (perimeter.equals("max")) perimeter = String.valueOf(Const.DEFAULT_ZOOM);
        return perimeter;
    }
}
