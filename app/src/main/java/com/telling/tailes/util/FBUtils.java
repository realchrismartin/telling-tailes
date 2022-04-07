package com.telling.tailes.util;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.card.StoryRviewCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FBUtils {
    private static final String storyDBKey = "stories"; //TODO
    private static final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(storyDBKey);

    public static void updateLove(StoryRviewCard currentItem) {
        String currentUser = AuthUtils.getLoggedInUserID();

        ArrayList<String> loves = currentItem.getLoves();

        if (loves.contains(currentUser)) {
            Log.d("updateLove", "removing love from FB...");
            loves.remove(currentUser);
        } else {
            Log.d("updateLove", "adding love to FB...");
            loves.add(currentUser);
        }

        Map<String, Object> fbUpdate = new HashMap<>();
        fbUpdate.put("loves", loves);
        Task<Void> storyLoveTask =  ref.child(currentItem.getID()).updateChildren(fbUpdate);

        storyLoveTask.addOnCompleteListener(task -> {
            Log.d("updateLove", "added love to FB");
            currentItem.updateLoves(loves);
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
