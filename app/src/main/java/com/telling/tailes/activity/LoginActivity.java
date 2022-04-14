package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.util.AuthUtils;

import java.util.function.Consumer;

public class LoginActivity extends AppCompatActivity {

    private TextView usernameEntryView;
    private TextView passwordEntryView;
    private Toast loginToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Set views
        usernameEntryView = findViewById(R.id.enterUsernameView);
        passwordEntryView = findViewById(R.id.enterPasswordView);

        //Create toast
        loginToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

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
    }

    /*
         Handle saving data on device rotation
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        String username = usernameEntryView.getText().toString();
        String password = passwordEntryView.getText().toString();

        if(username.length() > 0) {
            state.putString("username",username);
        }

        if(password.length() > 0) {
            state.putString("password",password);
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

        if(state.containsKey("username")) {
            usernameEntryView.setText(state.getString("username"));
        }

        if(state.containsKey("password")) {
            passwordEntryView.setText(state.getString("password"));
        }
    }
t
    /*
        Attempt a login to the app
        If the login is successful, redirect to the feed
        Note: allows you to log in even if you're already logged in (i.e. change accounts)
     */
    public void login()
    {
        AuthUtils.logInUser(getApplicationContext(), usernameEntryView.getText().toString(), passwordEntryView.getText().toString(), new Consumer<String>() {
            @Override
            public void accept(String errorResult) {

                if(errorResult.length() > 0)
                {
                    loginToast.setText(errorResult);
                    loginToast.show();
                    return;
                }

                //If all is well, redirect to the feed once logged in
                Intent intent = new Intent(getApplicationContext(),StoryFeedActivity.class);
                startActivity(intent);
            }
        });
    }
}