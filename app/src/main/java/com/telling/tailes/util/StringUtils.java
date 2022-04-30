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
    public static final String filterTypeByAuthor = "By Author";

    public static final String messagingServiceErrorStoryId = "FCM storyId member is null";
    public static final String messagingServiceErrorType = "FCM type member is null";
    public static final String messagingServiceErrorFollowerUsername ="FCM followerUsername is null";

    public static final String messagingServiceTag = "MessagingService";
    public static final String authUtilsTag = "AuthUtils";

    public static final String loadingString = "loading";
    public static final String noAuthorsString = "noauthors";
    public static final String errorString = "error";
    public static final String emptyString = "";

    public static final String intentExtraStoryId = "storyId";
    public static final String intentExtraFollowerUsername = "followerUsername";
    public static final String intentExtraFeedFilter = "feedFilter";
    public static final String intentExtraAuthorId = "authorId";

    public static final String notificationChannelId = "TELLING_TAILES_CHANNEL_ID";
    public static final String notificationGroupId = "com.telling.tailes.NOTIFICATION";

    public static final String fcmTokenError = "User returned from updateUserToken was null, token may not have been updated";

    public static final String sharedPreferenceFileUser = "user_preferences";
    public static final String sharedPreferenceKeyUsername = "username";
    public static final String sharedPreferenceKeyMessagingToken = "messagingToken";
    public static final String defaultPassword = "thisisnotsecure";


    public static final String authUtilsErrorMessageUserNull = "";
    public static final String authUtilsErrorMessageTokenUpdateFailure = "";

    public static final String passwordHashAlgorithm = "SHA-512";

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
