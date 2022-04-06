package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.telling.tailes.R;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.feedTempButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFeed();
            }
        });

        findViewById(R.id.createTempButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCreate();
            }
        });

        findViewById(R.id.testMenuButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {goToHTest();}
        });

    }


    private void goToHTest() {
        Intent intent = new Intent(this, HamburgerMenuFragment.class);
        startActivity(intent);
    }

//    private void goToH2Test() {
//        Intent intent = new Intent(this, HTwoTestActivity.class);
//        startActivity(intent);
//    }

    public void goToFeed()
    {
        Intent intent = new Intent(this,StoryFeedActivity.class);

        startActivity(intent);
    }

    public void goToCreate()
    {
        Intent intent = new Intent(this,CreateStoryActivity.class);

        startActivity(intent);
    }
}