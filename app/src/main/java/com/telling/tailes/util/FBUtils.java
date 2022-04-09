package com.telling.tailes.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FBUtils {
    private static final String storyDBKey = "stories"; //TODO
    private static final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(storyDBKey);

    public static void updateLove(StoryRviewCard currentItem) {
        String currentUser = AuthUtils.getLoggedInUserID();

        ArrayList<String> lovers = currentItem.getLovers();

        if (lovers.contains(currentUser)) {
            Log.d("updateLove", "removing love from FB...");
            lovers.remove(currentUser);
        } else {
            Log.d("updateLove", "adding love to FB...");
            lovers.add(currentUser);
        }

        Map<String, Object> fbUpdate = new HashMap<>();
        fbUpdate.put("lovers", lovers);
        Task<Void> storyLoveTask =  ref.child(currentItem.getID()).updateChildren(fbUpdate);

        storyLoveTask.addOnCompleteListener(task -> {
            Log.d("updateLove", "added love to FB");
            currentItem.updateLovers(lovers);
        });

        storyLoveTask.addOnFailureListener(task -> {
            Log.d("updateLove", "unable to add love to FB");
            //TODO: alert user?
        });
    }

    public static void updateBookmark(StoryRviewCard currentItem) {
        //TODO: this whole thing
    }

    //Create a user. Assumes that the user has been validated - i.e. it doesn't exist already
    //Calls callback when user is created, or if creation fails
    //Returns false in callback if user wasn't created, otherwise returns true
    public static void createUser(Context context, String username, String password, Consumer<Boolean> callback) {
        callback.accept(false); //tODO
    }

    //Checks if user exists or not in Firebase
    //Calls callback when result is determined
    //Returns false in callback if user doesn't exist, otherwise returns true
    public static void userExists(Context context, String username, Consumer<Boolean> callback) {
        callback.accept(false); //TODO
    }

    //Checks if user can login with this password
    //Calls callback when result is determined
    //TODO: Note that this is the function we want to "override" in order to enable "passwordless" login
    public static void userPasswordValid(Context context, String username, String password, Consumer<Boolean> callback) {
        callback.accept(false); //TODO
    }
}
