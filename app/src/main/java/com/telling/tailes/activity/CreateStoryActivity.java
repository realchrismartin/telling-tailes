package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.util.GPTUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateStoryActivity extends AppCompatActivity {

    private static final int promptMinCharacters = 15;
    private static final int lengthMin = 5;
    private static final int lengthMax = 2048;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private SeekBar lengthSeekBar;
    private TextView promptView;
    private TextView storyView;

    private Toast toast;

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
                //TODO: Change to nav to preview and send intent
                String display = promptView.getText().toString() + " " + msg.getData().getString("Story");
                storyView.setText(display);
            }
        };

        //Define click handlers for creating story
        findViewById(R.id.createStoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateCreateStory())
                {
                    handleCreateStory();
                }
            }
        });

        lengthSeekBar = findViewById(R.id.lengthSlider);
        storyView = findViewById(R.id.storyView); //TODO: remove and replace with intent passing
        promptView = findViewById(R.id.promptView);

        //Set maximum etc
        lengthSeekBar.setMin(lengthMin);
        lengthSeekBar.setMax(lengthMax);
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
           error = "Please enter a prompt that is at least " + promptMinCharacters + " character(s) long";
        }

        if(length <= 0 || length > 2048)
        {
           error += "\nThe length selected is too short. Try changing the slider.";
        }

        if( length > 2048)
        {
            error += "\nThe length selected is too long. Try changing the slider.";
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

                //Do the API call
                String story = GPTUtils.getStory(getApplicationContext(), promptView.getText().toString().trim(), lengthSeekBar.getProgress());
                int resultCode = story.length() <= 0 ? 1 : 0;

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