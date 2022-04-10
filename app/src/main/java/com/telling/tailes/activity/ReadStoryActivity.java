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
import com.telling.tailes.util.FBUtils;

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
    private Story story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_story);

        story = (Story) getIntent().getSerializableExtra("story");

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

        //Set love button default state
        updateLoveButtonState();

        //Set bookmark button default state
        updateBookmarkButtonState();
    }

    private void initListeners() {

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClickBookmark();
            }
        });

        loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClickLove();
            }
        });

        recycleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClickRecycle();

            }
        });
    }

   private void updateLoveButtonState() {
        if (story.getLovers().contains(AuthUtils.getLoggedInUserID())) {
            loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
        } else {
            loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
        }
    }

    private void updateBookmarkButtonState() {
        //TODO: set the bookmark state according to the Story - like with Love button
    }

    private void handleClickBookmark() {
        //TODO: handle clicking on a bookmark doing stuff in FB, etc., then updating the Story
    }

    private void handleClickRecycle() {
        //Navigate to the Create Story activity with a recycled prompt
        Intent intent = new Intent(getApplicationContext(),CreateStoryActivity.class);
        intent.putExtra("prompt",promptText);
        startActivity(intent);
    }

    private void handleClickLove() {
        FBUtils.updateLove(story);

        //TODO: doesn't handle async properly. This will not work.
        updateLoveButtonState();
    }
}