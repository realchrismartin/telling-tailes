package com.telling.tailes.card;


import android.util.Log;

import java.util.ArrayList;

public class StoryRviewCard implements StoryRviewCardClickListener {
    private final String id;
    private final String authorId;
    private final String title;
    private ArrayList<String> lovers;

    public StoryRviewCard(String id, String authorId, String title, ArrayList<String> lovers) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.lovers = lovers;
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

    public ArrayList<String> getLovers() {
        return lovers;
    }

    public void updateLovers(ArrayList<String> lovers) {
        this.lovers = lovers;
    }

    public void addLove(String userId) {
        lovers.add(userId);
    }

    public void removeLove(String userId) {
        lovers.remove(userId);
    }

    @Override
    public void onStoryClick(int position) {
        Log.d("Story Card", "Story Card has been clicked");
    }
}
