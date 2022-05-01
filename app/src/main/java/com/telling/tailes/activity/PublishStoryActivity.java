package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.R;
import com.telling.tailes.model.Story;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;
import com.telling.tailes.util.FloatingActionMenuUtil;
import com.telling.tailes.util.GPTUtils;
import com.telling.tailes.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PublishStoryActivity extends AppCompatActivity {

    private static final int storyTextSize = 20;
    private static final int titleCharacterLength = 5;
    private String draftSaveNotification;
    private String genericErrorNotification;

    private DatabaseReference ref;

    private TextView titleView;
    private TextView storyTextView;
    private TextView promptTextView;
    private ProgressBar loadingWheel;
    private Toast toast;

    private String storyId;
    private String promptText;
    private String storyText;
    private String lastStoryChunk;

    private boolean isFamOpen;
    private FloatingActionButton deleteFAB;
    private FloatingActionButton recycleFAB;
    private FloatingActionButton extendFAB;
    private FloatingActionButton famMenu;
    private ArrayList<FloatingActionButton> famList;

    private Button publishButton;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private boolean published = false;

    private boolean unsavedChanges = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_story);

        storyId = StringUtils.emptyString; //Set once a draft is saved

        draftSaveNotification = getString(R.string.publish_story_draft_saved_notification);
        genericErrorNotification = getString(R.string.generic_error_notification);

        titleView = findViewById(R.id.titleEditText);
        loadingWheel = findViewById(R.id.storyPublishLoadingWheel);
        loadingWheel.setVisibility(View.INVISIBLE);

        //Set up DB ref
        ref = FirebaseDatabase.getInstance().getReference().child(StringUtils.storyDBKey);

        //Set up toast
        toast = Toast.makeText(getApplicationContext(), StringUtils.emptyString,Toast.LENGTH_SHORT);

        //Set up prompt text view
        promptTextView = findViewById(R.id.publishPromptTextView);

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

                String errorMsg = msg.getData().getString(StringUtils.backgroundTaskResultError);

                hideLoadingWheel();

                if(errorMsg != null && errorMsg.length() > 0) {
                    toast.setText(errorMsg);
                    toast.show();
                    return;
                }

                String type = msg.getData().getString(StringUtils.backgroundTaskResultType) != null ?
                        msg.getData().getString(StringUtils.backgroundTaskResultType)
                        :
                        StringUtils.backgroundResultPropertyPublish;

                switch(type) {
                    case(StringUtils.backgroundResultPropertyStoryData) : {
                        lastStoryChunk = msg.getData().getString(StringUtils.backgroundTaskResultDataStory);
                        storyText += lastStoryChunk;
                        storyTextView.setText(storyText);
                        handleClickPublish(true);
                        break;
                    }
                    case(StringUtils.backgroundResultPropertyPublish) : {
                        String publishMsg = msg.getData().getString(StringUtils.backgroundTaskResultDataPublished);

                        if (publishMsg != null && publishMsg.equals(StringUtils.msgTrue)) {
                            goToFeed();
                        }
                        break;
                    }
                }

            }
        };

        //Load data from device rotation
        loadInstanceState(savedInstanceState);

        //Load data from intent passed here by CreateStoryActivity
        //Note that this will override anything loaded in from save state
        loadIntentData(getIntent());

        //Set the story text and prompt text independently
        promptTextView.setText(promptText);
        storyTextView.setText(storyText);
        lastStoryChunk = storyText;

        publishButton = findViewById(R.id.publishButton);
        deleteFAB = findViewById(R.id.publishDeleteFAB);
        recycleFAB = findViewById(R.id.publishRecycleFAB);
        extendFAB = findViewById(R.id.publishExtendFAB);

        famList = new ArrayList<>();
        famList.add(extendFAB);
        famList.add(recycleFAB);
        famList.add(deleteFAB);
        isFamOpen = false;

        famMenu = findViewById(R.id.famFAB);
        famMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFamOpen = FloatingActionMenuUtil.toggleFAM(isFamOpen, famList);
                if (isFamOpen) {
                    famMenu.setImageResource(R.drawable.expand_down_white);
                    return;
                }
                famMenu.setImageResource(R.drawable.expand_up_white);
            }
        });

        storyTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                storyText = storyTextView.getText().toString();
                unsavedChanges = true;
            }
        });

        //Define click handler for publishing a story
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(published || !validatePublishStory()) {
                    return;
                }

                handleClickPublish(false);
            }
        });

        deleteFAB.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view)  {
               handleClickDeleteDraft();
               goToFeed();
           }
        });

        recycleFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClickDeleteDraft();
                handleClickRecycle();
            }
        });

        extendFAB.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view)   {
             handleAppendText();
          }
        });

        //If hasn't been saved as a draft, publish draft of this
        if(storyId.equals(StringUtils.emptyString) && storyText.length() > 0) {
           handleClickPublish(true);
        }
    }

    private void hideLoadingWheel() {
        loadingWheel.setVisibility(View.INVISIBLE);
    }

    private void showLoadingWheel() {
        loadingWheel.setVisibility(View.VISIBLE);
    }

    //Handle saving draft on destroy if there's unsaved data
    @Override
    protected void onDestroy() {
       super.onDestroy();
       if(unsavedChanges) {
           handleClickPublish(true);
       }
    }

    //Handle saving data on device rotation
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putString(StringUtils.savedInstanceStory, storyText);
        state.putString(StringUtils.savedInstancePrompt, promptText);
        state.putString(StringUtils.savedInstanceTitle, titleView.getText().toString());
        state.putString(StringUtils.savedInstanceStoryId, storyId);
        super.onSaveInstanceState(state);
    }

    //Handle loading data on activity creation, if any is saved
    protected void loadInstanceState(@NonNull Bundle state) {

        //Note: this is intentional, sometimes "nonnull" state is null for some reason
        if(state == null) {
            return;
        }

        storyText = state.getString(StringUtils.savedInstanceStory);
        promptText = state.getString(StringUtils.savedInstancePrompt);
        titleView.setText(state.getString(StringUtils.savedInstanceTitle));
        storyId = state.getString(StringUtils.savedInstanceStoryId);

    }

    //Handle loading intent data
    protected void loadIntentData(Intent intent) {

        //Only load data if extras are present
        if(intent.hasExtra(StringUtils.intentExtraStoryId)) {
            storyId = intent.getStringExtra(StringUtils.intentExtraStoryId);
        }

        if(intent.hasExtra(StringUtils.intentExtraStory)) {
            storyText = intent.getStringExtra(StringUtils.intentExtraStory);
        }

        if(intent.hasExtra(StringUtils.intentExtraPrompt)) {
            promptText = intent.getStringExtra(StringUtils.intentExtraPrompt);
        }
    }

    /*
        Validate that story can be published
     */
    private boolean validatePublishStory() {
        boolean valid = true;

        String error = StringUtils.emptyString;

        String title = titleView.getText().toString();

        if(title.length() < titleCharacterLength) {
            error = getString(R.string.title_too_short_1) + titleCharacterLength + getString(R.string.title_too_short_2) + title.length() + getString(R.string.title_too_short_3);
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
        intent.putExtra(StringUtils.intentExtraPrompt, promptText);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //Handle user clicking delete draft
    private void handleClickDeleteDraft() {

        if(storyId.equals(StringUtils.emptyString)) {
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
                        resultData.putString(StringUtils.backgroundTaskResultError, StringUtils.emptyString);
                        resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyPublish);

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

        unsavedChanges = false;

        String title = titleView.getText().toString();
        String userId = AuthUtils.getLoggedInUserID(getApplicationContext());
        ArrayList<String> lovers = new ArrayList<String>();
        ArrayList<String> bookmarkers = new ArrayList<String>();

        if(storyId.equals(StringUtils.emptyString)) {
            storyId = new Date().toString().replace(StringUtils.space, StringUtils.emptyString) + StringUtils.hyphen + userId;
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
                       resultData.putString(StringUtils.backgroundTaskResultError, StringUtils.emptyString);

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
                               resultData.putString(StringUtils.backgroundTaskResultError, getString(R.string.user_get_error));

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
                                       resultData.putString(StringUtils.backgroundTaskResultError, getString(R.string.user_update_error));

                                       Message resultMessage = new Message();
                                       resultMessage.setData(resultData);

                                       backgroundTaskResultHandler.sendMessage(resultMessage);
                                       return;
                                   }

                                   String body = user.getUsername() + StringUtils.space + getString(R.string.message_published_story_body) + StringUtils.colon + StringUtils.space + story.getTitle();

                                   FBUtils.sendNotificationToFollowers(getApplicationContext(), user.getUsername(), getString(R.string.message_published_story), body, StringUtils.emptyString, StringUtils.backgroundResultPropertyPublish, storyId, new Consumer<Boolean>() {
                                       @Override
                                       public void accept(Boolean messageResult) {
                                           Bundle resultData = new Bundle();
                                           resultData.putString(StringUtils.backgroundTaskResultError, messageResult ? StringUtils.emptyString : getString(R.string.generic_error_notification));
                                           resultData.putString(StringUtils.backgroundTaskResultDataPublished, StringUtils.msgTrue);

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
     */
    private void handleAppendText()
    {
        showLoadingWheel();

        unsavedChanges = false;

        String inputText = promptText + storyText;

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                //Ask GPT to complete the prompt... again
                String story = GPTUtils.getStory(getApplicationContext(), inputText, 25); //Note: hardcoded additional length due to minimum length being too short
                int resultCode = story.length() <= 0 ? 1 : 0;

                //Set up a bundle
                //Result code != 0 means something in GPT failed
                Bundle resultData = new Bundle();
                resultData.putInt(StringUtils.backgroundTaskResultResult, resultCode);
                resultData.putString(StringUtils.backgroundTaskResultDataStory, story);
                resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyStoryData);

                Message resultMessage = new Message();
                resultMessage.setData(resultData);

                //Notify the activity that the API call is done
                backgroundTaskResultHandler.sendMessage(resultMessage);
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