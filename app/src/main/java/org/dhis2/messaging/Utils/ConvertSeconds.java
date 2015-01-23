package org.dhis2.messaging.Utils;

import java.util.concurrent.TimeUnit;

public class ConvertSeconds {

    public String convertToDHM(long sec) {
        int days = (int) TimeUnit.SECONDS.toDays(sec);
        long hours = TimeUnit.SECONDS.toHours(sec) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(sec) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours);

        String back = "";
        if (days > 0)
            back = days + "d ";
        else {
            if (hours > 0) {
                back += hours + "h ";
            }
            if (minutes > 0) {
                back += minutes + "m ";
            }
        }
        return back;
    }
}
