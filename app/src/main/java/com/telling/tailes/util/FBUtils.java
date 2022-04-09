package com.telling.tailes.util;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.card.StoryRviewCard;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FBUtils {
    private static final String storyDBKey = "stories"; //TODO
    private static final String usersDBKey = "users"; //TODO

    private static final DatabaseReference storiesRef = FirebaseDatabase.getInstance().getReference().child(storyDBKey);
    private static final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child(usersDBKey);

    public static void updateLove(Context context, StoryRviewCard currentItem, Consumer<Boolean> callback) {

        if(!AuthUtils.userIsLoggedIn(context))
        {
            callback.accept(false);
            return;
        }

        String currentUser = AuthUtils.getLoggedInUserID(context);

        ArrayList<String> lovers = currentItem.getLovers();

        if (lovers.contains(currentUser)) {
            lovers.remove(currentUser);
        } else {
            lovers.add(currentUser);
        }

        Map<String, Object> fbUpdate = new HashMap<>();
        fbUpdate.put("lovers", lovers);
        Task<Void> storyLoveTask =  storiesRef.child(currentItem.getID()).updateChildren(fbUpdate);

        storyLoveTask.addOnCompleteListener(task -> {
            currentItem.updateLovers(lovers);
            callback.accept(true);
        });

        storyLoveTask.addOnFailureListener(task -> {
            callback.accept(false);
        });
    }

    public static void updateBookmark(Context context, StoryRviewCard currentItem, Consumer<Boolean> callback) {

        if(!AuthUtils.userIsLoggedIn(context))
        {
            return;
        }

        //TODO: implement
        callback.accept(false); //TODO
    }

    //Checks if user exists or not in Firebase
    //Calls callback when result is determined
    //Returns false in callback if user doesn't exist, otherwise returns true
    public static void userExists(Context context, String username, Consumer<Boolean> callback) {
        callback.accept(false); //TODO
    }

    //Create a user. Assumes that the user account doesn't exist already // this has already been checked
    //Calls callback when user is created, or if creation fails
    //Returns false in callback if user wasn't created, otherwise returns true
    public static void createUser(Context context, String username, String password, Consumer<Boolean> callback) {

        String hashedPassword = AuthUtils.hashPassword(password);

        callback.accept(false); //TODO
    }

    //Checks if user can login with this (cleartext) password
    //Calls callback when result is determined
    //TODO: Note that this is the function we want to "override" in order to enable "passwordless" login - just callback.accept(true)
    public static void userPasswordValid(Context context, String username, String password, Consumer<Boolean> callback) {

        String hashedPassword = AuthUtils.hashPassword(password);

        callback.accept(false); //TODO
    }
}
