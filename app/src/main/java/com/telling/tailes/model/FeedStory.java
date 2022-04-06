package com.telling.tailes.model;

//TODO: remove if only used in endless scroll testing
public class FeedStory {
    private int Val;

    private FeedStory() {};

    private FeedStory(int Val) {
        this.Val = Val;

    }

    public int getVal() {return Val;}
    public void setVal(int Val) {this.Val = Val;}
}
