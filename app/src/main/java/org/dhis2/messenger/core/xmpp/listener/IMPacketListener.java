package org.dhis2.messenger.core.xmpp.listener;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import org.dhis2.messenger.CurrentTime;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;
import org.dhis2.messenger.gui.activity.HomeActivity;
import org.dhis2.messenger.model.IMMessageModel;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.delay.packet.DelayInformation;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by iNick on 14.11.14.
 */
public class IMPacketListener extends IntentService implements StanzaListener {

    public static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;
    private PendingIntent contentIntent;
    private Context context;

    public IMPacketListener() {
        super("Chat listener");
    }

    public IMPacketListener(Context context) {
        super("Chat listener");
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(context, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
        IMMessageModel messageModel = null;
        String JID = "";
        Message message = (Message) packet;
        DelayInformation inf;
        if (message.getBody() != null) {
            String username = message.getFrom();
            String sub[] = username.split("/");
            JID = sub[0];
            inf = message.getExtension("x", "jabber:x:delay");
            if (inf != null) {
                messageModel = new IMMessageModel(message.getBody(), JID, new SimpleDateFormat("yyyy.MM.dd HH:mm").format(inf.getStamp()));
            } else {
                messageModel = new IMMessageModel(message.getBody(), JID, new CurrentTime().getCurrentTime());

            }
        }
        XMPPSessionStorage.getInstance().addMessage(JID, messageModel);

        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;

        if (!componentInfo.getPackageName().equalsIgnoreCase("org.dhis2.messenger"))
            sendNotification(messageModel);
        else if (componentInfo.getClassName().equalsIgnoreCase("org.dhis2.messenger.gui.activity.IMChatActivity"))
            if (!JID.equals(XMPPSessionStorage.getInstance().JID)) {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }
    }

    private void sendNotification(IMMessageModel model) {
        String[] tmp = model.JID.split("@");
        String username = tmp[0];
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New chat message")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(username + ": " + model.text))
                        .setAutoCancel(true)
                        .setContentText(username + ": " + model.text);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setSmallIcon(R.drawable.original);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

}
