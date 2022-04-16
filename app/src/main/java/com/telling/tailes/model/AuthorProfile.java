package com.telling.tailes.model;

public class AuthorProfile {
    private String authorId;
    private int storyCount;
    private int loveCount;

    public AuthorProfile() {
        //TODO!
        authorId = "somenonexistenttest";
        storyCount = 1;
        loveCount = 99;
    }

    public String getAuthorId() {
        return authorId;
    }

    public int getStoryCount() {
        return storyCount;
    }

    public int getLoveCount() {
        return loveCount;
    }
}
