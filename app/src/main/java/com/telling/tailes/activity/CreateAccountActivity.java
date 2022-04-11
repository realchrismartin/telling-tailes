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

public class CreateAccountActivity extends AppCompatActivity {

    private TextView usernameEntryView;
    private TextView passwordEntryView;
    private TextView passwordConfirmationEntryView;

    private Toast createToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        usernameEntryView = findViewById(R.id.createUsernameView);
        passwordEntryView = findViewById(R.id.createPasswordView);
        passwordConfirmationEntryView = findViewById(R.id.createPasswordConfirmView);

        createToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        findViewById(R.id.createAccountButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               createAccount();
            }
        });
    }

    /*
        Attempts to create the specified account
        If successful, creates the account, logs in, and redirects to the Feed
        If unsuccessful, shows an error message
     */
    private void createAccount() {

        AuthUtils.createUser(getApplicationContext(), usernameEntryView.getText().toString(), passwordEntryView.getText().toString(), passwordConfirmationEntryView.getText().toString(), new Consumer<String>() {
            @Override
            public void accept(String errorResult) {

                if(errorResult.length() > 0) {
                    createToast.setText(errorResult);
                    createToast.show();
                    return;
                }

                AuthUtils.logInUser(getApplicationContext(),usernameEntryView.getText().toString(),passwordEntryView.getText().toString(), new Consumer<String>() {
                    @Override
                    public void accept(String loginErrorResult) {

                        if(loginErrorResult.length() > 0) {
                            createToast.setText(loginErrorResult);
                            createToast.show();
                            return;
                        }

                        //If all is well, redirect to the feed once logged in
                        Intent intent = new Intent(getApplicationContext(),StoryFeedActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}