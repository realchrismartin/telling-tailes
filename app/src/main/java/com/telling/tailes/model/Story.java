package com.telling.tailes.model;

import java.util.ArrayList;

public class Story {

    private String id;
    private String authorID;
    private String title;
    private String storyText;
    private boolean isDraft;
    private ArrayList<String> loves;

    public Story() {};

    public Story(String id, String authorID, boolean isDraft, String title, String storyText, ArrayList<String> loves)
    {
        this.id = id;
        this.isDraft = isDraft;
        this.authorID = authorID;
        this.title = title;
        this.storyText = storyText;
        this.loves = loves;
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

    public ArrayList<String> getLoves() {
        if (loves == null) { //TODO: is there a better way to check if this exists?
            return new ArrayList<String>();
        }
        return loves;
    }

    public void addLove(String userId) {
        loves.add(userId);
    }
}
