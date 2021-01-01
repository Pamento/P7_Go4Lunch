package com.pawel.p7_go4lunch.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class LocalAppSettings {

    private final boolean notification;
    private final String hour;
    private final boolean localisation;
    private final String perimeter;


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

    private int getRadius() {
        if (perimeter.equals("max")) return 6000;
        else return Integer.getInteger(perimeter);
    }
    public float getPerimeter() {
        if (perimeter.equals("max")) return Const.DEFAULT_ZOOM;
        else return 15f;
    }
}
