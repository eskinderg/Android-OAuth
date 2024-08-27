package app.mynote.fragments.note;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Note implements Serializable {
    @SerializedName("header")
    @Expose
    private String header;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("userId")
    @Expose
    private String userId;

    @SerializedName("colour")
    @Expose
    private String colour;

    @SerializedName("height")
    @Expose
    private Number height;

    @SerializedName("width")
    @Expose
    private Number width;

    @SerializedName("left")
    @Expose
    private Number left;

    @SerializedName("top")
    @Expose
    private Number top;

    @SerializedName("selection")
    @Expose
    private String selection;

    @SerializedName("archived")
    @Expose
    private boolean archived;

    @SerializedName("pinned")
    @Expose
    private boolean pinned;

    @SerializedName("favorite")
    @Expose
    private boolean favorite;

    @SerializedName("active")
    @Expose
    private boolean active;

    @SerializedName("spellCheck")
    @Expose
    private boolean spellCheck;

    @SerializedName("pinOrder")
    @Expose
    private String pinOrder;

    @SerializedName("dateCreated")
    @Expose
    private String dateCreated;

    @SerializedName("dateModified")
    @Expose
    private String dateModified;

    @SerializedName("dateArchived")
    @Expose
    private String dateArchived;

    @SerializedName("dateSync")
    @Expose
    private String dateSync;

    @SerializedName("owner")
    @Expose
    private String owner;

    @SerializedName("text")
    @Expose
    private String text;

    public Note() {
    }

    public String getDateSync() {
        return dateSync;
    }

    public void setDateSync(String dateSync) {
        this.dateSync = dateSync;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean getArchived() {
        return this.archived;
    }

    public void setArchived(boolean value) {
        this.archived = value;
    }

    public String getDateArchived() {
        return this.dateArchived;
    }

    public void setDateArchived(String dateArchived) {
        this.dateArchived = dateArchived;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isFavorite() {
        return this.favorite;
    }

    public void setFavorite(boolean value) {
        this.favorite = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelection() {
        return this.selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public boolean getPinned() {
        return this.pinned;
    }


    public String getPinOrder() {
        return this.pinOrder;
    }

    public void setPinOrder(String pinOrder) {
        this.pinOrder = pinOrder;
    }

    public boolean isPinned() {
        return this.pinned;
    }

    public void setPinned(boolean value) {
        this.pinned = value;
    }

    public boolean getSpellCheck() {
        return this.spellCheck;
    }

    public void setSpellCheck(Boolean spellCheck) {
        this.spellCheck = spellCheck;
    }

    public boolean getActive() {
        return this.active;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
