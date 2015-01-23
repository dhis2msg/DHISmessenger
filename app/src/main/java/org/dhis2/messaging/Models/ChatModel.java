package org.dhis2.messaging.Models;

import com.google.gson.annotations.SerializedName;

public class ChatModel {
    @SerializedName("text")
	public String message;
    @SerializedName("lastUpdated")
	public String date;
    @SerializedName("user")
    public NameAndIDModel user;

	public ChatModel(String message,String date, NameAndIDModel user) {
		super();
		this.message = message;
		this.date = convertDate(date);
        this.user = user;
	}
    public String convertDate(String d) {
        if(d.length() > 8) {
            d = d.substring(0, 10);
            d = d.replaceAll("\\D+", ".");
        }
        return d;
    }
}
