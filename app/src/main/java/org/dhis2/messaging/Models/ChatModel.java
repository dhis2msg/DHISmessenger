package org.dhis2.messaging.Models;

import com.google.gson.annotations.SerializedName;

public class ChatModel {
    @SerializedName("text")
    public String message;
    @SerializedName("lastUpdated")
    public String date;
    @SerializedName("user")
    public NameAndIDModel user;
    public String time;

    public ChatModel(String message, String date, NameAndIDModel user) {
        super();
        this.message = message;
        this.date = convertDate(date);
        this.user = user;
        this.time = convertTime(date);
    }

    public String convertDate(String d) {
        if (d.length() > 10) {
            d = d.substring(0, 10);
            String[] tmp = d.split("-");
            d = tmp[2] + "." + tmp[1] + "." + tmp[0];
        }
        return d;
    }

    public String convertTime(String d) {
        if (d.length() > 20) {
            String[] tmp = d.split("T");
            d = tmp[1].substring(0, 5);
        }
        return d;
    }
}
