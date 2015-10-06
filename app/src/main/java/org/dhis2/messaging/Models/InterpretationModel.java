package org.dhis2.messaging.Models;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InterpretationModel {
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

    public String convertDate(String d) {
        d = d.substring(0, 10);
        d = d.replaceAll("\\D+", ".");
        return d;
    }
}
