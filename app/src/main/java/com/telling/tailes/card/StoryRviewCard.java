package com.telling.tailes.card;


import android.util.Log;

public class StoryRviewCard implements StoryRviewCardClickListener {
    private final int number;
    private final int val;

    public StoryRviewCard(int number, int val) {
        this.number = number;
        this.val = val;
    }

    public int getNumber() {
        return number;
    }

    public int getVal() {
        return val;
    }

    @Override
    public void onStoryClick(int position) {
        Log.d("Story Card", "Story Card has been clicked");
    }
}
