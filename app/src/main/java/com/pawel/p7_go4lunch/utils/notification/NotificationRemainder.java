package com.pawel.p7_go4lunch.utils.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.pawel.p7_go4lunch.R;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.Const;

import java.util.List;

import static com.pawel.p7_go4lunch.utils.Const.CHANNEL_ID;

public class NotificationRemainder extends ContextWrapper {

    private final Context mContext;
    private final CharSequence nTicker;
    private final CharSequence intro;
    private final CharSequence noWorkmates;
    private final NotificationData nData;
    private String restoName;
    private String restoAdress;
    private final StringBuilder workmatesList = new StringBuilder();

    public NotificationRemainder(Context base) {
        super(base);
        mContext = base;
        nTicker = mContext.getString(R.string.app_name);
        intro = mContext.getString(R.string.notification_intro);
        noWorkmates = mContext.getString(R.string.notification_no_workmates_go);
        nData = NotificationData.getInstance();
        setNotifContent();
    }

    private void setNotifContent() {
        Restaurant r = nData.getRestaurant();
        restoName = r.getName();
        restoAdress = r.getAddress();
        if (!nData.getUsers().isEmpty()) {
            List<User> users = nData.getUsers();
            workmatesList.append(restoAdress).append("\n")
                    .append(intro).append("\n");
            for (User u : users) {
                workmatesList.append(u.getName()).append("\n");
            }
        }
    }

    public void showNotification() {
        CharSequence workmates = workmatesList.length() > 0 ? workmatesList : noWorkmates;

        if (Build.VERSION.SDK_INT >= 26) {
            CharSequence name = Const.VERBOSE_NOTIF_CHANNEL_NAME;
            String description = Const.VERBOSE_NOTIF_CHANNEL_DESCRIPT;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager nMgr = mContext.getSystemService(NotificationManager.class);
            if (nMgr != null) {
                nMgr.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_go4lunch).setTicker(nTicker)
                .setContentTitle(restoName)
                .setContentText(restoAdress)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(workmates))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[0]);

        NotificationManagerCompat.from(mContext).notify(Const.NOTIF_ID, builder.build());
    }
}
