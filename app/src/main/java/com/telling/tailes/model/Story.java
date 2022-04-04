package com.telling.tailes.model;

public class Story {

    private String id;
    private String authorId;
    private String title;
    private String storyText;
    private boolean isDraft;

    public Story(String id, String authorID, boolean isDraft, String title, String storyText)
    {
        this.id = id;
        this.isDraft = isDraft;
        this.authorId = authorID;
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
        return authorId;
    }
}
