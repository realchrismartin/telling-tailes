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
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new UserSettingsDialogFragment()).commit();
    }

    public void userSettingsOnclickHelper(View view) {
        switch (view.getId()) {
            case R.id.userSettingsLogOut:
                logout();
                break;
            default:
                Toast.makeText(getApplicationContext(),
                        "onClickHelperDefault",
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void logout() {
        AuthUtils.logOutUser(getApplicationContext(), new Consumer<String>() {
            @Override
            public void accept(String s) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}