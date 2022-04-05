package com.telling.tailes.model;

public class Story {

    private String id;
    private String authorID;
    private String title;
    private String storyText;
    private boolean isDraft;

    public Story() {};

    public Story(String id, String authorID, boolean isDraft, String title, String storyText)
    {
        this.id = id;
        this.isDraft = isDraft;
        this.authorID = authorID;
        this.title = title;
        this.storyText = storyText;
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
}
