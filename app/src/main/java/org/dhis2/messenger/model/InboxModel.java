package org.dhis2.messenger.model;

import java.util.ArrayList;

public class InboxModel implements Comparable<InboxModel> {
    private final String subject;
    private final String date;
    private final String id;
    private final String lastSender;
    private String time;
    private boolean read;
    public ArrayList<ChatModel> messages = new ArrayList<>();
    public ArrayList<NameAndIDModel> members = new ArrayList<>();

    public InboxModel(String subject, String date, String id, String lastSender, boolean read) {
        this.subject = subject;
        this.date = convertDate(date);
        this.id = id;
        this.read = read;
        this.lastSender = lastSender;
        this.time = convertTime(date);
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

}
