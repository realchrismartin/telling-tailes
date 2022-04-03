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

import com.telling.tailes.R;
import com.telling.tailes.util.GPTUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateStoryActivity extends AppCompatActivity {

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private SeekBar lengthSeekBar;
    private TextView promptView;
    private TextView storyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);

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

        findViewById(R.id.createStoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCreateStory();
            }
        });

        lengthSeekBar = findViewById(R.id.lengthSlider);
        storyView = findViewById(R.id.storyView); //TODO: remove and replace with intent passing
        promptView = findViewById(R.id.promptView);

        //Set maximum etc
        lengthSeekBar.setMin(5);
        lengthSeekBar.setMax(2048);
    }

    /*
        Button onClick handler for creating a story
     */
    private void handleCreateStory()
    {

        String prompt = promptView.getText().toString().trim();
        int length = lengthSeekBar.getProgress();

        if(prompt.length() <= 0 || length <= 0 || length > 2048)
        {
            Log.e("CreateStoryActivity","Prompt or length were not correct"); //TODO
           return;
        }

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                //Do the API call
                String story = GPTUtils.getStory(getApplicationContext(), prompt, length);
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