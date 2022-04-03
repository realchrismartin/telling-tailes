package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.telling.tailes.R;
import com.telling.tailes.adapter.StoryRviewAdapter;
import com.telling.tailes.card.StoryRviewCard;

import java.util.ArrayList;

import com.telling.tailes.card.StoryRviewCardClickListener;
import com.telling.tailes.model.FeedStory;

public class StoryFeedActivity extends AppCompatActivity {

    private DatabaseReference testRef;

    private ArrayList<StoryRviewCard> storyCardList = new ArrayList<>();
    private RecyclerView storyRview;
    private StoryRviewAdapter storyRviewAdapter;
    private RecyclerView.LayoutManager storyRviewLayoutManager;

    private TextView testText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        createStoryRecyclerView();

        testRef = FirebaseDatabase.getInstance().getReference().child("will_test");

        testText = findViewById(R.id.testTextView);

        Button testButton = findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadStoryData();
            }
        });
    }

    private void createStoryRecyclerView() {
        storyRviewLayoutManager = new LinearLayoutManager(this);
        storyRview = findViewById(R.id.story_recycler_view);
        storyRview.setHasFixedSize(true);
        storyRviewAdapter = new StoryRviewAdapter(storyCardList);

        StoryRviewCardClickListener storyClickListener = new StoryRviewCardClickListener() {
            @Override
            public void onStoryClick(int position) {
                Toast.makeText(StoryFeedActivity.this,
                        "Story clicked!",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        };
        storyRviewAdapter.setOnStoryClickListener(storyClickListener);


        storyRview.setAdapter(storyRviewAdapter);
        storyRview.setLayoutManager(storyRviewLayoutManager);
    }

    private void loadStoryData() {
        Task<DataSnapshot> storyDataGetTask = testRef.get();

        storyDataGetTask.addOnCompleteListener(task -> {
            testRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    //testText.setText(snapshot.getValue().toString());
                    FeedStory story = snapshot.getValue(FeedStory.class);
                    storyCardList.add(0, new StoryRviewCard(
                            story.getVal()
                    ));
                    storyRviewAdapter.notifyItemChanged(0);

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
}