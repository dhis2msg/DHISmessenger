package org.dhis2.messenger.core.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.dhis2.messenger.gui.activity.HomeActivity;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.SaveDataSqlLite;
import org.dhis2.messenger.SharedPrefs;

/**
 * Created by iNick on 04.10.14.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (SharedPrefs.getCredentials(this) != null) {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String messageType = gcm.getMessageType(intent);

            if (!extras.isEmpty()) {
                if (extras.getString("sender") != SharedPrefs.getUserName(this)) {
                    if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                        //sendNotification("Send error: " + extras.toString());
                    } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                        //sendNotification("Deleted messages on server: " + extras.toString());
                    } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                        sendNotification(extras.getString("type"), "New message from " + extras.getString("sender"));
                    }
                }
            }
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void sendNotification(String title, String msg) {


        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title) //"DHIS Messenger")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setAutoCancel(true)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setSmallIcon(R.drawable.original);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SaveDataSqlLite db = new SaveDataSqlLite(this);
        db.open();
        db.updateNotificationCount();
        db.close();
    }
}

