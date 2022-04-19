package com.telling.tailes.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Story implements Serializable {

    private String id;
    private String authorID;
    private String title;
    private String promptText;
    private String storyText;
    private boolean isDraft;
    private ArrayList<String> lovers;
    private ArrayList<String> bookmarkers;
    private int loveCount;
    private long timestamp;

    private Story() {};

    public Story(String id, String authorID, boolean isDraft, String title,
                 String promptText, String storyText, ArrayList<String> lovers, ArrayList<String> bookmarkers, int loveCount, long timestamp)
    {
        this.id = id;
        this.isDraft = isDraft;
        this.authorID = authorID;
        this.title = title;
        this.promptText = promptText;
        this.storyText = storyText;
        this.lovers = lovers;
        this.bookmarkers = bookmarkers;
        this.timestamp = -timestamp; //Store timestamp in reverse
    }

    public String getId()
    {
        return id;
    }

    public boolean getIsDraft()
    {
        return isDraft;
    }

    public String getTitle() { return title; }

    public String getPromptText() { return promptText; }

    public String getStoryText()
    {
        return storyText;
    }

    public String getAuthorID()
    {
        return authorID;
    }

    public long getTimestamp() {
        return timestamp; //Timestamp is stored in reverse in order to trick FB database ordering - this will be negative
    }

    public int getLoveCount() {
        return loveCount; //Love count is stored in reverse in order to trick FB database ordering - this will be negative
    }

    private void initLovers() {
        if (lovers == null) { //TODO: is there a better way to check if this exists?
            lovers = new ArrayList<String>();
        }
  }

    public ArrayList<String> getLovers() {
        initLovers();
        return lovers;
    }

    public void addLover(String userId) {
        initLovers();
        lovers.add(userId);
        loveCount--; //Stored in reverse
    }

    public void removeLover(String userId) {
        initLovers();
        lovers.remove(userId);
        loveCount++; //Stored in reverse
    }

    public ArrayList<String> getBookmarkers() {
        initBookmarkers();
        return bookmarkers;
    }

    private void initBookmarkers() {
        if (bookmarkers == null) {
            bookmarkers = new ArrayList<String>();
        }
    }


    public void removeBookmark(String userID) {
        initBookmarkers();
        bookmarkers.remove(userID);
    }

    public void addBookmark(String userID) {
        initBookmarkers();
        bookmarkers.add(userID);
    }
}
