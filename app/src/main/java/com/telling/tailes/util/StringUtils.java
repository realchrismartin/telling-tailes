package com.telling.tailes.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringUtils {

    public static final String backgroundResultPropertyCreate = "create";
    public static final String backgroundResultPropertyLogin = "login";
    public static final String backgroundResultPropertyLogout = "logout";
    public static final String backgroundResultPropertyPasswordChange = "password_change";
    public static final String backgroundResultPropertyResult = "Result";
    public static final String backgroundResultPropertyStory = "story";
    public static final String backgroundResultPropertyUnfollow ="unfollowAuthor";
    public static final String backgroundResultPropertyFollowed ="followedAuthors";
    public static final String backgroundResultPropertyAuthorProfile ="authorProfile";
    public static final String backgroundResultPropertyPublish = "publish";
    public static final String backgroundResultPropertyStoryData = "storyData";
    public static final String backgroundResultPropertyStoryLove = "love";
    public static final String backgroundResultPropertyStoryBookmark = "bookmark";
    public static final String backgroundResultPropertyStoryTokenRefresh = "tokenRefresh";


    public static final String filterTypeBookmarks = "Bookmarks";
    public static final String filterTypeByFollowedAuthors = "By Followed Authors";
    public static final String filterTypeDrafts = "Drafts";
    public static final String filterTypeMyTailes = "My T(ai)les";



    public static final String loadingString = "loading";
    public static final String noAuthorsString = "noauthors";
    public static final String errorString = "error";


    public static final String storyDBKey = "stories";


    //Given a string, get the number of "words" in the string
    public static int getWordCount(String input) {
        return input.split(" ").length;
    }


    //Given a string, get an "obfuscated" string
    public static String getAsterisks(String input) {

        int num = input.length();

        StringBuilder result = new StringBuilder();

        for(int i=0;i<num;i++) {
            result.append("*");
        }

        return result.toString();
    }

    //Given an arbitrary string, convert the string to an integer that reasonably corresponds with the input
    //Used for notification ids based on user / story ids
    //Note that this is not really unique, which doesn't matter in this use case
    public static Integer toIntegerId(String input) {
        return Arrays.hashCode(input.getBytes(StandardCharsets.UTF_8));
    }
}
