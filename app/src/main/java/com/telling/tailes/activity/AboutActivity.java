package com.telling.tailes.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.telling.tailes.R;


public class AboutActivity extends AppCompatActivity {

    private TextView aboutText;
    protected void onCreate (Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_about);
    }

}
