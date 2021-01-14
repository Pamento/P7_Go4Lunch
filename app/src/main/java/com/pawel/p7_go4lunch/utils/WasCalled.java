package com.pawel.p7_go4lunch.utils;

public abstract class WasCalled {

    private static boolean iWasCalled = false;
    private static int iterator = 0;

    public static boolean isWasCalled() {
        if (iterator == 0) {
            iterator = 1;
            return iWasCalled;
        }
        iWasCalled = true;
        return iWasCalled;
    }
}
