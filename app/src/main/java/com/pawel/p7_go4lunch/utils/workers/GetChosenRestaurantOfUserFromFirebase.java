package com.pawel.p7_go4lunch.utils.workers;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.notification.NotificationData;

import static com.pawel.p7_go4lunch.utils.Const.KEY_RESTO_ID;
import static com.pawel.p7_go4lunch.utils.Const.KEY_USER_ID;

public class GetChosenRestaurantOfUserFromFirebase extends Worker {
    private static final String TAG = "NOTIF";
    private static Restaurant r;

    public GetChosenRestaurantOfUserFromFirebase(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = getInputData().getString(KEY_USER_ID);
        if (userId == null) Result.failure();
        NotificationData notifData = NotificationData.getInstance();
        Log.i(TAG, "doWork: getResto  userID " + userId);
        String restoID = "";
        FirebaseUserRepository.getInstance().
                getUser(userId).addOnSuccessListener(documentSnapshot -> {
            Log.i(TAG, "RESTO ___1____doWork: ");
            User user = documentSnapshot.toObject(User.class);
            Restaurant r = user.getUserRestaurant();
            notifData.setRestaurant(r);
            Data d = new Data.Builder().putString(KEY_RESTO_ID, r.getPlaceId()).build();
            Log.i(TAG, "RESTO ___2____doWork: restoID: << " + restoID + " >>//////");
        });
        // TODO put the data in this way will work ?
        // TODO Result run before Firebase giv back data !!!
        return Result.success();
    }
}
