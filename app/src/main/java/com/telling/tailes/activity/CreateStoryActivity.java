package com.telling.tailes.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.telling.tailes.R;
import com.telling.tailes.util.GPTUtils;
import com.telling.tailes.util.StringUtils;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateStoryActivity extends AppCompatActivity {

    //Length requirements for validation
    private static final int promptMinWords = 30;
    private static final int lengthMin = 40;
    private static final int lengthMax = 2048;

    //Request IDs
    private static final int REQUEST_AUDIO = 0;

    //Notification string resources
    private String genericErrorNotification;
    private String lengthTooLongNotification;
    private String lengthTooShortNotification;
    private String createInProgressNotification;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;
    private ActivityResultLauncher<Intent> launcher;

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
        toast = Toast.makeText(getApplicationContext(),getString(R.string.empty_string),Toast.LENGTH_SHORT);

        //Set up views
        lengthSeekBar = findViewById(R.id.lengthSlider);

        promptView = findViewById(R.id.promptView);
        loadingWheel = findViewById(R.id.storyCreateLoadingWheel);

        //If intent includes a prompt, prepopulate the prompt
        //Note that this will be overridden by saved intent data, if present in the bundle
        if(getIntent().hasExtra(getString(R.string.intent_extra_prompt))) {
            promptView.setText(getIntent().getStringExtra(getString(R.string.intent_extra_prompt)));
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
                if(validateCreatedStory(msg.getData().getInt(StringUtils.backgroundResultPropertyResult)))
                {
                    goToPublish(promptView.getText().toString(),msg.getData().getString(StringUtils.backgroundResultPropertyStory));
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

        //Set up launcher for activity result handling (mic recordings)
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if(result.getResultCode() != Activity.RESULT_OK){
                            toast.setText(R.string.no_microphone_error_notification);
                            toast.show();
                            return;
                        }

                        if (result.getData() != null && result.getData().getExtras() != null) {
                            promptView.setText(String.join("," , result.getData().getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS)));
                            return;
                        }

                        Log.e(getString(R.string.log_error_tag_voice_input), getString(R.string.log_error_msg_voice_input));
                    }});

        //Define click handler for recording prompt from voice
        findViewById(R.id.recordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClickRecordPrompt();
            }
        });

        //Finish loading
        hideLoadingWheel();
    }

    //Handle user clicking record prompt
    private void handleClickRecordPrompt() {

        if(!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            toast.setText(R.string.no_microphone_error_notification);
            toast.show();
            return;
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            try {
                launcher.launch(i);
            } catch(ActivityNotFoundException ex) {
                toast.setText(R.string.no_microphone_error_notification);
                toast.show();
            }
        } else {
            ActivityCompat.requestPermissions(CreateStoryActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
        }
    }
    /*
         Handle saving data on device rotation
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putString(getString(R.string.saved_instance_prompt),promptView.getText().toString());
        state.putInt(getString(R.string.saved_instance_progress),lengthSeekBar.getProgress());
        super.onSaveInstanceState(state);
    }

    /*
         Handle loading data on activity creation, if any is saved
     */
    protected void loadInstanceState(@NonNull Bundle state) {
        if(state == null) {
            return;
        }

        lengthSeekBar.setProgress(state.getInt(getString(R.string.saved_instance_progress)));
        promptView.setText(state.getString(getString(R.string.saved_instance_prompt)));
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
        intent.putExtra(getString(R.string.intent_extra_prompt),prompt);
        intent.putExtra(getString(R.string.intent_extra_story),story);
        startActivity(intent);
    }

    /*
       Return true if valid, false otherwise.
       Display any issues with input as a Toast
     */
    private boolean validateCreateStory() {
        boolean valid = true;
        String error = getString(R.string.empty_string);

        String prompt = promptView.getText().toString().trim();
        int length = lengthSeekBar.getProgress();
        int wordCount = StringUtils.getWordCount(prompt);

        if(wordCount < promptMinWords)
        {
            error = getString(R.string.prompt_too_short_1) + getString(R.string.space) + promptMinWords + getString(R.string.space) + getString(R.string.prompt_too_short_2) + getString(R.string.space) + wordCount + getString(R.string.space) + getString(R.string.prompt_too_short_3);
        }

        if(length < lengthMin)
        {
            if(error.length() > 0) { error += getString(R.string.newline); }
            error += lengthTooShortNotification;
        }

        if(length > lengthMax)
        {
            if(error.length() > 0) { error += getString(R.string.newline); }
            error += lengthTooLongNotification;
        }

        if(loading)
        {
            if(error.length() > 0) { error += getString(R.string.newline); }
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

       String error = getString(R.string.empty_string);

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
                resultData.putInt(StringUtils.backgroundResultPropertyResult, resultCode);
                resultData.putString(StringUtils.backgroundResultPropertyStory, story);

                Message resultMessage = new Message();
                resultMessage.setData(resultData);

                //Notify the activity that the API call is done
                backgroundTaskResultHandler.sendMessage(resultMessage);
            }
        });
    }
}