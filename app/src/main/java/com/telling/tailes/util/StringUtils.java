package com.telling.tailes.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    //Must match xml resource names
    // These are external facing, but must be here due to switch/case limitations
    public static final String filterTypeBookmarks = "Bookmarks";
    public static final String filterTypeByFollowedAuthors = "By Followed Authors";
    public static final String filterTypeDrafts = "Drafts";
    public static final String filterTypeMyTailes = "My T(ai)les";
    public static final String filterTypeByAuthor = "By Author";
    public static final String filterTypePopular = "Popular";

    public static final String messagingServiceErrorStoryId = "FCM storyId member is null";
    public static final String messagingServiceErrorType = "FCM type member is null";
    public static final String messagingServiceErrorFollowerUsername ="FCM followerUsername is null";

    public static final String messagingServiceTag = "MessagingService";
    public static final String authUtilsTag = "AuthUtils";
    public static final String fbUtilsTag = "FBUtils";
    public static final String gptUtilsTag = "GPTUtils";

    public static final String fbUtilsErrorRollbackFollowers = "Rolling back followers due to failed followee update";

    public static final String loadingString = "loading";
    public static final String noneString = "none";
    public static final String noAuthorsString = "noauthors";
    public static final String errorString = "error";
    public static final String emptyString = "";
    public static final String space = " ";
    public static final String quote = "\"";
    public static final String newline = "\\n";
    public static final String hyphen = "-";
    public static final String colon = ":";
    public static final String apostropheS = "'s";

    public static final String intentExtraStoryId = "storyId";
    public static final String intentExtraFollowerUsername = "followerUsername";
    public static final String intentExtraFeedFilter = "feedFilter";
    public static final String intentExtraAuthorId = "authorId";
    public static final String intentExtraStory = "story";
    public static final String intentExtraPrompt = "prompt";

    public static final String notificationChannelId = "TELLING_TAILES_CHANNEL_ID";
    public static final String notificationChannelName = "TELLING_TAILES_CHANNEL_NAME";
    public static final String notificationGroupId = "com.telling.tailes.NOTIFICATION";

    public static final String fcmTokenError = "User returned from updateUserToken was null, token may not have been updated";

    public static final String sharedPreferenceFileUser = "user_preferences";
    public static final String sharedPreferenceKeyUsername = "username";
    public static final String sharedPreferenceKeyPassword = "password";
    public static final String preferenceKeyChangePassword = "change_password";
    public static final String sharedPreferenceKeyMessagingToken = "messagingToken";
    public static final String defaultPassword = "thisisnotsecure";

    public static final String authUtilsErrorMessageUserNull = "";
    public static final String authUtilsErrorMessageTokenUpdateFailure = "";

    public static final String fcmServerKey = "key=AAAAlPniYbs:APA91bGHtElSy0xiM_zOHTbq1GUggAVhRVhuQ5e2t-K-ww_W0cCsrJbuWj6wDq0_NYgHkFDhR1j1pM8cdK5kTaXJSO93ZDvGceLt-luvH6V9tCxIk5VDKlJKviEnc5Kz-vghIy8C4Jxr";
    public static final String fcm_uri = "https://fcm.googleapis.com/fcm/send";
    public static final String passwordHashAlgorithm = "SHA-512";

    public static final String drawableBell = "bell";
    public static final String drawableBookmarkOutline = "bookmark_outline";
    public static final String drawableBookmarkSolid = "bookmark_solid";
    public static final String drawableBookWithMark = "bookwithmark";
    public static final String drawableFavoriteOutline = "favorite_outline";
    public static final String drawableFavoriteSolid = "favorite_solid";
    public static final String drawableKey = "key";
    public static final String drawableLaurels = "laurels";
    public static final String drawableLogout = "logout";
    public static final String drawablePeople = "people";
    public static final String drawableText = "text";

    public static final String notificationPropertyTitle = "title";
    public static final String notificationPropertyBody = "body";
    public static final String notificationPropertyBadge = "badge";
    public static final String notificationPropertyContent = "content";
    public static final String notificationPropertyType = "type";
    public static final String notificationPropertyFollowerUsername = "followerUsername";
    public static final String notificationPropertyTo = "to";
    public static final String notificationPropertyPriority = "priority";
    public static final String notificationPropertyNotification = "notification";
    public static final String notificationPropertyData = "data";
    public static final String notificationPropertyPriorityValueHigh = "high";
    public static final String notificationPropertyBadgeValue1 = "1";

    public static final String httpPostMethod = "POST";
    public static final String httpContentType = "Content-Type";
    public static final String httpAuthorization = "Authorization";
    public static final String httpContentTypeJSON = "application/json";
    public static final String httpAuthorizationBearer = "Bearer";

    public static final String gptCompletionURI = "https://api.openai.com/v1/engines/text-davinci-002/completions";
    public static final String gptFilterURI = "https://api.openai.com/v1/engines/content-filter-alpha/completions";
    public static final String gptToken = "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";
    public static final String gptPropertyPrompt = "prompt";
    public static final String gptPropertyMaxTokens = "max_tokens";
    public static final String gptPropertyTopP = "top_p";
    public static final String gptPropertyTopLogProbs = "top_logprobs";
    public static final String gptPropertyLogProbs = "logprobs";
    public static final String gptPropertyTemperature = "temperature";
    public static final String gptPropertyChoices = "choices";
    public static final String gptPropertyText = "text";
    public static final String gptProbabilityLevel1 = "1";
    public static final String gptProbabilityLevel2 = "2";
    public static final String gptOpenAiURI = "https://openai.com/api/";

    public static final String filterSortPropertyId = "id";
    public static final String filterSortPropertyLoveCount = "loveCount";
    public static final String filterSortPropertyTimestamp = "timestamp";
    public static final String filterSortPropertyTitle = "title";
    public static final String gptUtilsErrorNoChoices = "Response contained no options";

    public static final String fbUtilsErrorNotification = "Failed to send notification";
    public static final String notificationTypeFollow = "follow";
    public static final String userModelBookmarkProperty = "bookmarks";

    public static final String gptCompletionStart = "<|endoftext|>";
    public static final String gptCompletionEnd = "\n--\nLabel:";

    public static final String storyDBKey = "stories";
    public static final String usersDBKey = "users";

    public static final String backgroundTaskResultType = "type";
    public static final String backgroundTaskResultResult = "result";
    public static final String backgroundTaskResultUsername = "username";
    public static final String backgroundTaskResultPassword = "password";
    public static final String backgroundTaskResultFollows = "follows";
    public static final String backgroundTaskResultCreateError = "createError";
    public static final String backgroundTaskResultLoginError = "loginError";
    public static final String backgroundTaskResultError = "error";
    public static final String backgroundTaskResultFollowed = "followed";

    public static final String backgroundTaskResultDataAuthorId = "authorId";
    public static final String backgroundTaskResultDataStoryCount = "storyCount";
    public static final String backgroundTaskResultDataLoveCount = "loveCount";
    public static final String backgroundTaskResultDataFollowCount = "followCount";
    public static final String backgroundTaskResultDataFollowing = "following";
    public static final String backgroundTaskResultDataProfileIcon = "profileIcon";
    public static final String backgroundTaskResultDataStory = "story";
    public static final String backgroundTaskResultDataStoryUnderscore = "story_";
    public static final String backgroundTaskResultDataPublished = "published";
    public static final String backgroundTaskResultDataLastType = "last_type";
    public static final String backgroundTaskResultDataTimeStamp = "timestamp";
    public static final String backgroundTaskResultDataLastStory = "last_story";
    public static final String backgroundTaskResultDataBookmarks = "bookmarks";

    public static final String authorProfileDialogFragment = "AuthorProfileDialogFragment";
    public static final String authorProfileFollowDialogFragment = "AuthorProfileDialogFragmentFollow";

    public static final String savedInstanceUsername = "username";
    public static final String savedInstancePassword = "password";
    public static final String savedInstancePasswordConfirmation = "passwordConfirmation";
    public static final String savedInstancePrompt = "prompt";
    public static final String savedInstanceProgress = "progress";
    public static final String savedInstanceStory = "story";
    public static final String savedInstanceTitle = "title";
    public static final String savedInstanceStoryId = "StoryId";

    public static final String voiceInputErrorTag = "Listening for voice input";
    public static final String voiceInputErrorMsg = "Voice input has result but no data - this is an anomalous result";
    public static final String newPasswordNullError = "Context was null, or new password is null";
    public static final String userNullError = "User was null";

    public static final String msgTrue = "true";
    public static final String doubleString = "double";
    public static final String stringString = "string";

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

    /*
        Helper method to read a string from an InputStream
     */
    public static String readString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        String res = StringUtils.emptyString;
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

        return res;
    }
}
