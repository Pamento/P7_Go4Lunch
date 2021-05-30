package com.pawel.p7_go4lunch.utils.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.service.AlarmService;
import com.pawel.p7_go4lunch.utils.Const;
import com.pawel.p7_go4lunch.utils.LocalAppSettings;
import com.pawel.p7_go4lunch.utils.notification.NotificationData;
import com.pawel.p7_go4lunch.utils.notification.NotificationRemainder;

import java.util.List;

import static com.pawel.p7_go4lunch.utils.Const.ALARM_MULTIPLE;
import static com.pawel.p7_go4lunch.utils.Const.ALARM_SINGLE;

public class AlarmReceiver extends BroadcastReceiver {

    private Context context;
    private final NotificationData notifData = NotificationData.getInstance();
    private LocalAppSettings mAppSettings;
    private Intent mIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        getLocalAppSettings(context);
        if (mAppSettings.isNotification()) {
            this.context = context;
            this.mIntent = intent;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                getResto(user.getUid());
            } else {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }
    }

    private void getResto(String uid) {
        FirebaseUserRepository.getInstance().getUser(uid).addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            Restaurant r = new Restaurant();
            if (user != null) r = user.getUserRestaurant();
            notifData.setRestaurant(r);
            if (r != null) getWorkmates(r.getPlaceId());
        });
    }

    private void getWorkmates(String placeId) {
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
        int alarmId = mIntent.getIntExtra(Const.ALARM_ID, -1);
        // If in Setting of this app the alarm repeating is set to false, do: cancelAlarm()
        // because the ALARM_SINGLE it run only once.
        if (!(mAppSettings.isNotif_recurrence() && alarmId == ALARM_MULTIPLE)) {
            AlarmService.cancelAlarm();
        }
        // Recurrence of notification was changed between set of alarm and display of notification
        if (mAppSettings.isNotif_recurrence() && alarmId == ALARM_SINGLE) {
            AlarmService.startRepeatedAlarm(mAppSettings.getHour());
        }
        if (!mAppSettings.isNotif_recurrence()) {
            mAppSettings.setNotification(false);
        }
    }

    private void getLocalAppSettings(Context ctx) {
        mAppSettings = new LocalAppSettings(ctx);
    }
}