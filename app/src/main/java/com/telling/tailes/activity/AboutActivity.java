package com.telling.tailes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.telling.tailes.R;


public class AboutActivity extends AppCompatActivity {

    private TextView aboutTextTitle;
    private TextView aboutTextBody;
    private Button createButton;


    protected void onCreate (Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_about);
        createButton = findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateStoryActivity.class);
                startActivity(intent);
            }
        });
    }

}
