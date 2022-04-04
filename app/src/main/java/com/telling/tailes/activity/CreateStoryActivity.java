package com.telling.tailes.activity;

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

    private static final int promptMinCharacters = 30;
    private static final int lengthMin = 40;
    private static final int lengthMax = 2048;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private SeekBar lengthSeekBar;
    private TextView promptView;
    private ProgressBar loadingWheel;

    private Toast toast;

    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);

        //Set up toast
        toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        //Set up background executor for handling web request threads
        backgroundTaskExecutor = Executors.newFixedThreadPool(2);

        //Define handling for results from the background thread
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                hideLoadingWheel();
                if(validateCreatedStory(msg.getData().getInt("Result")))
                {
                    goToPublish(promptView.getText().toString() + " " + msg.getData().getString("Story"));
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

        //Set up views
        lengthSeekBar = findViewById(R.id.lengthSlider);
        promptView = findViewById(R.id.promptView);
        loadingWheel = findViewById(R.id.storyCreateLoadingWheel);
        hideLoadingWheel();

        //Set seekbar min and max
        lengthSeekBar.setMin(lengthMin);
        lengthSeekBar.setMax(lengthMax);
    }

    private void hideLoadingWheel() {
        loadingWheel.setVisibility(View.INVISIBLE);
        loading = false;
    }

    private void showLoadingWheel() {
        loadingWheel.setVisibility(View.VISIBLE);
        loading = true;
    }

    private void goToPublish(String story) {
        Intent intent = new Intent(this,PublishStoryActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT,story);
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

        //TODO: extract string resources
        if(prompt.length() <= promptMinCharacters)
        {
           error = "Please enter a prompt that is at least " + promptMinCharacters + " character(s) long (currently using " + prompt.length() + " character(s))";
        }

        if(length <= lengthMin)
        {
           error += "\nThe length selected is too short. Try changing the slider.";
        }

        if(length > lengthMax)
        {
            error += "\nThe length selected is too long. Try changing the slider.";
        }

        if(loading)
        {
            error += "\nPlease wait while our tireless robot monkeys draft Shakespeare for you...";
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
          error = "Something went wrong, and it's our fault. Maybe try again later?";
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
                resultData.putInt("Result", resultCode);
                resultData.putString("Story", story);

                Message resultMessage = new Message();
                resultMessage.setData(resultData);

                //Notify the activity that the API call is done
                backgroundTaskResultHandler.sendMessage(resultMessage);
            }
        });
    }
}