package com.telling.tailes.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.telling.tailes.R;
import com.telling.tailes.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.function.Consumer;

public class AuthUtils {

    /*
        Return the name of the logged in user
        If a user is not logged in, return a blank string
     */
    public static String getLoggedInUserID(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        return sharedPref.getString("username",""); //TODO: unhardcode
    }

    /*
        Return true if a user is logged in, false otherwise
     */
    public static boolean userIsLoggedIn(Context context) {
        return !getLoggedInUserID(context).equals("");
    }

    //Attempts to log in as the specified user
    //Calls callback with a zero length string if successful
    //Otherwise, calls callback with error message(s)
    public static void logInUser(Context context, String username, String password, Consumer<String> callback)
    {
        //Check if user exists
        FBUtils.getUser(context,username, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if (user == null) {
                    callback.accept(context.getResources().getString(R.string.login_error_notification));
                    return;
                }

                //Check if user's password is valid
                FBUtils.userPasswordValid(context, username, password, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) {

                        if(!result) {
                            callback.accept(context.getResources().getString(R.string.login_error_notification));
                            return;
                        }

                        //User's password is valid
                        //Get a new messaging token for this user on login
                        Task<String> tokenTask = FirebaseMessaging.getInstance().getToken();

                        tokenTask.addOnFailureListener(new OnFailureListener() {
                                                           @Override
                                                           public void onFailure(@NonNull Exception e) {
                                                               callback.accept(e.getMessage());
                                                           }
                                                       });

                        tokenTask.addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                //Token is obtained, update user in DB
                                String token = task.getResult();

                                user.setMessagingToken(token);

                                FBUtils.updateUser(context, user, new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean aBoolean) {
                                        if(!aBoolean) {
                                            callback.accept(context.getResources().getString(R.string.login_error_notification));
                                        }

                                        //If user exists and password is valid, log the user in
                                        //Note: this could be more secure.
                                        updateLogin(context,username);

                                        callback.accept(""); //Indicate that all is well and complete login
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    //Log out - very simply, just delete the username from shared preferences and update the user's token
    public static void logOutUser(Context context, Consumer<String> callback) {

        //Persist username temporarily
        String username = getLoggedInUserID(context);

        //Log out locally
        updateLogin(context,"");

        //Update user to clear messaging token
        FBUtils.getUser(context,username, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if (user == null) {
                    callback.accept(context.getResources().getString(R.string.generic_error_notification));
                    return;
                }

                user.setMessagingToken("");

                FBUtils.updateUser(context, user, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        if (!aBoolean) {
                            callback.accept(context.getResources().getString(R.string.login_error_notification));
                            return;
                        }

                        callback.accept(""); //Indicate that all is well and complete login
                    }
                });
            }
        });
    }

    //Attempts to create the specified user
    //Calls the callback when done - either with a zero length string (on success) or an error message (on failure)
    public static void createUser(Context context, String username, String password, String confirmationPassword, int profileIcon, Consumer<String> callback)
    {
        boolean usernameTooSimple = username.length() < 5; //TODO?
        boolean passwordsDoNotMatch = !password.equals(confirmationPassword);
        boolean passwordTooSimple = password.length() < 5; //TODO?

        if(usernameTooSimple) {
            callback.accept(context.getResources().getString(R.string.username_complexity_error_notification));
            return;
        }

        if(passwordsDoNotMatch) {
            callback.accept(context.getResources().getString(R.string.password_not_same_error_notification));
            return;
        }

        if(passwordTooSimple) {
            callback.accept(context.getResources().getString(R.string.password_complexity_error_notification));
            return;
        }

        //Check if user exists
        FBUtils.userExists(context,username, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean result) {
                if(result) {
                    callback.accept(context.getResources().getString(R.string.user_exists_error_notification));
                    return;
                }

                //If user doesn't exist and passwords match and meet complexity requirement, create account
                FBUtils.createUser(context,username, password,profileIcon, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) {

                        if(!result) {
                            callback.accept(context.getResources().getString(R.string.create_user_error_notification));
                            return;
                        }

                        callback.accept(""); //Successful creation
                    }
                });
            }
        });
    }

    //Called on either login or logout
    private static void updateLogin(Context context, String username) {
        SharedPreferences sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("username", username); //TODO: unhardcode
        editor.apply();
    }

    //Given a string password, return a SHA-512 hashed version of the password and the salt used to hash the password
    public static Pair<String,String> hashPassword(String password) {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String saltString = new String(salt,StandardCharsets.UTF_8);

        return new Pair<String,String>(hashPassword(password,saltString),saltString);
    }

    //Given a string password and a specific salt, return a hashed version of the password using that salt
    public static String hashPassword(String password, String salt) {
        String result = "insecurepassword";

        if(salt == null) {
            return hashPassword(password).first;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            result = new String(md.digest(password.getBytes(StandardCharsets.UTF_8)),StandardCharsets.UTF_8);
        } catch(NoSuchAlgorithmException ex) {
            Log.e("AuthUtils","Failed to hash password: " + ex.getMessage());
        }

        return result;
    }
}
