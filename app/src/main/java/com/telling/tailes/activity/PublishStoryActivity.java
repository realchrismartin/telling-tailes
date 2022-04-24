package com.telling.tailes.activity;

import androidx.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.R;
import com.telling.tailes.model.Story;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PublishStoryActivity extends AppCompatActivity {

    private static final String storyDBKey = "stories"; //TODO
    private static final int storyTextSize = 20;
    private static final int titleCharacterLength = 5;
    private String draftSaveNotification;
    private String genericErrorNotification;

    private DatabaseReference ref;
    private TextView storyTextView;
    private TextView titleView;
    private ProgressBar loadingWheel;
    private Toast toast;

    private String storyId;
    private String promptText;
    private String storyText;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private boolean published = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_story);

        storyId = ""; //Set once a draft is saved

        draftSaveNotification = getString(R.string.publish_story_draft_saved_notification);
        genericErrorNotification = getString(R.string.generic_error_notification);

        titleView = findViewById(R.id.titleTextView);
        loadingWheel = findViewById(R.id.storyPublishLoadingWheel);

        //Set up DB ref
        ref = FirebaseDatabase.getInstance().getReference().child(storyDBKey);

        //Set up toast
        toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        //Set up story text view
        storyTextView = findViewById(R.id.publishStoryTextView);

        //Make story text scrollable
        storyTextView.setMovementMethod(new ScrollingMovementMethod());
        storyTextView.setTextSize(storyTextSize);

        backgroundTaskExecutor = Executors.newFixedThreadPool(10);

        //Define handling for data results from the background thread
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            int i = 0;
            @Override
            public void handleMessage(Message msg) {
                if (msg.getData() == null) {
                    return;
                }

                String errorMsg = msg.getData().getString("error");

                if(errorMsg.length() > 0) {
                    toast.setText(errorMsg);
                    toast.show();
                }

                hideLoadingWheel();

                String publishMsg = msg.getData().getString("published");

                if (publishMsg != null && publishMsg.equals("true")) {
                    goToFeed();
                }
            }
        };

        //Load data from device rotation
        loadInstanceState(savedInstanceState);

        //Load data from intent passed here by CreateStoryActivity
        //Note that this will override anything loaded in from save state
        loadIntentData(getIntent());

        //Set the story text to whatever the prompt and story text are, post load
        storyTextView.setText(promptText + " " + storyText);

        //Define click handler for publishing a story
        findViewById(R.id.publishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(published || !validatePublishStory()) {
                    return;
                }

                handleClickPublish(false);
            }
        });

        findViewById(R.id.storyDeleteButton).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view)  {
               handleClickDeleteDraft();
               goToFeed();
           }
        });

        findViewById(R.id.storyRecycleButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClickDeleteDraft();
                handleClickRecycle();
            }
        });

        //If hasn't been saved as a draft, publish draft of this
        if(storyId.equals("") && storyText.length() > 0) {
           handleClickPublish(true);
        }
    }

    private void hideLoadingWheel() {
        loadingWheel.setVisibility(View.INVISIBLE);
    }

    private void showLoadingWheel() {
        loadingWheel.setVisibility(View.VISIBLE);
    }

    //Handle saving data on device rotation
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putString("story",storyText);
        state.putString("prompt",promptText);
        state.putString("title",titleView.getText().toString());
        state.putString("storyId",storyId);
        super.onSaveInstanceState(state);
    }

    //Handle loading data on activity creation, if any is saved
    protected void loadInstanceState(@NonNull Bundle state) {

        //Note: this is intentional, sometimes "nonnull" state is null for some reason
        if(state == null) {
            return;
        }

        storyText = state.getString("story");
        promptText = state.getString("prompt");
        titleView.setText(state.getString("title"));
        storyId = state.getString("storyId");

    }

    //Handle loading intent data
    protected void loadIntentData(Intent intent) {

        //Only load data if extras are present
        if(intent.hasExtra("storyId")) {
            storyId = intent.getStringExtra("storyId");
        }

        if(intent.hasExtra("story")) {
            storyText = intent.getStringExtra("story");
        }

        if(intent.hasExtra("prompt")) {
            promptText = intent.getStringExtra("prompt");
        }
    }

    /*
        Validate that story can be published
     */
    private boolean validatePublishStory() {
        boolean valid = true;

        String error = "";

        String title = titleView.getText().toString();

        if(title.length() < titleCharacterLength) {
            error = "Enter a title that is at least " + titleCharacterLength + " characters (currently using " + title.length() + " character(s))";
        }

        if(error.length() > 0 ) {
            valid = false;
            toast.setText(error);
            toast.show();
        }

        return valid;
    }

    //Handle user clicking recycle
    private void handleClickRecycle() {
        //Navigate to the Create Story activity with a recycled prompt
        Intent intent = new Intent(getApplicationContext(),CreateStoryActivity.class);
        intent.putExtra("prompt",promptText);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //Handle user clicking delete draft
    private void handleClickDeleteDraft() {

        if(storyId.equals("")) {
            return;
        }

        showLoadingWheel();

        backgroundTaskExecutor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        Task<Void> storyDeleteTask = ref.child(storyId).removeValue();

                        storyDeleteTask.addOnFailureListener(task -> {
                            Bundle resultData = new Bundle();
                            resultData.putString("error", "");

                            Message resultMessage = new Message();
                            resultMessage.setData(resultData);

                            backgroundTaskResultHandler.sendMessage(resultMessage);
                        });
                    }
                }
        );


    }

    /*
        Handle user clicking publish
        Publish a story to the feed, or save a draft, then redirect to the feed if successful (if not drafting)
     */
    private void handleClickPublish(boolean asDraft) {

        if(!AuthUtils.userIsLoggedIn(getApplicationContext()))
        {
            return;
        }

        String title = titleView.getText().toString();
        String userId = AuthUtils.getLoggedInUserID(getApplicationContext());
        ArrayList<String> lovers = new ArrayList<String>();
        ArrayList<String> bookmarkers = new ArrayList<String>();

        if(storyId.equals("")) {
            storyId = new Date().toString().replace(" ","") + "-" + userId;
        }

        //Ensure that title is always entered, even if it's a draft
        if(title.length() <= 0) {
            title = storyId;
        }

        Story story = new Story(storyId,userId,asDraft,title,promptText,storyText,lovers, bookmarkers,0,System.currentTimeMillis());

        backgroundTaskExecutor.execute(
            new Runnable() {
               @Override
           public void run() {

               Task<Void> storyPublishTask = ref.child(story.getId()).setValue(story);

               storyPublishTask.addOnCompleteListener(task -> {

                   //Only increment story count if this isn't a draft
                   if(asDraft) {
                       Bundle resultData = new Bundle();
                       resultData.putString("error", "");

                       Message resultMessage = new Message();
                       resultMessage.setData(resultData);

                       backgroundTaskResultHandler.sendMessage(resultMessage);
                       return;
                   }

                   published = true;

                   FBUtils.getUser(getApplicationContext(), AuthUtils.getLoggedInUserID(getApplicationContext()), new Consumer<User>() {
                       @Override
                       public void accept(User user) {

                           if(user == null) {
                               Bundle resultData = new Bundle();
                               resultData.putString("error", "Failed to get user"); //TODO

                               Message resultMessage = new Message();
                               resultMessage.setData(resultData);

                               backgroundTaskResultHandler.sendMessage(resultMessage);
                              return;
                           }

                           user.incrementStoryCount();
                           FBUtils.updateUser(getApplicationContext(), user, new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean result) {

                                   if(!result) {
                                       Bundle resultData = new Bundle();
                                       resultData.putString("error", "Failed to update user after publish"); //TODO

                                       Message resultMessage = new Message();
                                       resultMessage.setData(resultData);

                                       backgroundTaskResultHandler.sendMessage(resultMessage);
                                       return;
                                   }

                                   //TODO:
                                   FBUtils.sendNotification(getApplicationContext(), user.getUsername(), "test", "test", "test", new Consumer<Boolean>() {
                                       @Override
                                       public void accept(Boolean messageResult) {
                                           Bundle resultData = new Bundle();
                                           resultData.putString("error", messageResult ? "" : getString(R.string.generic_error_notification));
                                           resultData.putString("published", "true");

                                           //Send meesage rom thresd
                                           Message resultMessage = new Message();
                                           resultMessage.setData(resultData);

                                           backgroundTaskResultHandler.sendMessage(resultMessage);
                                       }
                                   });
                               }
                           });
                       }
                   });
               });

               storyPublishTask.addOnFailureListener(task -> {
                   toast.setText(genericErrorNotification);
                   toast.show();
                   published = false;
               });
           }
       });
    }

    /*
        Go back to the feed after having published a story
     */
    private void goToFeed() {
        Intent intent = new Intent(this,StoryFeedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}