package com.telling.tailes.model;

import java.util.ArrayList;

public class User {

    private String username;
    private String hashedPassword;
    private String salt;
    private String messagingToken;

    private ArrayList<String> follows;
    private ArrayList<String> followers;
    private ArrayList<String> bookmarks;

    private int stories;
    private int loves;
    private int profileIcon;


    public User(){}

    public User (String username, ArrayList<String> bookmarks) {
        this.username = username;
        this.bookmarks = bookmarks;
    }

    public User(String username, int profileIcon,  String hashedPassword, String salt, ArrayList<String> follows, ArrayList<String> followers, int stories, int loves, String messagingToken)

    {
        this.username = username;
        this.profileIcon = profileIcon;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.follows = follows;
        this.followers = followers;
        this.stories = stories;
        this.loves = loves;
        this.messagingToken = messagingToken;
    }


    public String getUsername() { return username; }

    public int getProfileIcon() { return profileIcon; }

    public String getHashedPassword() { return hashedPassword; }

    public String getSalt() { return salt; }

    public String getMessagingToken() { return messagingToken; }

    public void setMessagingToken(String messagingToken) { this.messagingToken = messagingToken; }

    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public void setSalt(String salt) { this.salt = salt; }

    public ArrayList<String> getFollows() {
        initFollows();
        return follows;
    }

    public ArrayList<String> getFollowers() {
        initFollowers();
        return followers;
    }

    //Add or remove the specified user as a follow, depending on whether they are present
    public void updateFollows(String usernameToFollow) {
        initFollows();
       if(follows.contains(usernameToFollow)) {
           follows.remove(usernameToFollow);
       } else {
           follows.add(usernameToFollow);
       }
    }

    //Add or remove the specified user as a follower, depending on whether they are present
    public void updateFollowers(String usernameWhoIsFollowing) {
        initFollowers();

        if(followers.contains(usernameWhoIsFollowing)) {
            followers.remove(usernameWhoIsFollowing);
        } else {
            followers.add(usernameWhoIsFollowing);
        }
    }

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
    public boolean checkPassword(String password) {
        //return AuthUtils.hashPassword(password,this.salt).equals(this.hashedPassword); //Note: Uncomment this to re-enable password authentication
        return true; //Comment this out
    }

    public ArrayList<String> getBookmarks() {
        initBookmarks();
        return bookmarks;
    }

    public void setBookmarks(ArrayList<String> bookmarks) { this.bookmarks = bookmarks;
    }

    private void initBookmarks() {
        if (bookmarks == null) {
            bookmarks = new ArrayList<String>();
        }
    }

    private void initFollows() {
        if(follows == null) {
            follows = new ArrayList<String>();
        }
    }

    private void initFollowers() {
        if(followers == null) {
            followers = new ArrayList<String>();
        }
    }
}
