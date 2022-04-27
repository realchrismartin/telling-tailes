package com.telling.tailes.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.telling.tailes.R;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.function.Consumer;

public class MessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "CHANNEL_ID";

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
                showNotification(notification);
            }
        }
    }

    private void showNotification(RemoteMessage.Notification remoteMessageNotification) {

        Intent intent = new Intent(); //TODO?
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

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
}
