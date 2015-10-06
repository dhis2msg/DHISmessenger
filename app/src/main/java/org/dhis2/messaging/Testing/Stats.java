package org.dhis2.messaging.Testing;

import android.app.ActivityManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dhis2.messaging.R;

/**
 * Created by iNick on 14.02.15.
 */
public class Stats extends Fragment {
    TextView dhis_messages, im_messages, im_conference_messages,
            online, notifications;//, dataUse, dataSent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_stats, container, false);
        dhis_messages = (TextView) view.findViewById(R.id.dhisMessages);
        im_messages = (TextView) view.findViewById(R.id.IMmessages);
        im_conference_messages = (TextView) view.findViewById(R.id.conferenceCount);
        online = (TextView) view.findViewById(R.id.onlineCount);
        notifications = (TextView) view.findViewById(R.id.notificationCount);
        //dataUse = (TextView) view.findViewById(R.id.totalDatause);
        //dataSent = (TextView) view.findViewById(R.id.totalSent);

        setData();
        return view;
    }

    public void setData() {
        SaveDataSqlLite data = new SaveDataSqlLite(getActivity());
        data.open();
        Cursor c = data.getDataRaw();
        c.moveToFirst();
        dhis_messages.setText(c.getString(c.getColumnIndex(data.DHIS_MESSAGE_SENDT_COUNT)));
        im_messages.setText(c.getString(c.getColumnIndex(data.IM_MESSAGE_SENDT_COOUNT)));
        im_conference_messages.setText(c.getString(c.getColumnIndex(data.IM_DISCUSSION_MESSAGE_SENDT_COOUNT)));

        int seconds = Integer.parseInt(c.getString(c.getColumnIndex(data.ONLINE_COUNT)));
        online.setText(convertToSMH(seconds));
        notifications.setText(c.getString(c.getColumnIndex(data.NOTIFICATION_COUNT)));
        data.close();
        c.close();

    }

    public String convertToSMH(int totalSeconds) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;

        String builder = "";
        if (hours > 0) {
            builder = "Hours: " + hours;
        }
        if (minutes > 0) {
            builder += " Minutes: " + minutes;
        }
        if (seconds > 0) {
            builder += " Seconds: " + seconds;
        }
        if (builder.equals(""))
            return "0";

        return builder;
    }
}
