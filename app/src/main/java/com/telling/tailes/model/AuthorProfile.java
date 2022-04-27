package com.telling.tailes.model;

public class AuthorProfile {
    private String authorId;
    private int storyCount;
    private int loveCount;
    private int followCount;
    private int profileIcon;
    private boolean following;

    public AuthorProfile(String authorId, int profileIcon, int storyCount, int loveCount, int followCount, boolean following) {
        this.authorId = authorId;
        this.storyCount = storyCount;
        this.loveCount = loveCount;
        this.followCount = followCount;
        this.following = following;
        this.profileIcon = profileIcon;
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

    public int getFollowCount() { return followCount; }

    public boolean following() { return following; }

    public int getProfileIcon() { return profileIcon; }
}
