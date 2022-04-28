package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

import com.telling.tailes.R;
import com.telling.tailes.fragment.UserSettingsDialogFragment;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.DrawableUtils;
import com.telling.tailes.util.FBUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UserSettingsActivity extends AppCompatActivity {

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        backgroundTaskExecutor = Executors.newFixedThreadPool(2);
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if(msg.getData() == null) {
                    return;
                }
                TextView usernameView = findViewById(R.id.settings_user_name_view);
                usernameView.setText(AuthUtils.getLoggedInUserID(getApplicationContext()));
                int icon = msg.getData().getInt("icon");
                ImageView profileIconView = findViewById(R.id.settings_user_profile_image);
                profileIconView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), DrawableUtils.getProfileIconResourceId(icon)));

                getSupportFragmentManager().beginTransaction().replace(R.id.setting_view, new UserSettingsDialogFragment()).commit();
            }
        };
        getUserIcon();
    }

    private void getUserIcon() {
        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FBUtils.getUser(getApplicationContext(), AuthUtils.getLoggedInUserID(getApplicationContext()), new Consumer<User>() {
                    @Override
                    public void accept(User user) {
                        //Set up a bundle
                        Bundle resultData = new Bundle();
                        resultData.putInt("icon",user.getProfileIcon());

                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);

                        //Notify the activity that follow data has been retrieved
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }
}