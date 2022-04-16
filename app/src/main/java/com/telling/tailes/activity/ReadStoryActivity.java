package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.fragment.AuthorProfileDialogFragment;
import com.telling.tailes.model.AuthorProfile;
import com.telling.tailes.model.Story;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReadStoryActivity extends AppCompatActivity {

    private static final int storyTextSize = 20;

    private TextView titleTextView;
    private TextView authorTextView;
    private TextView storyTextView;
    private ImageButton bookmarkButton;
    private Button loveButton;
    private Button recycleButton;
    private Button authorProfileButton;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;
    private AuthorProfileDialogFragment authorProfileDialogFragment;

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
        authorProfileButton = findViewById(R.id.storyCardAuthorProfileButton);

        readStoryToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        //Set up background executor for handling author profile data request threads
        backgroundTaskExecutor = Executors.newFixedThreadPool(2);

        //Define handling for author profile data results from the background thread
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if (authorProfileDialogFragment != null) {
                    authorProfileDialogFragment.dismiss();
                }

                //Show a generic error instead of loading author profile if data wasn't retrieved properly
                if (msg.getData() == null || msg.getData().getInt("result") > 0) {
                    readStoryToast.setText(R.string.generic_error_notification);
                    readStoryToast.show();
                    return;
                }

                //If all is well, show the author profile fragment with the retrieved data
                authorProfileDialogFragment = new AuthorProfileDialogFragment();
                authorProfileDialogFragment.setArguments(msg.getData());
                authorProfileDialogFragment.show(getSupportFragmentManager(),"AuthorProfileDialogFragment");

            }
        };

        //Define onClick handler for opening author profile
        authorProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               handleAuthorClick();
            }
        });

        initViews(story);
        initListeners();
    }

    /*
        Card onClick handler for opening an author profile
     */
    public void handleAuthorClick()
    {
        String username = story.getAuthorID();

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                FBUtils.getAuthorProfile(getApplicationContext(),username, new Consumer<AuthorProfile>() {
                    @Override
                    public void accept(AuthorProfile authorProfile) {

                        //Set up a bundle of author profile result data
                        Bundle resultData = new Bundle();
                        resultData.putString("type", "authorProfile");
                        resultData.putInt("result", authorProfile != null ? 0 : 1); //If authorProfile, there's some issue - handle error

                        if (authorProfile != null) {
                            resultData.putString("authorId", authorProfile.getAuthorId());
                            resultData.putInt("storyCount", authorProfile.getStoryCount());
                            resultData.putInt("loveCount", authorProfile.getLoveCount());
                            resultData.putBoolean("following", authorProfile.following());
                        }

                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);

                        //Notify the activity that profile data has been retrieved
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
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
        if (story.getLovers().contains(AuthUtils.getLoggedInUserID(getApplicationContext()))) {
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
        FBUtils.updateLove(getApplicationContext(), story, new Consumer<Story>() {
            @Override
            public void accept(Story result) {
                if(result == null) {
                    readStoryToast.setText(R.string.generic_error_notification);
                } else {
                    story = result;
                    updateLoveButtonState();
                }
            }
        });
    }
}