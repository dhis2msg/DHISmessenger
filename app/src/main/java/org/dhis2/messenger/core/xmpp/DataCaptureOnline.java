package org.dhis2.messenger.core.xmpp;

import android.util.TimeFormatException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by iNick on 14.02.15.
 */
public class DataCaptureOnline {
    public static String onlineSince;
    SimpleDateFormat format;

    public DataCaptureOnline() {
        format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        onlineSince = format.format(Calendar.getInstance().getTime());
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l / 1000; //For seconds
    }

    public int stopDateAndGetRatio() {
        String timeOffline = format.format(Calendar.getInstance().getTime());
        long ratio = 0;
        try {
            Date dateOnline = format.parse(onlineSince);
            Date dateOffline = format.parse(timeOffline);
            ratio = dateOffline.getTime() - dateOnline.getTime();
            return safeLongToInt(ratio);

        } catch (TimeFormatException e) {

        } catch (ParseException e) {

        }
        return 0;
    }
}
