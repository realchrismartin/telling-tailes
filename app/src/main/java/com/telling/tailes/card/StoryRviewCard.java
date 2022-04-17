package com.telling.tailes.card;


import com.telling.tailes.model.Story;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;

import java.util.ArrayList;

public class StoryRviewCard {
    private Story story;
    private Boolean bookmarked;

    public StoryRviewCard(Story story) {
        this.story = story;
        this.bookmarked = false;
    }

    public String getID() {
        return story.getId();
    }

    public String getAuthorId() {
        return story.getAuthorID();
    }

    public String getTitle() {
        return story.getTitle();
    }

    public Story getStory() { return story; }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void toggleBookmarked() {
        this.bookmarked = !bookmarked;
    }

    public ArrayList<String> getBookmarkers() {
        return story.getBookmarkers();
    }

    public void setStory(Story story) { this.story = story; }

}
