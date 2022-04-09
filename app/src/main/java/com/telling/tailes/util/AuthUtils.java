package com.telling.tailes.util;

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
    //Returns a zero length string if successful, otherwise returns an error
    public static String logInUser(String username, String password)
    {
        return "There was some error"; //TODO
    }
}
