package com.telling.tailes.card;


import com.telling.tailes.model.Story;

import java.util.ArrayList;

public class StoryRviewCard {
    public  static final Integer CARD_TYPE_STORY = 0;
    public  static final Integer CARD_TYPE_LOADING = 1;
    private Story story;
    private Integer cardType;

    public StoryRviewCard() {
        this.cardType = CARD_TYPE_LOADING;
    }

    public StoryRviewCard(Story story) {
        this.cardType = CARD_TYPE_STORY;
        this.story = story;
    }

    public String getID() {
        switch (cardType) {
            case 0:
                return story.getId();
            case 1:
                return "loading";
            default:
                return "bad";
        }

    }

    public String getAuthorId() {
        return story.getAuthorID();
    }

    public String getTitle() {
        return story.getTitle();
    }

    public Story getStory() { return story; }

    public void setStory(Story story) { this.story = story; }

    public Integer getType() {
        return cardType;
    }
}
