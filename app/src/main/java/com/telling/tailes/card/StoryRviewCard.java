package com.telling.tailes.card;


import android.util.Log;

public class StoryRviewCard implements StoryRviewCardClickListener {
    private final String id;
    private final String authorId;
    private final String title;

    public StoryRviewCard(String id, String authorId, String title) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
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

    @Override
    public void onStoryClick(int position) {
        Log.d("Story Card", "Story Card has been clicked");
    }
}
