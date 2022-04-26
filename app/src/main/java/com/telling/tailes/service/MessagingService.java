package com.telling.tailes.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.telling.tailes.R;
import com.telling.tailes.activity.ReadStoryActivity;
import com.telling.tailes.model.Story;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "CHANNEL_ID";
    private Story story = null;

    @Override
    public void onNewToken(String newToken) {

        //Note: not in background thread
        FBUtils.getUser(getApplicationContext(), AuthUtils.getLoggedInUserID(getApplicationContext()), new Consumer<User>() {
            @Override
            public void accept(User user) {

               if(user == null) {
                   Log.e("MessageService","User was null, couldn't update token");
                   return;
               }

               user.setMessagingToken(newToken);

               FBUtils.updateUser(getApplicationContext(), user, new Consumer<Boolean>() {
                   @Override
                   public void accept(Boolean aBoolean) {
                      if(!aBoolean)  {
                          Log.e("MessageService","Failed to update user token for current user");
                      }
                   }
               });
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
                String storyId = remoteMessage.getData().get("storyId");
                if (storyId == null) {
                    storyId = "";
                    Log.e("Message Received", "FCM storyId member is null");
                }
                showNotification(notification, type, storyId);
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void showNotification(RemoteMessage.Notification remoteMessageNotification, String type, String storyId) {

        Intent intent;
        PendingIntent pendingIntent;

        switch (type) {
            case ("publish"): {
                Log.d("message handler", "PUBLISH");
                // Create an Intent for the activity you want to start
                intent = new Intent(this, ReadStoryActivity.class);
                // add story here
                getStoryOffMainThread(storyId);
                intent.putExtra("story", story);

                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(intent);
                // Get the PendingIntent containing the entire back stack
                pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                break;
            }
            case ("follow") :
            case ("love"):
            default:
                Log.d("message handler", "default");
                intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
        }

        Notification notification;

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        notification = builder.setContentTitle(remoteMessageNotification.getTitle())
                .setContentText(remoteMessageNotification.getBody())
                .setColor(Color.argb(100, 100,100, 100))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_baseline_favorite_story_24)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(0, notification);
    }

    private void getStoryOffMainThread(String storyId) {
        Executor backgroundTaskExecutor = Executors.newFixedThreadPool(2);
        Handler backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                story = (Story) msg.getData().getSerializable("story");
            }
        };

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FBUtils.getStory(getApplicationContext(), storyId, new Consumer<Story>() {
                    @Override
                    public void accept(Story story) {
                        //Set up a bundle
                        Bundle resultData = new Bundle();
                        resultData.putString("type", "story");
                        resultData.putSerializable("story", story);
                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);

                        //Notify the activity that bookmarks have been retrieved
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }

/*    private Story getStoryHelper(Story story) {
        return story;
    }*/
}
