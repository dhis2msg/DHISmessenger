package org.dhis2.messenger.model;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class InboxModel implements Comparable<InboxModel>, CopyAttributes<InboxModel> {
    private String subject;
    private String date;
    private String id;
    private String lastSender;
    private String time;
    private boolean read;
    private Date dateObj;
    public ArrayList<ChatModel> messages = new ArrayList<>();
    public ArrayList<NameAndIDModel> members = new ArrayList<>();

    public static SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


    public InboxModel(String subject, String date, String id, String lastSender, boolean read) {
        this.subject = subject;
        this.date = convertDate(date);
        this.id = id;
        this.read = read;
        this.lastSender = lastSender;
        this.time = convertTime(date);

        //example of the argument date: "2015-12-06T21:59:27.687+0000" currently named "lastUpdated" in the api
        try {
            this.dateObj = InboxModel.formater.parse(date);
        } catch (java.text.ParseException e) {
            Log.e("InboxModel-date", "Got an exception while trying to parse the date string to a date object.");
            e.printStackTrace();
        }
    }

    /**
     * Updates this inboxModel's fields to another model's fields.
     * This is currently used by RESTSessionStorage, when the cache has received the same page.
     * Messages and Members are the caches for the chat conversations and must be preserved.
     * Thus update the other fields instead. :) + it's shorter than replacing the old one in the arrayList.
     * @param other
     * @return true if changed
     */
    public boolean copyAttributesFrom(InboxModel other) {
        boolean changed = false;
        this.subject = other.subject;
        if (this.dateObj.before(other.dateObj)) {
            changed = true;
            this.read = false;
        }
        this.dateObj = other.dateObj;
        this.date = other.date;
        this.lastSender = other.lastSender;
        this.time = other.time;
        return changed;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getDate() {
        return this.date;
    }

    public String getId() {
        return this.id;
    }

    public String getTime() {
        return this.time;
    }

    public String getLastSender() {
        return this.lastSender;
    }

    public boolean getRead() {
        return this.read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String convertDate(String d) {
        d = d.substring(0, 10);
        String[] tmp = d.split("-");
        d = tmp[2] + "." + tmp[1] + "." + tmp[0];
        return d;
    }

    public String convertTime(String d) {
        if (d.length() > 20) {
            String[] tmp = d.split("T");
            d = tmp[1].substring(0, 5);
        }
        return d;
    }

    public int compareTo(InboxModel model) {
        if (!read && model.read) {
            return -1;
        }
        if (read && !model.read) {
            return 1;
        }
        return 0;
    }

    /**
     * In order for the Array list to detect duplicates.
     * Each InboxModel has a unique id given by the server.
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof InboxModel)) {
            return false;
        }
        return this.id.equals(((InboxModel) other).getId());
    }
}
