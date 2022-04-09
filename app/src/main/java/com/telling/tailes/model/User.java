package com.telling.tailes.model;

public class User {

    private String id;
    private String username;
    private String hashedPassword;

    public User(String username, String hashedPassword)
    {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() { return username; }
}
