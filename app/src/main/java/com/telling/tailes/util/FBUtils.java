package com.telling.tailes.util;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.model.AuthorProfile;
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

    //Update the specified user with the provided User data
    //Call the callback with true if all is well, otherwise call it with false
    public static void updateUser(Context context, User user, Consumer<Boolean> callback) {
        Task<Void> updateUserTask = usersRef.child(user.getUsername()).setValue(user);

        updateUserTask.addOnCompleteListener(task -> {
            callback.accept(true);
        });

        updateUserTask.addOnFailureListener(task -> {
            callback.accept(false);
        });
    }

    //Given a story, update the story to either love or unlove the story
    //CAll the callback providing the updated story if all is well
    //Otherwise call the callback with null
    public static void updateLove(Context context, Story story, Consumer<Story> callback) {

        if(!AuthUtils.userIsLoggedIn(context))
        {
            callback.accept(null);
            return;
        }

        String currentUser = AuthUtils.getLoggedInUserID(context);

        getStory(context, story.getId(), new Consumer<Story>() {
            @Override
            public void accept(Story story) {

                if(story == null)
                {
                    callback.accept(null);
                    return;
                }

                boolean increment = !story.getLovers().contains(currentUser);

                if(increment) {
                    story.addLover(AuthUtils.getLoggedInUserID(context));
                } else {
                    story.removeLover(AuthUtils.getLoggedInUserID(context));
                }

                Map<String, Object> fbUpdate = new HashMap<>();
                fbUpdate.put(story.getId(),story);
                Task<Void> storyLoveTask =  storiesRef.updateChildren(fbUpdate);

                //Update the author's love count to add or remove a love
                if(increment) {
                    storyLoveTask.addOnCompleteListener(task -> {
                        //Increment the count
                        updateAuthorLoveCount(context, story.getAuthorID(), true, new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean result) {
                                if(!result) {
                                   callback.accept(null);
                                } else {
                                    //Return the updated story
                                   getStory(context, story.getId(), new Consumer<Story>() {
                                       @Override
                                       public void accept(Story updatedStory) {
                                          callback.accept(updatedStory);
                                       }
                                   });
                                }
                            }
                        });
                    });
                } else {
                    storyLoveTask.addOnCompleteListener(task -> {
                        //Decrement the count
                        updateAuthorLoveCount(context, story.getAuthorID(), false, new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean result) {
                                if(!result) {
                                    callback.accept(null);
                                } else {
                                    //Return the updated story
                                    getStory(context, story.getId(), new Consumer<Story>() {
                                        @Override
                                        public void accept(Story story) {
                                            callback.accept(story);
                                        }
                                    });
                                }
                            }
                        });
                    });
                }

                storyLoveTask.addOnFailureListener(task -> {
                    callback.accept(null);
                });
            }
        });
    }

    //Get the current state of the specified story
    private static void getStory(Context context, String storyId, Consumer<Story> callback) {
        Task<DataSnapshot> getUserTask = storiesRef.child(storyId).get();

        getUserTask.addOnCompleteListener(task -> {
            DataSnapshot storyResult = task.getResult();

            if(!storyResult.exists()) {
                callback.accept(null);
                return;
            }

            callback.accept(storyResult.getValue(Story.class));
        });

        getUserTask.addOnFailureListener(task -> {
            callback.accept(null);
        });
    }

    //Increment or decrement love count for this author
    private static void updateAuthorLoveCount(Context context, String authorId, Boolean loved, Consumer<Boolean> callback) {
        getUser(context, authorId, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if(user == null) {
                    callback.accept(false);
                    return;
                }

                if(loved) {
                    user.incrementLoveCount();
                } else {
                    user.decrementLoveCount();
                }

                updateUser(context, user, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) {
                       callback.accept(result);
                    }
                });
            }
        });
    }

    //Increment or decrement story count for this author
    private static void updateAuthorStoryCount(Context context, String authorId, Boolean add, Consumer<Boolean> callback) {
        getUser(context, authorId, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if(user == null) {
                    callback.accept(false);
                    return;
                }

                if(add) {
                    user.incrementStoryCount();
                } else {
                    user.decrementStoryCount();
                }

                updateUser(context, user, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) {
                        callback.accept(result);
                    }
                });
            }
        });
    }

    public static void updateBookmark(Context context, Story story, Consumer<Story> callback) {

        if(!AuthUtils.userIsLoggedIn(context))
        {
            callback.accept(null);
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
            callback.accept(story);
        });

        storyBookmarkTask.addOnFailureListener(task -> {
            callback.accept(null);
        });

        getUser(context, AuthUtils.getLoggedInUserID(context), new Consumer<User>() {
            @Override
            public void accept(User user) {
                Map<String, Object> fbUserUpdate= new HashMap<>();
                ArrayList<String> newBookmarks = user.getBookmarks();

                if (newBookmarks.contains(currentStory)) {
                    newBookmarks.remove(currentStory);
                } else {
                    newBookmarks.add(currentStory);
                }

                user.setBookmarks(newBookmarks);
                fbUserUpdate.put(user.getUsername(), (Object) user);
                Task<Void> userBookmarkTask = usersRef.updateChildren(fbUserUpdate);

                userBookmarkTask.addOnCompleteListener(task -> {
                    Log.d("updateUserBookmark", "added bookmark for user");
                    callback.accept(story);
                });

                userBookmarkTask.addOnFailureListener(task -> {
                    callback.accept(null);
                });
            }
        });





    }

    //Given a user, add the current logged in user as a follower (or remove a follow)
    //If follow is true - add a follow, otherwise remove one
    //Call callback with true if all is well, otherwise call it with false
    public static void updateFollow(Context context, String username, boolean follow, Consumer<Boolean> callback) {
        //TODO: implement
        callback.accept(true);
    }

    //Retrieves author profile data for the specified user
    //Calls callback with a new AuthorProfile instance when all data is retrieved and consolidated
    //Calls callback with null if there's some issue retrieving data
    public static void getAuthorProfile(Context context, String username, Consumer<AuthorProfile> callback) {

        //Get the current user's current follow list
        getUser(context, AuthUtils.getLoggedInUserID(context), new Consumer<User>() {
            @Override
            public void accept(User user) {
               if(user == null) {
                   //Current user doesn't exist
                   callback.accept(null);
               } else {
                   boolean following = user.getFollows().contains(username);

                   //Get the specified author's user
                   getUser(context, username, new Consumer<User>() {
                       @Override
                       public void accept(User user) {
                          if(user == null)  {
                              //Author doesn't exist
                              callback.accept(null);
                          } else {
                              callback.accept(new AuthorProfile(user.getUsername(),user.getProfileIcon(),user.getStories(),user.getLoves(),following));
                          }
                       }
                   });
               }
            }
        });
    }

    //Gets the user with the specified username
    //Runs callback with null if not found
    //Otherwise returns the User
    public static void getUser(Context context, String username, Consumer<User> callback) {

        Task<DataSnapshot> getUserTask = usersRef.child(username).get();

        getUserTask.addOnCompleteListener(task -> {
            DataSnapshot userResult = task.getResult();

            if(!userResult.exists()) {
               callback.accept(null);
               return;
            }

            callback.accept(userResult.getValue(User.class));
        });

        getUserTask.addOnFailureListener(task -> {
            callback.accept(null);
        });
    }

    //Checks if user exists or not in Firebase
    //Calls callback when result is determined
    //Returns false in callback if user doesn't exist, otherwise returns true
    public static void userExists(Context context, String username, Consumer<Boolean> callback) {
        getUser(context, username, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if(user == null) {
                    callback.accept(false);
                } else {
                    callback.accept(true);
                }
            }
        });
    }

    //Create a user. Assumes that the user account doesn't exist already // this has already been checked
    //Calls callback when user is created, or if creation fails
    //Returns false in callback if user wasn't created, otherwise returns true
    public static void createUser(Context context, String username, String password, int profileIcon, Consumer<Boolean> callback) {

        Pair<String,String> hashedPieces = AuthUtils.hashPassword(password);

        User user = new User(username,profileIcon,hashedPieces.first,hashedPieces.second,new ArrayList<>(),0,0);

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
