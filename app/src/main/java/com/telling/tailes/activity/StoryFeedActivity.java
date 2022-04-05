package com.telling.tailes.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.telling.tailes.R;
import com.telling.tailes.adapter.StoryRviewAdapter;
import com.telling.tailes.card.StoryRviewCard;

import java.util.ArrayList;

import com.telling.tailes.card.StoryRviewCardClickListener;
import com.telling.tailes.model.FeedStory;
import com.telling.tailes.util.EndlessScrollListener;

public class StoryFeedActivity extends AppCompatActivity {

    private static final String storyDBKey = "will_test"; //TODO

    private DatabaseReference testRef;

    private EndlessScrollListener scrollListener;
    private Query initialQuery;
    private int queryIndex;

    private ArrayList<StoryRviewCard> storyCardList = new ArrayList<>();
    private RecyclerView storyRview;
    private StoryRviewAdapter storyRviewAdapter;
//    private RecyclerView.LayoutManager storyRviewLayoutManager;
    private LinearLayoutManager storyRviewLayoutManager;

    private TextView testText;

    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);


        createStoryRecyclerView();

        testRef = FirebaseDatabase.getInstance().getReference(storyDBKey);

        testText = findViewById(R.id.testTextView);

        Button testButton = findViewById(R.id.testButton);
//        testButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                loadStoryData();
//            }
//        });

        counter = 0;

//        for (int i = 0; i < 10; i ++) {
//            int pos = storyCardList.size();
//            storyCardList.add(pos, new StoryRviewCard(counter++));
//            storyRviewAdapter.notifyItemInserted(pos);
//        }

        initialQuery = testRef.orderByChild("Val").limitToFirst(10);
        queryIndex = 10;
        initialQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    int pos = storyCardList.size();
                    FeedStory story = snapshot.getValue(FeedStory.class);
                    storyCardList.add(pos, new StoryRviewCard(
                            story.getVal()
                    ));
                    storyRviewAdapter.notifyItemInserted(pos);
                    int i = 4;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d("loadTest:onCancelled", "AUGH");
                // ...
            }
        });

        scrollListener = new EndlessScrollListener(storyRviewLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadStoryData();
            }
        };
        storyRview.addOnScrollListener(scrollListener);

        findViewById(R.id.goToCreateStoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCreateStory();
            }
        });
    }

    private void createStoryRecyclerView() {
        storyRviewLayoutManager = new LinearLayoutManager(this);
        storyRview = findViewById((R.id.story_recycler_view));
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
//        int pos = storyCardList.size();
//        for (int i = 0; i < 10; i ++) {
//            storyCardList.add(pos, new StoryRviewCard(counter++));
//            storyRviewAdapter.notifyItemInserted(pos);
//        }


        Query newQuery = initialQuery.startAfter(queryIndex);
        queryIndex += 10;
        newQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    int pos = storyCardList.size();
                    FeedStory story = snapshot.getValue(FeedStory.class);
                    storyCardList.add(pos, new StoryRviewCard(
                            story.getVal()
                    ));
                    storyRviewAdapter.notifyItemInserted(pos);
                    int i = 4;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d("loadTest:onCancelled", "AUGH");
                // ...
            }
        });


        /*Task<DataSnapshot> storyDataGetTask = testRef.get();

        storyCardList.add(0, new StoryRviewCard(1));
        storyRviewAdapter.notifyItemInserted(0);

        storyDataGetTask.addOnCompleteListener(task -> {
            testRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    //testText.setText(snapshot.getValue().toString());
                    FeedStory story = snapshot.getValue(FeedStory.class);
                    storyCardList.add(0, new StoryRviewCard(
                            story.getVal()
                    ));
                    storyRviewAdapter.notifyItemInserted(0);

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
        });*/
    }

    private void goToCreateStory() {
        Intent intent = new Intent(this,CreateStoryActivity.class);

        startActivity(intent);
    }
}