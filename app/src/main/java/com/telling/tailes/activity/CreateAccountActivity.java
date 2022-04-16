package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.DrawableUtils;

import java.util.function.Consumer;

public class CreateAccountActivity extends AppCompatActivity {

    private TextView usernameEntryView;
    private TextView passwordEntryView;
    private TextView passwordConfirmationEntryView;
    private int selectedProfileIcon;

    private Toast createToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //TODO
        selectedProfileIcon = 0;

        Button p = findViewById(R.id.profileIcon1);

        p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               selectedProfileIcon += 1;
               p.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplicationContext(), DrawableUtils.getProfileIconResourceId(selectedProfileIcon)),null,null,null);

               if(selectedProfileIcon > DrawableUtils.profileIconCount()) {
                   selectedProfileIcon = 0;
               }
            }
        });

        usernameEntryView = findViewById(R.id.createUsernameView);
        passwordEntryView = findViewById(R.id.createPasswordView);
        passwordConfirmationEntryView = findViewById(R.id.createPasswordConfirmView);

        createToast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        //Load instance state data if applicable
        loadInstanceState(savedInstanceState);

        findViewById(R.id.createAccountButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               createAccount();
            }
        });
    }

    /*
         Handle saving data on device rotation
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putString("username",usernameEntryView.getText().toString());
        state.putString("password",passwordEntryView.getText().toString());
        state.putString("passwordConfirmation",passwordConfirmationEntryView.getText().toString());
        super.onSaveInstanceState(state);
    }

    /*
        Handle loading data on activity creation, if any is saved
     */
    protected void loadInstanceState(@NonNull Bundle state) {
        if(state == null) {
            return;
        }

        usernameEntryView.setText(state.getString("username"));
        passwordEntryView.setText(state.getString("password"));
        passwordConfirmationEntryView.setText(state.getString("passwordConfirmation"));
    }

    /*
        Attempts to create the specified account
        If successful, creates the account, logs in, and redirects to the Feed
        If unsuccessful, shows an error message
     */
    private void createAccount() {

        AuthUtils.createUser(getApplicationContext(), usernameEntryView.getText().toString(), passwordEntryView.getText().toString(), passwordConfirmationEntryView.getText().toString(), selectedProfileIcon, new Consumer<String>() {
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