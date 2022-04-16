package com.telling.tailes.card;


import com.telling.tailes.model.Story;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;

import java.util.ArrayList;

public class StoryRviewCard {
    private Story story;
    private User user;

    public StoryRviewCard(Story story) {
        this.story = story;
        //this.user = AuthUtils.getLoggedInUserID();
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

    public ArrayList<String> getLovers() {
        return story.getLovers();
    }

    public void addLove(String userId) {
        story.addLover(userId);
    }

    public void removeLove(String userId) {
        story.removeLover(userId);
    }

    public Story getStory() { return story; }
}
