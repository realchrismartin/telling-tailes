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

    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final String NOTIFICATION_GROUP_ID = "com.telling.tailes.NOTIFICATION";

    @Override
    public void onNewToken(String newToken) {

        //Note: may not run in a separate thread if this class doesn't already
        AuthUtils.updateUserToken(getApplicationContext(), newToken, new Consumer<User>() {
            @Override
            public void accept(User user) {
                if(user == null) {
                    Log.e("MessagingService.onNewToken","User returned from updateUserToken was null, token may not have been updated");
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
                    type = "";
                    Log.e("Message Received", "FCM type member is null");
                }
                String storyId = remoteMessage.getData().get("storyID");
                if (storyId == null) {
                    storyId = "";
                    Log.e("Message Received", "FCM storyId member is null");
                }
                String followerUsername = remoteMessage.getData().get("followerUsername");
                if (followerUsername == null) {
                    followerUsername = "";
                    Log.e("Message Received", "FCM followerUsername is null");
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
            case ("publish"): {
                Log.d("message handler", "PUBLISH");
                // Create an Intent for the activity you want to start
                intent = new Intent(this, ReadStoryActivity.class);
                // add story here
                intent.putExtra("storyID", storyId);

                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(intent);

                notificationId = StringUtils.toIntegerId(storyId);

                // Get the PendingIntent containing the entire back stack
                pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                break;
            }
            case ("follow") : {
                Log.d("message handler", "follow");
                intent = new Intent(getApplicationContext(), StoryFeedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("feedFilter", "By Author");
                intent.putExtra("authorId", followerUsername);

                notificationId = StringUtils.toIntegerId(followerUsername);
                pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
                break;
            }

            case ("love"):
            default:
                Log.d("message handler", "default");
                intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                notificationId = StringUtils.toIntegerId(storyId);
                pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        //Actual notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(remoteMessageNotification.getTitle())
                .setContentText(remoteMessageNotification.getBody())
                .setColor(Color.argb(100, 100,100, 100))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.bookwithmark_pitch)
                .setContentIntent(pendingIntent)
                .setGroup(NOTIFICATION_GROUP_ID)
                .build();

        //Summary notification for compatibility with older versions of Android
        Notification summaryNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(remoteMessageNotification.getTitle())
                .setContentText("New Messages")
                .setSmallIcon(R.drawable.ic_baseline_favorite_story_24)
                .setGroup(NOTIFICATION_GROUP_ID)
                .setGroupSummary(true)
                .build();

        notificationManager.notify(notificationId, notification);
        notificationManager.notify(0,summaryNotification);
    }
}
