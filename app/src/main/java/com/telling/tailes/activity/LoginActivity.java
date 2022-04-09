package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.util.AuthUtils;

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
                goToCreate();
            }
        });

    }

    /*
        Attempt a login to the app
        If the login is successful, redirect to the feed
     */
    public void login()
    {

        String errorResult = AuthUtils.logInUser(usernameEntryView.getText().toString(),passwordEntryView.getText().toString());

        if(errorResult.length() <= 0)
        {
            Intent intent = new Intent(this,StoryFeedActivity.class);
            startActivity(intent);
            return;
        }

        loginToast.setText(errorResult);
        loginToast.show();
    }

    /*
        Navigate to the Create Account activity
     */
    public void goToCreate()
    {
        Intent intent = new Intent(this,CreateAccountActivity.class);

        startActivity(intent);
    }
}