package org.dhis2.messenger.core.gcm;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;

import java.util.List;

/**
 * Created by iNick on 04.10.14.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;

        if (componentInfo.getPackageName().equalsIgnoreCase("org.dhis2.messenger")) {
            Bundle extras = intent.getExtras();

            if (!extras.isEmpty()) {
                if (!extras.getString("sender").equals(SharedPrefs.getUserName(context))) {

                    if (componentInfo.getClassName().equals("org.dhis2.messenger.gui.activity.RESTChatActivity")) {
                        Intent i = new Intent("org.dhis2.messenger.gui.activity.RESTChatActivity");
                        context.sendBroadcast(i);
                    } else {
                        new ToastMaster(context, "New " + extras.getString("type") + " \nFrom " + extras.getString("sender"), true);

                        if (componentInfo.getClassName().equals("org.dhis2.messenger.gui.activity.HomeActivity")) {
                            Intent i = new Intent("org.dhis2.messenger.gui.activity.HomeActivity");
                            context.sendBroadcast(i);
                        }
                    }
                }
            }
        } else {
            ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
    }
}
