package com.telling.tailes.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.telling.tailes.R;
import com.telling.tailes.activity.ReadStoryActivity;
import com.telling.tailes.activity.StoryFeedActivity;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.StringUtils;

import java.util.function.Consumer;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String newToken) {

        //Note: may not run in a separate thread if this class doesn't already
        AuthUtils.updateUserToken(getApplicationContext(), newToken, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if(user == null) {
                    Log.e(StringUtils.messagingServiceTag,StringUtils.fcmTokenError);
                }
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        boolean showNotification = true;

        try {
            showNotification = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.setting_hide_title),true);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if (showNotification && remoteMessage.getData().size() > 0) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            if(notification != null) {
                String type = remoteMessage.getData().get("type");
                if (type == null) {
                    type = StringUtils.emptyString;
                    Log.e(StringUtils.messagingServiceTag, StringUtils.messagingServiceErrorType);
                }
                String storyId = remoteMessage.getData().get(StringUtils.intentExtraStoryId);
                if (storyId == null) {
                    storyId = StringUtils.emptyString;
                    Log.e(StringUtils.messagingServiceTag, StringUtils.messagingServiceErrorStoryId);
                }
                String followerUsername = remoteMessage.getData().get(StringUtils.intentExtraFollowerUsername);
                if (followerUsername == null) {
                    followerUsername = StringUtils.emptyString;
                    Log.e(StringUtils.messagingServiceTag, StringUtils.messagingServiceErrorFollowerUsername);
                }

                showNotification(notification, type, storyId, followerUsername);
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void showNotification(RemoteMessage.Notification remoteMessageNotification, String type, String storyId, String followerUsername) {

        Intent intent;
        PendingIntent pendingIntent;

        int notificationId = 1; //Starts at 0 because summary notification id is 0 and must be unique

        switch (type) {
            case("love"):
            case ("publish"): {
                // Create an Intent for the activity you want to start
                intent = new Intent(this, ReadStoryActivity.class);
                intent.putExtra(StringUtils.intentExtraStoryId, storyId);

                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(intent);

                notificationId = StringUtils.toIntegerId(storyId);

                // Get the PendingIntent containing the entire back stack
                pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                break;
            }
            case ("follow") : {
                intent = new Intent(getApplicationContext(), StoryFeedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(StringUtils.intentExtraFeedFilter, StringUtils.filterTypeByAuthor);
                intent.putExtra(StringUtils.intentExtraAuthorId, followerUsername);

                notificationId = StringUtils.toIntegerId(followerUsername);
                pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
                break;
            }


            default:
                intent = new Intent(getApplicationContext(), StoryFeedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                notificationId = StringUtils.toIntegerId(storyId);
                pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        //Actual notification
        Notification notification = new NotificationCompat.Builder(this, StringUtils.notificationChannelId)
                .setContentTitle(remoteMessageNotification.getTitle())
                .setContentText(remoteMessageNotification.getBody())
                .setColor(Color.argb(100, 100,100, 100))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.bookwithmark_color)
                .setContentIntent(pendingIntent)
                .setGroup(StringUtils.notificationGroupId)
                .build();

        //Summary notification for compatibility with older versions of Android
        Notification summaryNotification = new NotificationCompat.Builder(this, StringUtils.notificationChannelId)
                .setContentTitle(remoteMessageNotification.getTitle())
                .setContentText("New Messages")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.bookwithmark_color)
                .setGroup(StringUtils.notificationGroupId)
                .setGroupSummary(true)
                .build();

        notificationManager.notify(notificationId, notification);
        notificationManager.notify(0,summaryNotification);
    }
}
