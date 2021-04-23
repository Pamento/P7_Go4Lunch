package com.pawel.p7_go4lunch.utils.notification;

import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;

import java.util.List;

public class NotificationData {

    private static volatile NotificationData sNotificationData;
    private Restaurant mRestaurant;
    private List<User> mUsers;

    private NotificationData() {
        //Prevent form the reflection api.
        if (sNotificationData != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static NotificationData getInstance() {
        if (sNotificationData == null) {
            synchronized (NotificationData.class) {
                if (sNotificationData == null) sNotificationData = new NotificationData();
            }
        }
        return sNotificationData;
    }

    public Restaurant getRestaurant() {
        return mRestaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        mRestaurant = restaurant;
    }

    public List<User> getUsers() {
        return mUsers;
    }

    public void setUsers(List<User> users) {
        mUsers = users;
    }
}
