package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.telling.tailes.model.Story;
import com.telling.tailes.util.EndlessScrollListener;

public class StoryFeedActivity extends AppCompatActivity {

    private static final String storyDBKey = "stories"; //TODO move to app-wide variable?

    private DatabaseReference testRef;

    private EndlessScrollListener scrollListener;
    private Query initialQuery;
    private int queryIndex;

    private ArrayList<StoryRviewCard> storyCardList = new ArrayList<>();
    private SwipeRefreshLayout feedSwipeRefresh;
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

        createStorySwipeToRefresh();
        createStoryRecyclerView();

        testRef = FirebaseDatabase.getInstance().getReference(storyDBKey);

        testText = findViewById(R.id.testTextView);

        Button testButton = findViewById(R.id.testButton);

        loadFirstStories();

        scrollListener = new EndlessScrollListener(storyRviewLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextStories();
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

    private void createStorySwipeToRefresh() {
        feedSwipeRefresh = findViewById(R.id.feedSwipeRefresh);
        feedSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(StoryFeedActivity.this,
                        "Pulled to refresh!",
                        Toast.LENGTH_SHORT)
                        .show();
                refreshStories();
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

    private void loadFirstStories() {
        initialQuery = testRef.orderByChild("id").limitToFirst(10);
        loadStoryData(initialQuery);
    }

    private void loadNextStories() {
        Toast.makeText(StoryFeedActivity.this,
                "Loading more stories",
                Toast.LENGTH_SHORT)
                .show();
        String id = storyCardList.get(storyCardList.size()-1).getID();
        Query newQuery = initialQuery.startAfter(id);
        loadStoryData(newQuery);
    }

    private void loadStoryData(Query query) {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    int pos = storyCardList.size();
                    Story story = snapshot.getValue(Story.class);
                    storyCardList.add(pos, new StoryRviewCard(
                            story.getID(),
                            story.getAuthorID(),
                            story.getTitle(),
                            story.getLovers()
                    ));
                    storyRviewAdapter.notifyItemInserted(pos);

                }
                feedSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d("loadTest:onCancelled", "AUGH");
                // ...
            }
        });
    }

    private void refreshStories() {
        storyCardList.clear();
        storyRviewAdapter.notifyDataSetChanged();
        scrollListener.resetState();
        loadFirstStories();
    }

    private void goToCreateStory() {
        Intent intent = new Intent(this,CreateStoryActivity.class);

        startActivity(intent);
    }
}