package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.R;

public class StoryFeedActivity extends AppCompatActivity {

    private static final String storyDBKey = "will_test"; //TODO

    private DatabaseReference testRef;
    private TextView testText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        testRef = FirebaseDatabase.getInstance().getReference().child(storyDBKey);

        testText = findViewById(R.id.testTextView);

        Button testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadStoryData();
            }
        });

        findViewById(R.id.goToCreateStoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCreateStory();
            }
        });
    }

    private void loadStoryData() {
        Task<DataSnapshot> storyDataGetTask = testRef.get();

        storyDataGetTask.addOnCompleteListener(task -> {
            testRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    testText.setText(snapshot.getValue().toString());
                    int i = 4;
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    //TODO: if story is liked by user
                    //TODO: if story is bookmarked by user
                    //TODO: if story is liked by OTHER user?
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    //TODO: if story is deleted or author is deleted
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }

    private void goToCreateStory() {
        Intent intent = new Intent(this,CreateStoryActivity.class);

        startActivity(intent);
    }
}