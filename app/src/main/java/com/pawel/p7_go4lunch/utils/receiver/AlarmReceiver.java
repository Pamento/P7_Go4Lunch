package com.pawel.p7_go4lunch.utils.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pawel.p7_go4lunch.utils.workers.GetChosenRestaurantOfUserFromFirebase;
import com.pawel.p7_go4lunch.utils.workers.GetWorkmatesForChosenRestoFromFirebase;
import com.pawel.p7_go4lunch.utils.workers.ShowNotificationRemainder;

import static com.pawel.p7_go4lunch.utils.Const.KEY_USER_ID;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            WorkManager manager = WorkManager.getInstance(context);
            OneTimeWorkRequest getChosenRestoFromFirebase = new OneTimeWorkRequest.Builder(GetChosenRestaurantOfUserFromFirebase.class)
                    .setInputData(getUserId(user))
                    .build();
            WorkContinuation workContinuation = manager.beginWith(getChosenRestoFromFirebase);

            OneTimeWorkRequest getWorkmatesFromFirebase = new OneTimeWorkRequest.Builder(GetWorkmatesForChosenRestoFromFirebase.class).build();
            workContinuation = workContinuation.then(getWorkmatesFromFirebase);
            // TODO how to pass data to display in notif ?
            OneTimeWorkRequest showNotif = new OneTimeWorkRequest.Builder(ShowNotificationRemainder.class).build();
            workContinuation = workContinuation.then(showNotif);
            workContinuation.enqueue();
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private Data getUserId(FirebaseUser user) {
        Data.Builder builder = new Data.Builder();
        if (user != null) {
            builder.putString(KEY_USER_ID, user.getUid());
        }
        return builder.build();
    }
}