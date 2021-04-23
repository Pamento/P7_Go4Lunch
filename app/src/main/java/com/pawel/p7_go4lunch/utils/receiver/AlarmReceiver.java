package com.pawel.p7_go4lunch.utils.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.service.AlarmService;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.notification.NotificationData;
import com.pawel.p7_go4lunch.utils.notification.NotificationRemainder;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "NOTIF";

    private Context context;
    private final NotificationData notifData = NotificationData.getInstance();
    private LocalAppSettings mAppSettings;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i(TAG, "onReceive: user " + user);
        if (user != null) {
            getResto(user.getUid());
            getLocalAppSettings(context);
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private void getResto(String uid) {
        Log.i(TAG, "getResto: RUN");
        FirebaseUserRepository.getInstance().getUser(uid).addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) Log.i(TAG, "getResto: user::: " + user.getUid());
            Restaurant r = user.getUserRestaurant();
            if (r != null) Log.i(TAG, "getResto: resto " + r.getPlaceId());
            notifData.setRestaurant(r);
            getWorkmates(r.getPlaceId());
            Log.i(TAG, "getResto: after:::getWorkmates()");
        });
    }

    private void getWorkmates(String placeId) {
        Log.i(TAG, "getWorkmates: RUN");
        FirebaseUserRepository.getInstance().getUsersWithTheSameRestaurant(placeId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> usersList = queryDocumentSnapshots.toObjects(User.class);
                    notifData.setUsers(usersList);
                    showNotifRemainder();
                });
    }

    private void showNotifRemainder() {
        NotificationRemainder notif = new NotificationRemainder(context);
        notif.showNotification();
        checkForNextNotif();
    }

    private void checkForNextNotif() {
        AlarmService.cancelAlarm();
        if (!mAppSettings.isNotif_recurrence()) {
            mAppSettings.setNotification(false);
        } else {
            AlarmService.startAlarm(mAppSettings.getHour());
        }
    }

    private void getLocalAppSettings(Context ctx) {
        mAppSettings = new LocalAppSettings(ctx);
    }
}