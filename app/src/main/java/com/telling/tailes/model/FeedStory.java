package com.telling.tailes.model;

public class FeedStory {
    private int val;

    private FeedStory() {};

    private FeedStory(int val) {
        this.val = val;

    }

    public int getVal() {return val;}
    public void setVal(int val) {this.val = val;}
}
