package com.pawel.p7_go4lunch.utils.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pawel.p7_go4lunch.utils.notification.NotificationRemainder;

public class ShowNotificationRemainder extends Worker {

    public ShowNotificationRemainder(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        NotificationRemainder notif = new NotificationRemainder(context);
        notif.showNotification();
        return Result.success();
    }
}
