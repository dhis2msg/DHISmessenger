package org.dhis2.messenger.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class InterpretationModel implements CacheListElement<InterpretationModel> {
    @SerializedName("id")
    public String id;
    @SerializedName("text")
    public String text;
    @SerializedName("lastUpdated")
    public String date;
    @SerializedName("user")
    public NameAndIDModel user;
    @SerializedName("type")
    public String type;
    @SerializedName("comments")
    public List<ChatModel> comments;
    public String pictureUrl;
    public Bitmap picture;
    private Date dateObj;
    private boolean read;

    public InterpretationModel(String id, String text, String date, NameAndIDModel user,
                               String type, String pictureUrl, Bitmap picture, List<ChatModel> comments, boolean read) {
        this.id = id;
        this.text = text;
        this.date = convertDate(date);
        this.user = user;
        this.type = type;
        this.pictureUrl = pictureUrl;
        this.picture = picture;
        this.comments = comments;
        this.read = read;
        //example of the argument date: "2015-12-06T21:59:27.687+0000" currently named "lastUpdated" in the api
        try {
            this.dateObj = InboxModel.formater.parse(date);
        } catch (java.text.ParseException e) {
            Log.e("InboxModel-date", "Got an exception while trying to parse the date string to a date object.");
            e.printStackTrace();
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean getRead() {
        return read;
    }

    /**
     * Updates this InterpretationModel's fields to another model's fields.
     * This is currently used by RESTSessionStorage, when the cache has received the same page.
     * Messages and Members are the caches for the chat conversations and must be preserved.
     * Thus update the other fields instead. :) + it's shorter than replacing the old one in the arrayList.
     * @param other
     */
    public boolean copyAttributesFrom(InterpretationModel other) {
        boolean changed = false;
        if (this.dateObj.before(other.dateObj)) {
            changed = true;
            this.read = false;
        }
        this.text = other.text;
        this.dateObj = other.dateObj;
        this.date = other.date;
        this.user = other.user;
        this.type = other.type;
        this.pictureUrl = other.pictureUrl;
        //TODO : find out if I need to replace the picture. and when ?!
        this.picture = other.picture;
        this.comments = other.comments;
        return changed;
    }

    public String convertDate(String d) {
        d = d.substring(0, 10);
        d = d.replaceAll("\\D+", ".");
        return d;
    }

    /**
     * In order for the Array list to detect duplicates.
     * Each InterpretationModel has a unique id given by the server.
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof InterpretationModel)) {
            return false;
        }
        return this.id.equals(((InterpretationModel) other).id);
    }
}
