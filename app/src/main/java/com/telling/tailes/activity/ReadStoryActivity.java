package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.telling.tailes.R;
import com.telling.tailes.fragment.AuthorProfileDialogFragment;
import com.telling.tailes.model.AuthorProfile;
import com.telling.tailes.model.Story;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;
import com.telling.tailes.util.StringUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReadStoryActivity extends AppCompatActivity {

    private static final int storyTextDefaultSize = 24; //Default text size if not overridden by prefs

    private TextView titleTextView;
    private TextView storyTextView;
    private TextView promptTextView;
    private ImageButton bookmarkButton;
    private Button loveButton;
    private FloatingActionButton recycleFAB;
    private Button authorProfileButton;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;
    private AuthorProfileDialogFragment authorProfileDialogFragment;

    private Toast readStoryToast;

    private String promptText;
    private Story story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_story);

        //Find all views
        titleTextView = findViewById(R.id.storyCardTitle);
        storyTextView = findViewById(R.id.readStoryTextView);
        promptTextView = findViewById(R.id.readPrompTextView);

        bookmarkButton = findViewById(R.id.storyCardBookmarkButton);
        loveButton = findViewById(R.id.storyCardLoveButton);
        recycleFAB = findViewById(R.id.recyclePromptFAB);
        authorProfileButton = findViewById(R.id.storyCardAuthorProfileButton);

        readStoryToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        //Set up background executor for handling author profile data request threads
        backgroundTaskExecutor = Executors.newFixedThreadPool(2);

        //Define handling for results from the background thread
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                //Show a generic error instead if data wasn't retrieved properly
                if (msg.getData() == null || msg.getData().getInt(StringUtils.backgroundTaskResultResult) > 0) {
                    readStoryToast.setText(R.string.generic_error_notification);
                    readStoryToast.show();
                    return;
                }

                String type = msg.getData().getString(StringUtils.backgroundTaskResultType);

                switch(type) {
                    case (StringUtils.backgroundResultPropertyAuthorProfile): {
                        if (authorProfileDialogFragment != null) {
                            authorProfileDialogFragment.dismiss();
                        }
                        authorProfileDialogFragment = new AuthorProfileDialogFragment();
                        authorProfileDialogFragment.setArguments(msg.getData());
                        authorProfileDialogFragment.show(getSupportFragmentManager(), StringUtils.authorProfileDialogFragment);
                        break;
                    }

                    case (StringUtils.backgroundResultPropertyStoryLove): {
                        story = (Story)msg.getData().getSerializable(StringUtils.backgroundTaskResultDataStory);
                        updateLoveButtonState();
                        break;
                    }

                    case (StringUtils.backgroundResultPropertyStoryBookmark) : {
                        story = (Story)msg.getData().getSerializable(StringUtils.backgroundTaskResultDataStory);
                        updateBookmarkButtonState();
                        break;
                    }

                    case (StringUtils.backgroundResultPropertyStory) : {
                        story = (Story) msg.getData().getSerializable(StringUtils.backgroundTaskResultDataStory);
                        initViews(story);
                        break;

                    }
                }
            }
        };

        if (getIntent().getSerializableExtra(StringUtils.backgroundTaskResultDataStory) !=null) {
            story = (Story) getIntent().getSerializableExtra(StringUtils.backgroundTaskResultDataStory);
            initViews(story);
        }
        if (getIntent().getStringExtra(StringUtils.intentExtraStoryId)!= null) {
            handleStory(getIntent().getStringExtra(StringUtils.intentExtraStoryId));
        }

        initListeners();
    }

    /*
        handler for fetching story from FireBase if coming from intent not story feed
    */
    private void handleStory(String storyID) {
        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FBUtils.getStory(getApplicationContext(), storyID, new Consumer<Story>() {
                    @Override
                    public void accept(Story story) {
                        Bundle resultData = new Bundle();
                        resultData.putSerializable(StringUtils.backgroundTaskResultDataStory, story);
                        resultData.putInt(StringUtils.backgroundTaskResultResult, story ==null? 1:0);
                        resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyStory);

                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initViews(Story story) {

        //Set font size to preference setting
        try {
            promptTextView.setTextSize(PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.setting_text_size_title),storyTextDefaultSize));
            storyTextView.setTextSize(PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.setting_text_size_title),storyTextDefaultSize));
        } catch(Exception ex) {
            ex.printStackTrace();
            promptTextView.setTextSize(storyTextDefaultSize);
            storyTextView.setTextSize(storyTextDefaultSize);
        }

        //Set up views with story data
        titleTextView.setText(story.getTitle());
        authorProfileButton.setText(story.getAuthorID());
        String p = story.getPromptText();
        promptTextView.setText(p);
        String s = story.getStoryText();
        storyTextView.setText(s);

        //Set up private variables
        String storyText = story.getStoryText();
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

        recycleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClickRecycle();
            }
        });

        authorProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAuthorClick();
            }
        });
    }

   private void updateLoveButtonState() {
        if (story.getLovers().contains(AuthUtils.getLoggedInUserID(getApplicationContext()))) {
            loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_solid_color, 0, 0, 0);
        } else {
            loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite_outline_color, 0, 0, 0);
        }
    }

    private void updateBookmarkButtonState() {
        if (story.getBookmarkers().contains(AuthUtils.getLoggedInUserID(getApplicationContext()))) {
            bookmarkButton.setImageResource(R.drawable.bookmark_solid_color);
        } else {
            bookmarkButton.setImageResource(R.drawable.bookmark_outline_color);
        }
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
                        resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyAuthorProfile);
                        resultData.putInt(StringUtils.backgroundTaskResultResult, authorProfile != null ? 0 : 1); //If authorProfile, there's some issue - handle error

                        if (authorProfile != null) {
                            resultData.putString(StringUtils.backgroundTaskResultDataAuthorId, authorProfile.getAuthorId());
                            resultData.putInt(StringUtils.backgroundTaskResultDataStoryCount, authorProfile.getStoryCount());
                            resultData.putInt(StringUtils.backgroundTaskResultDataLoveCount, authorProfile.getLoveCount());
                            resultData.putInt(StringUtils.backgroundTaskResultDataFollowCount, authorProfile.getFollowCount());
                            resultData.putInt(StringUtils.backgroundTaskResultDataProfileIcon,authorProfile.getProfileIcon());
                            resultData.putBoolean(StringUtils.backgroundTaskResultDataFollowing, authorProfile.following());
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

    private void handleClickBookmark() {
        backgroundTaskExecutor.execute(new Runnable() {
               @Override
               public void run() {

                   FBUtils.updateBookmark(getApplicationContext(), story, new Consumer<Story>() {
                       @Override
                       public void accept(Story result) {
                           Bundle resultData = new Bundle();
                           resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyStoryBookmark);
                           resultData.putInt(StringUtils.backgroundTaskResultResult, result != null ? 0 : 1);
                           resultData.putSerializable(StringUtils.backgroundTaskResultDataStory, result);

                           Message resultMessage = new Message();
                           resultMessage.setData(resultData);

                           backgroundTaskResultHandler.sendMessage(resultMessage);
                       }
                   });
               }
           });
    }

    private void handleClickRecycle() {
        //Navigate to the Create Story activity with a recycled prompt
        Intent intent = new Intent(getApplicationContext(),CreateStoryActivity.class);
        intent.putExtra(StringUtils.intentExtraPrompt, promptText);
        startActivity(intent);
    }

    private void handleClickLove() {

        if(AuthUtils.getLoggedInUserID(getApplicationContext()).equals(story.getAuthorID())) {
            readStoryToast.setText(R.string.love_own_story_error);
            readStoryToast.show();
            return;
        }

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                FBUtils.updateLove(getApplicationContext(), story, new Consumer<Story>() {
                    @Override
                    public void accept(Story result) {

                        //Set up a bundle of data
                        Bundle resultData = new Bundle();
                        resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyStoryLove);
                        resultData.putInt(StringUtils.backgroundTaskResultResult, result != null ? 0 : 1);
                        resultData.putSerializable(StringUtils.backgroundTaskResultDataStory, result);

                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);

                        backgroundTaskResultHandler.sendMessage(resultMessage);

                    }
                });
            }
        });
    }
}