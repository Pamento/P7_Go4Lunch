package com.pawel.p7_go4lunch.utils.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pawel.p7_go4lunch.dataServices.repositorys.FirebaseUserRepository;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.notification.NotificationData;

import java.util.List;

import static com.pawel.p7_go4lunch.utils.Const.KEY_RESTO_ID;

public class GetWorkmatesForChosenRestoFromFirebase extends Worker {
    private static final String TAG = "NOTIF";

    public GetWorkmatesForChosenRestoFromFirebase(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationData notifData = NotificationData.getInstance();
        String restoId = getInputData().getString(KEY_RESTO_ID);
        Log.i(TAG, "doWork: Workmates:: restoId: " + restoId);
        if (restoId == null) return Result.failure();
        FirebaseUserRepository.getInstance()
                .getUsersWithTheSameRestaurant(restoId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> usersList = queryDocumentSnapshots.toObjects(User.class);
                    notifData.setUsers(usersList);
                });
        return Result.success();
    }
}
