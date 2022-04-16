package com.telling.tailes.model;

public class AuthorProfile {
    private String authorId;
    private int storyCount;
    private int loveCount;
    private boolean following;

    public AuthorProfile(String authorId, int storyCount, int loveCount, boolean following) {
        this.authorId = authorId;
        this.storyCount = storyCount;
        this.loveCount = loveCount;
        this.following = following;
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

    public boolean following() { return following; }
}
