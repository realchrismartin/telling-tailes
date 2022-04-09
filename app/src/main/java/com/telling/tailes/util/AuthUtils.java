package com.telling.tailes.util;

import android.content.Context;
import com.telling.tailes.R;
import java.util.function.Consumer;

public class AuthUtils {

    /*
        Return the name of the logged in user
        If a user is not logged in, return a blank string
     */
    public static String getLoggedInUserID() {
        return "authortest"; //TODO
    }

    /*
        Return true if a user is logged in, false otherwise
     */
    public static boolean userIsLoggedIn() {
        return false; //TODO
    }

    //Attempts to log in as the specified user
    //Calls callback with a zero length string if successful
    //Otherwise, calls callback with error message(s)
    public static void logInUser(Context context, String username, String password, Consumer<String> callback)
    {
        //Check if user exists
        FBUtils.userExists(context,username, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean result) {
                if (!result) {
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

                        //If user exists and password is valid, log the user in
                        //TODO: actually do login step, update shared properties to store the username
                        callback.accept(""); //Indicate that everything went ok
                    }
                });
            }
        });
    }

    //Attempts to create the specified user
    //Calls the callback when done - either with a zero length string (on success) or an error message (on failure)
    public static void createUser(Context context, String username, String password, String confirmationPassword, Consumer<String> callback)
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
                FBUtils.createUser(context,username, password, new Consumer<Boolean>() {
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
}
