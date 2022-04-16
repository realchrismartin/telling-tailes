package com.telling.tailes.model;

import com.telling.tailes.util.AuthUtils;

import java.util.ArrayList;

public class User {

    private String id;
    private String username;
    private String hashedPassword;
    private String salt;
    private ArrayList<String> bookmarks;


    public User(){}

    public User (String username, ArrayList<String> bookmarks) { //TODO: add followedAuthors array list? this is another ticket
        this.username = username;
        this.bookmarks = bookmarks;
    }

    public User(String username, String hashedPassword, String salt)
    {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
    }


    public String getUsername() { return username; }

    public String getHashedPassword() { return hashedPassword; }

    public String getSalt() { return salt; }

    //Return true if this password is the user's cleartext password, false otherwise
    //TODO: Note that this is the function we want to "override" in order to enable "passwordless" login - just return true always
    public boolean checkPassword(String password) {
        return AuthUtils.hashPassword(password,this.salt).equals(this.hashedPassword);
    }

    public ArrayList<String> getBookmarks() {return bookmarks;}

    public void setBookmarks(ArrayList<String> bookmarks) {
            this.bookmarks = bookmarks;
    } 

}
