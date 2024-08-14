package com.example.drawer.auth;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("name")
    private String name;
    @SerializedName("preferred_username")
    private String preferredUsername;
    @SerializedName("given_name")
    private String givenName;
    @SerializedName("email")
    private String email;

    public User(String name, String preferredUsername, String givenName, String email) {
        this.name = name;
        this.preferredUsername = preferredUsername;
        this.givenName = givenName;
        this.email = email;
    }

    public String getGivenName() {
        return this.givenName;
    }

    public String getUserEmail() {
        return this.email;
    }
}
