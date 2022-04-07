package com.telling.tailes.util;

import android.util.Log;

import com.telling.tailes.card.StoryRviewCard;

import java.util.ArrayList;

public class FBUtils {

    public static void updateLove(StoryRviewCard currentItem) {
        String currentUser = "current_user"; //TODO: get current USER id

        if (currentItem.getLoves().contains(currentUser)) {
            Log.d("updateLove", "removing love from FB");
            currentItem.removeLove(currentUser);
        } else {
            Log.d("updateLove", "adding love to FB");
            currentItem.addLove(currentUser);
        }
        //TODO: update on the server side now
    }

    public static void updateBookmark(StoryRviewCard currentItem) {
        //TODO: this whole thing
    }

}
