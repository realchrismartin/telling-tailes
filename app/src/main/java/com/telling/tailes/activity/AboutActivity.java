package com.telling.tailes.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.telling.tailes.R;


public class AboutActivity extends AppCompatActivity {

    private static final int storyTextDefaultSize = 24; //Default text size if not overridden by prefs

    private TextView aboutTextPrompt;
    private TextView aboutTextBody;
    private Button createButton;
    private Button moreInformationButton;


    protected void onCreate (Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_about);

        aboutTextPrompt = findViewById(R.id.aboutTextPrompt);
        aboutTextBody = findViewById(R.id.aboutTextBody);
        createButton = findViewById(R.id.createButton);
        moreInformationButton = findViewById(R.id.moreInformationButton);

        //Set font size to preference setting
        try {
            aboutTextPrompt.setTextSize(PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.setting_text_size_title),storyTextDefaultSize));
            aboutTextBody.setTextSize(PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.setting_text_size_title),storyTextDefaultSize));
        } catch(Exception ex) {
            ex.printStackTrace();
            aboutTextPrompt.setTextSize(PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.setting_text_size_title),storyTextDefaultSize));
            aboutTextBody.setTextSize(storyTextDefaultSize);
        }

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateStoryActivity.class);
                startActivity(intent);
            }
        });

        moreInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.open_ai_url)));
                startActivity(intent);
            }
        });
    }


}
