package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.telling.tailes.R;

public class PublishStoryActivity extends AppCompatActivity {

    private static final int storyTextSize = 20;

    private TextView storyTextView;
    private TextView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_story);

        storyTextView = findViewById(R.id.storyTextView);
        titleView = findViewById(R.id.titleTextView);

        //Make story text scrolly.
        storyTextView.setMovementMethod(new ScrollingMovementMethod());
        storyTextView.setTextSize(storyTextSize);

        //Load data from intent passed here by CreateStoryActivity
        loadIntentData();
    }

    /*
        Load intent data and display it
     */
    private void loadIntentData()
    {
        Intent fromStoryCreate = getIntent();
        String story = fromStoryCreate.getStringExtra(Intent.EXTRA_TEXT);
        storyTextView.setText(story);
    }
}