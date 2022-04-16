package com.telling.tailes.util;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.model.Story;
import com.telling.tailes.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FBUtils {
    private static final String storyDBKey = "stories"; //TODO
    private static final String usersDBKey = "users"; //TODO

    private static final DatabaseReference storiesRef = FirebaseDatabase.getInstance().getReference().child(storyDBKey);
    private static final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child(usersDBKey);

    public static void updateLove(Context context, Story story, Consumer<Boolean> callback) {

        if(!AuthUtils.userIsLoggedIn(context))
        {
            callback.accept(false);
            return;
        }

        String currentUser = AuthUtils.getLoggedInUserID(context);

        ArrayList<String> lovers = story.getLovers();

        if (lovers.contains(currentUser)) {
            Log.d("updateLove", "removing love from FB...");
            story.removeLover(currentUser);
        } else {
            Log.d("updateLove", "adding love to FB...");
            story.addLover(currentUser);
        }

        Map<String, Object> fbUpdate = new HashMap<>();
        fbUpdate.put(story.getId(),story);
        Task<Void> storyLoveTask =  storiesRef.updateChildren(fbUpdate);

        storyLoveTask.addOnCompleteListener(task -> {
            Log.d("updateLove", "added love to FB");
            callback.accept(true);
        });

        storyLoveTask.addOnFailureListener(task -> {
            callback.accept(false);
        });
    }

    public static void updateBookmark(Context context, Story story, Consumer<Boolean> callback) {

        if(!AuthUtils.userIsLoggedIn(context))
        {
            callback.accept(false);
            return;
        }

        String currentStory = story.getId();
        String currentUser = AuthUtils.getLoggedInUserID(context);
        ArrayList<String> bookmarkers = story.getBookmarkers();


        if (bookmarkers.contains(currentUser)) {
            Log.d("updateBookmarks", "removing bookmark from FB...");
            story.removeBookmark(currentUser);
        } else {
            Log.d("updateBookmarks", "adding bookmark to FB...");
            story.addBookmark(currentUser);
        }

        Map<String, Object> fbUpdate = new HashMap<>();
        fbUpdate.put(story.getId(),story);
        Task<Void> storyBookmarkTask =  storiesRef.updateChildren(fbUpdate);

        storyBookmarkTask.addOnCompleteListener(task -> {
            Log.d("updateBookmark", "added bookmark to FB");
            callback.accept(true);
        });

        storyBookmarkTask.addOnFailureListener(task -> {
            callback.accept(false);
        });

    }


    //Checks if user exists or not in Firebase
    //Calls callback when result is determined
    //Returns false in callback if user doesn't exist, otherwise returns true
    public static void userExists(Context context, String username, Consumer<Boolean> callback) {

        Task<DataSnapshot> getUserTask = usersRef.child(username).get();

        getUserTask.addOnCompleteListener(task -> {
            DataSnapshot userResult = task.getResult();

            if(userResult.exists()) {
                callback.accept(true);
            } else {
                callback.accept(false);
            }
        });

        getUserTask.addOnFailureListener(task -> {
            callback.accept(false);
        });
    }

    //Create a user. Assumes that the user account doesn't exist already // this has already been checked
    //Calls callback when user is created, or if creation fails
    //Returns false in callback if user wasn't created, otherwise returns true
    public static void createUser(Context context, String username, String password, Consumer<Boolean> callback) {

        Pair<String,String> hashedPieces = AuthUtils.hashPassword(password);

        User user = new User(username, hashedPieces.first,hashedPieces.second);

        Task<Void> createUserTask = usersRef.child(user.getUsername()).setValue(user);

        createUserTask.addOnCompleteListener(task -> {
            callback.accept(true);
        });

        createUserTask.addOnFailureListener(task -> {
            callback.accept(false);
        });
    }

    //Checks if user can login with this (cleartext) password
    //Calls callback when result is determined
    public static void userPasswordValid(Context context, String username, String password, Consumer<Boolean> callback) {

        Task<DataSnapshot> getUserTask = usersRef.child(username).get();

        getUserTask.addOnCompleteListener(task -> {
            DataSnapshot userResult = task.getResult();

            if(!userResult.exists()) {
                callback.accept(false);
                return;
            }

            //Check password vs the newly provided one
            User user = userResult.getValue(User.class);

            if(user != null && user.checkPassword(password))
            {
                callback.accept(true);
                return;
            }

            callback.accept(false);

        });

        getUserTask.addOnFailureListener(task -> {
            callback.accept(false);
        });
    }

    public static void getBookmarks(Context context, Consumer<ArrayList<String>> callback) {
        ArrayList<String> bookmarks = new ArrayList<>();
        String currentUser = AuthUtils.getLoggedInUserID(context);
        Task<DataSnapshot> getBookmarksTask = usersRef.child(currentUser).child("bookmarks").get();

        getBookmarksTask.addOnCompleteListener(task -> {
            DataSnapshot bookmarksResult = task.getResult();
            for (DataSnapshot snapshot: bookmarksResult.getChildren()) {
                bookmarks.add(snapshot.getValue().toString());
            }

            callback.accept(bookmarks);
        });

        getBookmarksTask.addOnFailureListener(task -> {
          callback.accept(new ArrayList<>()); //TODO?
        });
    }

}
