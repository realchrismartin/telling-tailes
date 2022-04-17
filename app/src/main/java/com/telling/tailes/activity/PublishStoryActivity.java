package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
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
    private Toast toast;

    private String promptText;
    private String storyText;

    private boolean published = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_story);

        draftSaveNotification = getString(R.string.publish_story_draft_saved_notification);
        genericErrorNotification = getString(R.string.generic_error_notification);

        titleView = findViewById(R.id.titleTextView);

        //Set up DB ref
        ref = FirebaseDatabase.getInstance().getReference().child(storyDBKey);

        //Set up toast
        toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        //Set up story text view
        storyTextView = findViewById(R.id.publishStoryTextView);

        //Make story text scrollable
        storyTextView.setMovementMethod(new ScrollingMovementMethod());
        storyTextView.setTextSize(storyTextSize);

        //Load data from device rotation
        //TODO: This doesn't do anything due to lifecycle method override - turning the display saves as draft instead.
        //We may wish to address this.
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

                handlePublishStory(false);
            }
        });
    }

    //Handle saving data on device rotation
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putString("story",storyText);
        state.putString("prompt",promptText);
        state.putString("title",titleView.getText().toString());
        super.onSaveInstanceState(state);
    }

    //Handle loading data on activity creation, if any is saved
    protected void loadInstanceState(@NonNull Bundle state) {

        if(state == null) {
            return;
        }

        storyText = state.getString("story");
        promptText = state.getString("prompt");
        titleView.setText(state.getString("title"));

    }

    //Handle loading intent data
    protected void loadIntentData(Intent intent) {

        //Only load data if extras are present
        if(intent.hasExtra("story")) {
            storyText = intent.getStringExtra("story");
        }

        if(intent.hasExtra("prompt")) {
            promptText = intent.getStringExtra("prompt");
        }
    }

    //Handle saving drafts on stop
    //TODO: may need to override other lifecycle methods in order to save drafts in all cases
    @Override
    protected void onStop() {
        super.onStop();

        if(!published && storyTextView.getText().length() > 0)
        {
            handlePublishStory(true);
            toast.setText(draftSaveNotification);
            toast.show();
        }

        storyTextView.setText("");
        titleView.setText("");
        published = false;
    }

    @Override
    protected void onResume() {
       super.onResume();

       //Redirect to feed on resume if there's nothing to publish here
        //TODO: this makes the back button work weirdly. We probably don't want to do this.
       if(storyTextView.getText().length() <= 0)
       {
           goToFeed();
       }
    }

    /*
        Validate that story can be published
     */
    private boolean validatePublishStory()
    {
        boolean valid = true;

        String error = "";

        String title = titleView.getText().toString();

        if(title.length() < titleCharacterLength)
        {
            error = "Enter a title that is at least " + titleCharacterLength + " characters (currently using " + title.length() + " character(s))";
        }

        if(error.length() > 0 )
        {
            valid = false;
            toast.setText(error);
            toast.show();
        }

        return valid;
    }

    /*
        Publish a story to the feed, or save a draft, then redirect to the feed if successful (if not drafting)
     */
    private void handlePublishStory(boolean asDraft)
    {

        if(!AuthUtils.userIsLoggedIn(getApplicationContext()))
        {
            return;
        }

        String title = titleView.getText().toString();
        String storyText = storyTextView.getText().toString();
        String userId = AuthUtils.getLoggedInUserID(getApplicationContext());
        ArrayList<String> lovers = new ArrayList<String>();

        String storyId = userId + new Date().toString().replace(" ",""); //TODO: make this ID nicer

        //Ensure that title is always entered, even if it's a draft
        if(title.length() <= 0) {
            title = storyId; //TODO: make this nicer?
        }

        //TODO: do this all in a background thread
        //TODO: loading wheel

        Story story = new Story(storyId,userId,asDraft,title,promptText,storyText,lovers);

        Task<Void> storyPublishTask = ref.child(story.getId()).setValue(story);

        storyPublishTask.addOnCompleteListener(task -> {

            published = true;

            //Only increment story count if this isn't a draft
            if(asDraft) {
                goToFeed();
               return;
            }

            FBUtils.getUser(getApplicationContext(), AuthUtils.getLoggedInUserID(getApplicationContext()), new Consumer<User>() {
                        @Override
                        public void accept(User user) {
                            if(user != null) {
                               user.incrementStoryCount();
                               FBUtils.updateUser(getApplicationContext(), user, new Consumer<Boolean>() {
                                   @Override
                                   public void accept(Boolean result) {
                                       if(!result) {
                                           toast.setText(genericErrorNotification);
                                           toast.show();
                                       }

                                       goToFeed();
                                   }
                               });
                            }
                        }
                    });
        });

        storyPublishTask.addOnFailureListener(task -> {
            toast.setText(genericErrorNotification);
            toast.show();
            published = false;
        });
    }


    /*
        Go back to the feed after having published a story
     */
    private void goToFeed()
    {
        Intent intent = new Intent(this,StoryFeedActivity.class);
        startActivity(intent);
    }
}