package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.StringUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class LoginActivity extends AppCompatActivity {

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;
    private TextView usernameEntryView;
    private TextView passwordEntryView;
    private Toast loginToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Set up notification channel
        NotificationChannel channel = new NotificationChannel(StringUtils.notificationChannelId, StringUtils.notificationChannelName, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        backgroundTaskExecutor = Executors.newFixedThreadPool(2);

        //Set views
        usernameEntryView = findViewById(R.id.enterUsernameView);
        passwordEntryView = findViewById(R.id.enterPasswordView);

        //Create toast
        loginToast = Toast.makeText(getApplicationContext(), StringUtils.emptyString,Toast.LENGTH_SHORT);

        //Load data from saved state if applicable
        loadInstanceState(savedInstanceState);

        //Set listeners
        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        findViewById(R.id.createAccountNavButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if(msg.getData() == null) {
                    return;
                }

                String errors = msg.getData().getString(StringUtils.backgroundTaskResultError);

                if(errors.length() > 0) {
                    loginToast.setText(errors);
                    loginToast.show();
                    return;
                }

                //If all is well, redirect to the feed once logged in
                Intent intent = new Intent(getApplicationContext(),StoryFeedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        };
    }

    /*
         Handle saving data on device rotation
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        String username = usernameEntryView.getText().toString();
        String password = passwordEntryView.getText().toString();

        if(username.length() > 0) {
            state.putString(StringUtils.savedInstanceUsername, username);
        }

        if(password.length() > 0) {
            state.putString(StringUtils.savedInstancePassword, password);
        }

        super.onSaveInstanceState(state);
    }

    /*
         Handle loading data on activity creation, if any is saved
     */
    protected void loadInstanceState(@NonNull Bundle state) {

        if(state == null) {
            return;
        }

        if(state.containsKey(StringUtils.savedInstanceUsername)) {
            usernameEntryView.setText(state.getString(StringUtils.savedInstanceUsername));
        }

        if(state.containsKey(StringUtils.savedInstancePassword)) {
            passwordEntryView.setText(state.getString(StringUtils.savedInstancePassword));
        }
    }

    /*
        Attempt a login to the app
       If the login is successful, redirect to the feed
        Note: allows you to log in even if you're already logged in (i.e. change accounts)
     */
    public void login() {

        String username = usernameEntryView.getText().toString();
        String password = passwordEntryView.getText().toString();

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                Message resultMessage = new Message();
                Bundle resultData = new Bundle();
                resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundTaskResultError);

                if(username.length() <= 0 || password.length() <= 0) {
                    resultData.putString(StringUtils.backgroundTaskResultError, getString(R.string.login_error_notification));
                    resultMessage.setData(resultData);
                    backgroundTaskResultHandler.sendMessage(resultMessage);
                    return;
                }

                AuthUtils.logInUser(getApplicationContext(),username,password,new Consumer<String>() {
                    @Override
                    public void accept(String errorResult) {
                        resultData.putString(StringUtils.backgroundTaskResultError, errorResult);
                        resultMessage.setData(resultData);
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }
}