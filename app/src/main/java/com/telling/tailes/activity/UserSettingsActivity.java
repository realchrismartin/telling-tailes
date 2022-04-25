package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.fragment.UserSettingsDialogFragment;
import com.telling.tailes.util.AuthUtils;

import java.util.function.Consumer;

public class UserSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        getSupportFragmentManager().beginTransaction().replace(R.id.setting_view, new UserSettingsDialogFragment()).commit();
    }
}