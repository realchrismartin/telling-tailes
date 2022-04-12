package com.telling.tailes.util;

import android.content.Context;
import android.content.Intent;

import com.telling.tailes.activity.CreateStoryActivity;
import com.telling.tailes.activity.StoryFeedActivity;

public class IntentUtils {

    public static void openStoryFeedActivity(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), StoryFeedActivity.class);
        context.startActivity(intent);
    }

    public static void openCreateStoryActivity(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), CreateStoryActivity.class);
        context.startActivity(intent);
    }
}
