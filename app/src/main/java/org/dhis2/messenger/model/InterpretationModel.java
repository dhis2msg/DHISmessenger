package org.dhis2.messenger.model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InterpretationModel implements CopyAttributes<InterpretationModel>{
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

    public InterpretationModel(String id, String text, String date, NameAndIDModel user,
                               String type, String pictureUrl, Bitmap picture, List<ChatModel> comments) {
        this.id = id;
        this.text = text;
        this.date = convertDate(date);
        this.user = user;
        this.type = type;
        this.pictureUrl = pictureUrl;
        this.picture = picture;
        this.comments = comments;
    }
    /**
     * Updates this InterpretationModel's fields to another model's fields.
     * This is currently used by RESTSessionStorage, when the cache has received the same page.
     * Messages and Members are the caches for the chat conversations and must be preserved.
     * Thus update the other fields instead. :) + it's shorter than replacing the old one in the arrayList.
     * @param other
     */
    public void copyAttributesFrom(InterpretationModel other) {

        this.text = other.text;
        this.date = other.date;
        this.user = other.user;
        this.type = other.type;
        this.pictureUrl = other.pictureUrl;
        //TODO : find out if I need to replace the picture. and when ?!
        this.picture = other.picture;
        this.comments = other.comments;
    }

    public String convertDate(String d) {
        d = d.substring(0, 10);
        d = d.replaceAll("\\D+", ".");
        return d;
    }
}
