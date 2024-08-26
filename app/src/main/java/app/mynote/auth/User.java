package app.mynote.auth;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class User {
    @SerializedName("sub")
    private UUID id;
    @SerializedName("name")
    private String name;
    @SerializedName("preferred_username")
    private String preferredUsername;
    @SerializedName("given_name")
    private String givenName;
    @SerializedName("email")
    private String email;

    public String getGivenName() {
        return this.givenName;
    }

    public String getUserEmail() {
        return this.email;
    }

    public String getId() {
        return this.id.toString();
    }
}
