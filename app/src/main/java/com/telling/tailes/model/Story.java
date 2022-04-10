package com.telling.tailes.model;

import java.util.ArrayList;

public class Story {

    private String id;
    private String authorID;
    private String title;
    private String storyText;
    private boolean isDraft;
    private ArrayList<String> lovers;

    private Story() {};

    public Story(String id, String authorID, String title, String storyText, boolean isDraft, ArrayList<String> lovers)
    {
        this.id = id;
        this.isDraft = isDraft;
        this.authorID = authorID;
        this.title = title;
        this.storyText = storyText;
        this.lovers = lovers;
    }

    public String getId()
    {
        return id;
    }

    public boolean getIsDraft()
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

    public ArrayList<String> getLovers() {
        if (lovers == null) { //TODO: is there a better way to check if this exists?
            return new ArrayList<String>();
        }
        return lovers;
    }

    public void addLover(String userId) {
        lovers.add(userId);
    }
}
