package com.telling.tailes.card;

import com.telling.tailes.model.Story;

import java.util.ArrayList;

public class StoryRviewCard {
    private Story story;

    public StoryRviewCard(Story story) {
        this.story = story;
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

    public ArrayList<String> getBookmarkers() {
        return story.getBookmarkers();
    }

    public void setStory(Story story) { this.story = story; }

}
