package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.model.Story;
import com.telling.tailes.util.AuthUtils;

public class ReadStoryActivity extends AppCompatActivity {

    private static final int storyTextSize = 20;

    private TextView titleTextView;
    private TextView authorTextView;
    private TextView storyTextView;
    private ImageButton bookmarkButton;
    private Button loveButton;
    private Button recycleButton;

    private Toast readStoryToast;

    private String promptText;
    private String storyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_story);

        Story story = (Story) getIntent().getSerializableExtra("story");

        //Find all views
        titleTextView = findViewById(R.id.storyCardTitle);
        authorTextView = findViewById(R.id.storyCardAuthor);
        storyTextView = findViewById(R.id.readStoryTextView);

        bookmarkButton = findViewById(R.id.storyCardBookmarkButton);
        loveButton = findViewById(R.id.storyCardLoveButton);
        recycleButton = findViewById(R.id.storyCardRecycleButton);

        readStoryToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        initViews(story);
        initListeners();
    }

    @SuppressLint("SetTextI18n")
    private void initViews(Story story) {
        //Make story text scrollable
        storyTextView.setMovementMethod(new ScrollingMovementMethod());
        storyTextView.setTextSize(storyTextSize);

        //Set up views with story data
        titleTextView.setText(story.getTitle());
        authorTextView.setText(story.getAuthorID());
        storyTextView.setText(story.getPromptText() + " " + story.getStoryText());

        //Set up private variables
        storyText = story.getStoryText();
        promptText = story.getPromptText();

        //TODO: set up love button default state based on data
        if (story.getLovers().contains(AuthUtils.getLoggedInUserID())) {
            loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
        } else {
           loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
        }

        //TODO: set up bookmark button default state
    }

    private void initListeners() {

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readStoryToast.setText("Clicked bookmark"); //TODO
                readStoryToast.show();
            }
        });

        loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readStoryToast.setText("Clicked love!"); //TODO
                readStoryToast.show();
            }
        });

        recycleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Navigate to the Create Story activity with a recycled prompt
                Intent intent = new Intent(getApplicationContext(),CreateStoryActivity.class);
                intent.putExtra("prompt",promptText);
                startActivity(intent);
            }
        });
    }
}