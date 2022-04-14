package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.util.GPTUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateStoryActivity extends AppCompatActivity {

    //Length requirements for validation
    private static final int promptMinCharacters = 30;
    private static final int lengthMin = 40;
    private static final int lengthMax = 2048;

    //Bundle data keys
    private static final String resultKey = "Result";
    private static final String storyKey = "Story";

    //Notification string resources
    private String genericErrorNotification;
    private String lengthTooLongNotification;
    private String lengthTooShortNotification;
    private String createInProgressNotification;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private SeekBar lengthSeekBar;
    private TextView promptView;
    private ProgressBar loadingWheel;
    private Toast toast;

    //Whether background data is loading or not currently
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);

        //Set up string resources
        genericErrorNotification = getString(R.string.generic_error_notification);
        lengthTooLongNotification = getString(R.string.length_long_error_notification);
        lengthTooShortNotification = getString(R.string.length_short_error_notification);
        createInProgressNotification = getString(R.string.create_in_progress_notification);

        //Set up toast
        toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        //Set up views
        lengthSeekBar = findViewById(R.id.lengthSlider);
        promptView = findViewById(R.id.promptView);
        loadingWheel = findViewById(R.id.storyCreateLoadingWheel);

        //If intent includes a prompt, prepopulate the prompt
        //Note that this will be overridden by saved intent data, if present in the bundle
        if(getIntent().hasExtra("prompt")) {
            promptView.setText(getIntent().getStringExtra("prompt"));
        }

        //Load saved bundle data if applicable
        loadInstanceState(savedInstanceState);

        //Set seekbar min and max
        lengthSeekBar.setMin(lengthMin);
        lengthSeekBar.setMax(lengthMax);

        //Set up background executor for handling web request threads
        backgroundTaskExecutor = Executors.newFixedThreadPool(2);

        //Define handling for results from the background thread
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                hideLoadingWheel();
                if(validateCreatedStory(msg.getData().getInt(resultKey)))
                {
                    goToPublish(promptView.getText().toString(),msg.getData().getString(storyKey));
                }
            }
        };

        //Define click handlers for creating story
        findViewById(R.id.createStoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!validateCreateStory()) {
                    return;
                }

                showLoadingWheel();
                handleCreateStory();
            }
        });

        //Finish loading
        hideLoadingWheel();
    }

    /*
         Handle saving data on device rotation
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putString("prompt",promptView.getText().toString());
        state.putInt("length",lengthSeekBar.getProgress());
        super.onSaveInstanceState(state);
    }

    /*
         Handle loading data on activity creation, if any is saved
     */
    protected void loadInstanceState(@NonNull Bundle state) {
        if(state == null) {
            return;
        }

        lengthSeekBar.setProgress(state.getInt("progress"));
        promptView.setText(state.getString("prompt"));
    }

    private void hideLoadingWheel() {
        loadingWheel.setVisibility(View.INVISIBLE);
        loading = false;
    }

    private void showLoadingWheel() {
        loadingWheel.setVisibility(View.VISIBLE);
        loading = true;
    }

    private void goToPublish(String prompt, String story) {
        Intent intent = new Intent(this,PublishStoryActivity.class);
        intent.putExtra("prompt",prompt);
        intent.putExtra("story",story);
        startActivity(intent);
    }

    /*
       Return true if valid, false otherwise.
       Display any issues with input as a Toast
     */
    private boolean validateCreateStory() {
        boolean valid = true;
        String error = "";

        String prompt = promptView.getText().toString().trim();
        int length = lengthSeekBar.getProgress();

        if(prompt.length() <= promptMinCharacters)
        {
            //TODO: extract string resources
           error = "Please enter a prompt that is at least " + promptMinCharacters + " character(s) long (currently using " + prompt.length() + " character(s))";
        }

        if(length < lengthMin)
        {
            if(error.length() > 0) { error += "\n"; }
            error += lengthTooShortNotification;
        }

        if(length > lengthMax)
        {
            if(error.length() > 0) { error += "\n"; }
            error += lengthTooLongNotification;
        }

        if(loading)
        {
            if(error.length() > 0) { error += "\n"; }
            error += createInProgressNotification;
        }

        if(error.length() > 0)
        {
            toast.setText(error);
            toast.show();
            valid = false;
        }

        return valid;
    }

    /*
        If result code from GPT query is nonzero, show an error
     */
    private boolean validateCreatedStory(int resultCode)
    {
       boolean valid = true;

       String error = "";

       if(resultCode != 0)
       {
          error = genericErrorNotification;
       }

       if(error.length() > 0)
       {
          toast.setText(error);
          toast.show();
          valid = false;
       }

       return valid;
    }

    /*
        Button onClick handler for creating a story
     */
    private void handleCreateStory()
    {
        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                //Ask GPT to complete the prompt
                String story = GPTUtils.getStory(getApplicationContext(), promptView.getText().toString().trim(), lengthSeekBar.getProgress());
                int resultCode = story.length() <= 0 ? 1 : 0;

                //Set up a bundle
                //Result code != 0 means something in GPT failed
                Bundle resultData = new Bundle();
                resultData.putInt(resultKey, resultCode);
                resultData.putString(storyKey, story);

                Message resultMessage = new Message();
                resultMessage.setData(resultData);

                //Notify the activity that the API call is done
                backgroundTaskResultHandler.sendMessage(resultMessage);
            }
        });
    }
}