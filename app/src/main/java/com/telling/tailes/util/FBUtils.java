package com.telling.tailes.util;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.model.AuthorProfile;
import com.telling.tailes.model.Story;
import com.telling.tailes.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

                                           String body = currentUser + " " +  context.getString(R.string.message_loved_body) + " \"" + story.getTitle() + "\"";

                                           sendNotification(context, story.getAuthorID(), context.getString(R.string.message_loved), body, "", "love", story.getId(), new Consumer<Boolean>() {
                                               @Override
                                               public void accept(Boolean aBoolean) {

                                                   if(!aBoolean) {
                                                       Log.e("UpdateLove","Failed to send notification");
                                                   }
                                               }
                                           });

                                           //Note: we don't wait for notification to finish
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
    public static void getStory(Context context, String storyId, Consumer<Story> callback) {
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

        // Check if user is even logged in
        if(!AuthUtils.userIsLoggedIn(context)) {
            callback.accept(null);
            return;
        }

        String currentStory = story.getId();
        String currentUser = AuthUtils.getLoggedInUserID(context);

        // Get user from database to maintain parity
        getUser(context, AuthUtils.getLoggedInUserID(context), new Consumer<User>() {
            @Override
            public void accept(User user) {
                Map<String, Object> fbUserUpdate= new HashMap<>();
                ArrayList<String> oldBookmarks = user.getBookmarks();
                ArrayList<String> newBookmarks = user.getBookmarks();

                if (newBookmarks.contains(currentStory)) {
                    newBookmarks.remove(currentStory);
                } else {
                    newBookmarks.add(currentStory);
                }

                // Update user in database
                user.setBookmarks(newBookmarks);
                fbUserUpdate.put(user.getUsername(), (Object) user);
                Task<Void> userBookmarkTask = usersRef.updateChildren(fbUserUpdate);

                userBookmarkTask.addOnCompleteListener(task -> {
                    Log.d("updateUserBookmark", "added bookmark for user");

                    // Now that user has been updated properly, get story from database to update
                    getStory(context, story.getId(), new Consumer<Story>() {
                        @Override
                        public void accept(Story resultStory) {
                            // If story could not be found in database...
                            if (resultStory == null) {
                                user.setBookmarks(oldBookmarks);
                                fbUserUpdate.put(user.getUsername(), (Object) user);
                                Task<Void> userBookmarkRollbackTask = usersRef.updateChildren(fbUserUpdate);
                                // ... roll back changes to user
                                userBookmarkRollbackTask.addOnCompleteListener(task -> {
                                    callback.accept(null);
                                });
                                userBookmarkRollbackTask.addOnFailureListener(task -> {
                                    callback.accept(null);
                                });
                                return;
                            }

                            // Now, update story in database with new bookmarkers
                            ArrayList<String> bookmarkers = resultStory.getBookmarkers();

                            if (bookmarkers.contains(currentUser)) {
                                Log.d("updateBookmarks", "removing bookmark from FB...");
                                resultStory.removeBookmark(currentUser);
                            } else {
                                Log.d("updateBookmarks", "adding bookmark to FB...");
                                resultStory.addBookmark(currentUser);
                            }

                            Map<String, Object> fbUpdate = new HashMap<>();
                            fbUpdate.put(resultStory.getId(), resultStory);
                            Task<Void> storyBookmarkTask = storiesRef.updateChildren(fbUpdate);

                            storyBookmarkTask.addOnCompleteListener(task -> {
                                Log.d("updateBookmark", "added bookmark to FB");
                                callback.accept(resultStory);
                            });

                            storyBookmarkTask.addOnFailureListener(task -> {
                                callback.accept(null);
                            });
                        }
                    });
                });

                userBookmarkTask.addOnFailureListener(task -> {
                    callback.accept(null);
                });
            }
        });
    }

    //Given a user, add the user to the current logged in user's record as an author the current user is following
    //Additionally update the target user's follower list to include the current user
    //Call the callback with the updated current user record if successful
    //Otherwise, call it with null
    public static void updateFollow(Context context, String username, Consumer<User> callback) {

        //Get the current user
        getUser(context, AuthUtils.getLoggedInUserID(context), new Consumer<User>() {
            @Override
            public void accept(User follower) {
                if(follower == null) {
                    //Current user doesn't exist
                    callback.accept(null);
                    return;
                }

                //Get the target user
                getUser(context, username, new Consumer<User>() {
                    @Override
                    public void accept(User followee) {
                        if(followee == null)  {
                            //Target user doesn't exist
                            callback.accept(null);
                            return;
                        }

                        boolean removedFollow = followee.getFollowers().contains(follower.getUsername());

                        //Update the follower's list to include followee
                        follower.updateFollows(followee.getUsername());

                        updateUser(context, follower, new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean result) {

                                if(!result) {
                                    callback.accept(null);
                                    return;
                                }

                                //If follower list was updated, update followee list
                                followee.updateFollowers(follower.getUsername());

                                updateUser(context, followee, new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean followeeResult) {
                                       if(!followeeResult)  {

                                           //Rollback prior change if followee update failed
                                           follower.updateFollowers(followee.getUsername());

                                           updateUser(context, follower, new Consumer<Boolean>() {
                                                       @Override
                                                       public void accept(Boolean aBoolean) {
                                                           Log.e("updateFollowers","Rollback occurred in follower update with result " + aBoolean);
                                                            callback.accept(null); //Regardless of result, this failed
                                                       }
                                                   });
                                           return;
                                       }

                                        //Skip notification if unfollowing
                                        if(removedFollow) {
                                           callback.accept(follower);
                                           return;
                                        }

                                        // send notification if following
                                        String body = follower.getUsername() + " " + context.getString(R.string.message_followed_body);
                                        sendNotification(context, followee.getUsername(), context.getString(R.string.message_followed), body, "", "follow", "", new Consumer<Boolean>() {
                                            @Override
                                            public void accept(Boolean aBoolean) {

                                                if(aBoolean) {
                                                    Log.e("updateFollow","Failed to send follow notification");
                                                }

                                            }
                                        });

                                        //Note: we don't wait for notification to finish
                                        callback.accept(follower);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    //Retrieves author profile data for the specified user
    //Calls callback with a new AuthorProfile instance when all data is retrieved and consolidated
    //Calls callback with null if there's some issue retrieving data
    public static void getAuthorProfile(Context context, String username, Consumer<AuthorProfile> callback) {

        //Get the current user's current follow list
        getUser(context, AuthUtils.getLoggedInUserID(context), new Consumer<User>() {
            @Override
            public void accept(User currentUser) {
               if(currentUser == null) {
                   //Current user doesn't exist
                   callback.accept(null);
               } else {
                   boolean following = currentUser.getFollows().contains(username);

                   //Get the specified author's user
                   getUser(context, username, new Consumer<User>() {
                       @Override
                       public void accept(User user) {
                          if(user == null)  {
                              //Author doesn't exist
                              callback.accept(null);
                          } else {
                              callback.accept(new AuthorProfile(user.getUsername(),user.getProfileIcon(),user.getStories(),user.getLoves(),user.getFollowers().size(), following));
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

        Task<String> token = FirebaseMessaging.getInstance().getToken();

        token.addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                Pair<String,String> hashedPieces = AuthUtils.hashPassword(password);

                User user = new User(username,profileIcon,hashedPieces.first,hashedPieces.second,new ArrayList<>(),new ArrayList<>(),0,0,task.getResult());

                Task<Void> createUserTask = usersRef.child(user.getUsername()).setValue(user);

                createUserTask.addOnCompleteListener(t -> {
                    callback.accept(true);
                });

                createUserTask.addOnFailureListener(t -> {
                    callback.accept(false);
                });
            }
        });

        token.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FBUtils.createUser",e.toString());
                callback.accept(false);
            }
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

    public static void sendNotificationToFollowers(Context context, String username, String title, String body, String content, String type, String storyId, Consumer<Boolean> callback) {

        Task<DataSnapshot> userData = usersRef.child(username).get();

        userData.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                callback.accept(false);
            }
        });

        userData.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                DataSnapshot userResult = task.getResult();

                if (!userResult.exists()) {
                    callback.accept(false);
                    return;
                }

                User user = userResult.getValue(User.class);

                if(user == null) {
                    callback.accept(false);
                    return;
                }

                ArrayList<String> followers = user.getFollowers();

                for(String followerUsername : followers) {
                    sendNotification(context, followerUsername, title, body, content, type, storyId, new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) {
                            if(!aBoolean) {
                                Log.e("sendNotificationToFollowers",context.getString(R.string.generic_error_notification));
                            }
                        }
                    });
                }

                //Note: We don't wait on notification results to complete callback
                callback.accept(true);
            }
        });
    }

    //Send a FCM message to the specified recipient
    public static void sendNotification(Context context, String recipientUsername, String title, String body, String content, String type, String storyId, Consumer<Boolean> callback)
    {
        Task<DataSnapshot> userData = usersRef.child(recipientUsername).get();

        userData.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                callback.accept(false);
            }
        });

        userData.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                DataSnapshot userResult = task.getResult();

                if(!userResult.exists()) {
                    callback.accept(false);
                    return;
                }

                User recipientUser = userResult.getValue(User.class);

                if(recipientUser == null) {
                    callback.accept(false);
                    return;
                }

                //Run doFCMMessage in another(?) background thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doFCMMessage(context, recipientUser, title, body, content, type, storyId, new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) {
                                callback.accept(aBoolean);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    //Helper method to send a FCM message
    private static void doFCMMessage(Context context, User recipient, String title, String body, String content, String type, String storyId, Consumer<Boolean> callback) {

        String recipientFCMToken = recipient.getMessagingToken();

        if(recipientFCMToken == null || recipientFCMToken.equals("")) {
            //Silently accept missing or blank tokens - no notifications should be sent here
            callback.accept(true);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        JSONObject jNotification = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jNotification.put("title", title);
            jNotification.put("body", body);
            jNotification.put("badge", "1");
            data.put("content", content);
            data.put("type", type);
            data.put("storyID", storyId);
            jsonObject.put("to", recipientFCMToken);
            jsonObject.put("priority", "high");
            jsonObject.put("notification", jNotification);
            jsonObject.put("data", data);

        } catch (JSONException e) {
            e.printStackTrace();
            callback.accept(false);
            return;
        }

        try {

            String serverToken = context.getString(R.string.fcm_server_key);
            URL url = new URL(context.getString(R.string.fcm_uri));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", serverToken);
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.close();

            InputStream inputStream = conn.getInputStream();

            StringBuilder stringBuilder = new StringBuilder();
            String res = "";

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String len;
                while ((len = bufferedReader.readLine()) != null) {
                    stringBuilder.append(len);
                }
                bufferedReader.close();
                res = stringBuilder.toString().replace(",", ",\n");
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Finish regardless of result
            callback.accept(true);

        } catch (IOException e) {
            e.printStackTrace();
            callback.accept(false);
        }
    }
}
