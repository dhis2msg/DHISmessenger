package org.dhis2.messaging.Models;

/**
 * Created by iNick on 14.11.14.
 */
public class IMMessageModel {
    public String text, JID, date;

    public IMMessageModel(String text, String JID, String date) {
        this.text = text;
        this.JID = JID;
        this.date = date;
    }
}
