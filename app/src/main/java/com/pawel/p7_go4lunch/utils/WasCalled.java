package com.pawel.p7_go4lunch.utils;

public final class WasCalled {

    private static boolean restaurantListCalled = false;
    private static boolean locationWasCalled = false;
    private static int iterator1 = 0;
    private static int iterator2 = 0;

    public static boolean restaurantsList() {
        if (iterator1 == 0) {
            iterator1 = 1;
            return restaurantListCalled;
        }
        restaurantListCalled = true;
        return restaurantListCalled;
    }

    public static boolean isLocationWasCalled() {
        if (iterator2 == 0) {
            iterator2 = 1;
            return locationWasCalled;
        }
        locationWasCalled = true;
        return locationWasCalled;
    }

    public static boolean resetLocationWasCalled() {
        locationWasCalled = false;
        iterator2 = 0;
        return !locationWasCalled && iterator2 == 0;
    }
}
