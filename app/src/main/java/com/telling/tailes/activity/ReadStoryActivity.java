package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.telling.tailes.R;
import com.telling.tailes.model.Story;

public class ReadStoryActivity extends AppCompatActivity {

    private static final int storyTextSize = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_story);

        Story story = (Story)getIntent().getSerializableExtra("story");

        //Set story text view
        TextView storyTextView = findViewById(R.id.readStoryTextView);

        //Make story text scrollable
        storyTextView.setMovementMethod(new ScrollingMovementMethod());
        storyTextView.setTextSize(storyTextSize);

        //Set story text
        storyTextView.setText(story.getStoryText());
    }
}