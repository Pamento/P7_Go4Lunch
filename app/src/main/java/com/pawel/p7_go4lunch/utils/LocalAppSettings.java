package com.pawel.p7_go4lunch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class LocalAppSettings {

    private boolean notification;
    private String hour;
    private boolean localisation;
    private String perimeter;
    private boolean notif_recurrence;
    private final SharedPreferences prefs;

    public LocalAppSettings(Activity activity) {
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        initSharedPref();
    }

    public LocalAppSettings(Context ctx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        initSharedPref();
    }

    private void initSharedPref() {
        notification = prefs.getBoolean("notification", false);
        hour = prefs.getString("hour", "12");
        localisation = prefs.getBoolean("localisation", true);
        perimeter = prefs.getString("perimeter", "500");
        notif_recurrence = prefs.getBoolean("repeat", false);
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

    public int getRadius() {
        int rValue = 500;
        if (perimeter.equals("max")) return 6000;
        else {
            try {
                rValue = Integer.parseInt(perimeter);
            } catch (NumberFormatException ne) {
                ne.getStackTrace();
            }
        }
        return rValue;
    }

    public float getPerimeter() {
        if (perimeter.equals("max")) return Const.DEFAULT_ZOOM;
        else return 15f;
    }

    public boolean isNotif_recurrence() {
        return notif_recurrence;
    }

    public void setNotification(boolean notification) {
        prefs.edit().putBoolean( "notification", notification).apply();
    }

    public void setNotif_recurrence(boolean notif_recurrence) {
        prefs.edit().putBoolean("repeat", notif_recurrence).apply();
    }
}
