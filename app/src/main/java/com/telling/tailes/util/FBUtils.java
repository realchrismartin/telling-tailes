package com.telling.tailes.util;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.model.Story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FBUtils {
    private static final String storyDBKey = "stories"; //TODO
    private static final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(storyDBKey);

    public static void updateLove(Story story) {
        String currentUser = AuthUtils.getLoggedInUserID();

        ArrayList<String> lovers = story.getLovers();

        if (lovers.contains(currentUser)) {
            Log.d("updateLove", "removing love from FB...");
            story.removeLover(currentUser);
        } else {
            Log.d("updateLove", "adding love to FB...");
            story.addLover(currentUser);
        }

        Map<String, Object> fbUpdate = new HashMap<>();
        fbUpdate.put(story.getID(),story);
        Task<Void> storyLoveTask =  ref.updateChildren(fbUpdate);

        storyLoveTask.addOnCompleteListener(task -> {
            Log.d("updateLove", "added love to FB");
        });

        storyLoveTask.addOnFailureListener(task -> {
            Log.d("updateLove", "unable to add love to FB");
            //TODO: alert user?
        });
    }

    public static void updateBookmark(StoryRviewCard currentItem) {
        //TODO: this whole thing
    }

}
