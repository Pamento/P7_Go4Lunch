package com.pawel.p7_go4lunch.service;

import android.app.Application;
import android.content.Context;

public class Go4Lunch extends Application {

    private static Context sContext;

    public void onCreate() {
        super.onCreate();
        Go4Lunch.sContext = getApplicationContext();
    }

    public static Context getContext() {
        return Go4Lunch.sContext;
    }
}
