package com.telling.tailes.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Story implements Serializable {

    private String id;
    private String authorID;
    private String title;
    private String storyText;
    private boolean isDraft;
    private ArrayList<String> lovers;

    public Story() {};

    public Story(String id, String authorID, boolean isDraft, String title, String storyText, ArrayList<String> lovers)
    {
        this.id = id;
        this.isDraft = isDraft;
        this.authorID = authorID;
        this.title = title;
        this.storyText = storyText;
        this.lovers = lovers;
    }

    public String getID()
    {
        return id;
    }

    public boolean isDraft()
    {
        return isDraft;
    }

    public String getTitle()
    {
        return title;
    }

    public String getStoryText()
    {
        return storyText;
    }

    public String getAuthorID()
    {
        return authorID;
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
    }

    public void removeLover(String userId) {
        initLovers();
        lovers.remove(userId);
    }
}
