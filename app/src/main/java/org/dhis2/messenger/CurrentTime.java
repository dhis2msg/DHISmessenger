package org.dhis2.messenger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by iNick on 06.11.14.
 */
public class CurrentTime {
    public String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        return sdf.format(cal.getTime());
    }
}
