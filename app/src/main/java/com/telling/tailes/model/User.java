package com.telling.tailes.model;

import com.telling.tailes.util.AuthUtils;

import java.util.ArrayList;

public class User {

    private String id;
    private String username;
    private String hashedPassword;
    private String salt;
    private ArrayList<String> follows;
    private int stories;
    private int loves;

    public User(){}

    public User(String username, String hashedPassword, String salt, ArrayList<String> follows, int stories, int loves)
    {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.follows = follows;
        this.stories = stories;
        this.loves = loves;
    }

    public String getUsername() { return username; }

    public String getHashedPassword() { return hashedPassword; }

    public String getSalt() { return salt; }

    public ArrayList<String> getFollows() {
        //TODO: undo this workaround - this is since we have no follow data, otherwise it will crash
       if(follows == null)  { return new ArrayList<>(); }
        return follows; }

    public int getStories() { return stories; }

    public void incrementStoryCount() {
        stories += 1;
    }

    public void decrementStoryCount() {
        stories -= 1;
    }

    public int getLoves() { return loves; }

    public void incrementLoveCount() {
        loves += 1;
    }

    public void decrementLoveCount() {
        loves -= 1;
    }

    //Return true if this password is the user's cleartext password, false otherwise
    //TODO: Note that this is the function we want to "override" in order to enable "passwordless" login - just return true always
    public boolean checkPassword(String password) {
        return AuthUtils.hashPassword(password,this.salt).equals(this.hashedPassword);
    }
}
