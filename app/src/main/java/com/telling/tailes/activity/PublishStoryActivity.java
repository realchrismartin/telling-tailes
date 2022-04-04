package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.telling.tailes.R;

public class PublishStoryActivity extends AppCompatActivity {

    private TextView storyTextView;
    private TextView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_story);

        storyTextView = findViewById(R.id.storyTextView);
        titleView = findViewById(R.id.titleTextView);
    }
}