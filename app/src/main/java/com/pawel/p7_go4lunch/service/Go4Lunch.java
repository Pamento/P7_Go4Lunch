package com.pawel.p7_go4lunch.service;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

public class Go4Lunch extends MultiDexApplication {

    private static Context sContext;

    public void onCreate() {
        super.onCreate();
        Go4Lunch.sContext = getApplicationContext();
    }

    public static Context getContext() {
        return Go4Lunch.sContext;
    }
}
