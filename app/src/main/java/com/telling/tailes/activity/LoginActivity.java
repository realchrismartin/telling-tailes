package com.telling.tailes.activity;

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