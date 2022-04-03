package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.telling.tailes.R;
import com.telling.tailes.util.GPTUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateStoryActivity extends AppCompatActivity {

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

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
                //TODO: Add handling for web request completed tasks
                Log.e("CreateStoryActivity","We got a message " + msg.toString());

                String story = msg.getData().getString("Story");
                Log.e("SS",story);
            }
        };

        findViewById(R.id.createStoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCreateStory();
            }
        });
    }

    /*
        Button onClick handler for creating a story
     */
    private void handleCreateStory()
    {

        String prompt = "This is a test that";
        int length = 5;

        if(prompt.length() <= 0 || length <= 0 || length > 2048)
        {
            Log.e("CreateStoryActivity","Prompt or length were not correct"); //TODO
           return;
        }

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                int resultCode = 0;

                //Do the API call
                String story = GPTUtils.getStory(getApplicationContext(),prompt,length);

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