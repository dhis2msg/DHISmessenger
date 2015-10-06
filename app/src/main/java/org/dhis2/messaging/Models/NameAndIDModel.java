package org.dhis2.messaging.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by iNick on 29.09.14.
 */
public class NameAndIDModel {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
