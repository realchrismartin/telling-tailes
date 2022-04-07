package com.telling.tailes.card;


import android.util.Log;

import java.util.ArrayList;

public class StoryRviewCard implements StoryRviewCardClickListener {
    private final String id;
    private final String authorId;
    private final String title;
    private ArrayList<String> loves;

    public StoryRviewCard(String id, String authorId, String title, ArrayList<String> loves) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.loves = loves;
    }

    public String getID() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getLoves() {
        return loves;
    }

    public void addLove(String userId) {
        loves.add(userId);
    }

    public void removeLove(String userId) {
        loves.remove(userId);
    }

    @Override
    public void onStoryClick(int position) {
        Log.d("Story Card", "Story Card has been clicked");
    }
}
