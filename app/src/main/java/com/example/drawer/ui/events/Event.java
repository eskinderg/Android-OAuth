package com.example.drawer.ui.events;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Event implements Serializable {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("complete")
    @Expose
    private boolean complete;

    @SerializedName("userId")
    @Expose
    private String userId;

    public String getTitle() {
        return this.title;
    }

    public boolean getIsComplete() {
        return this.complete;
    }

    public void setIsComplete(boolean value) {
        this.complete = value;
    }

    public int getEventId() {
        return this.id;
    }

}
