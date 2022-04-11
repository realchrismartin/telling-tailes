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
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.EndlessScrollListener;

public class StoryFeedActivity extends AppCompatActivity {

    private static final String storyDBKey = "stories"; //TODO move to app-wide variable?

    private DatabaseReference storyRef;

    private EndlessScrollListener scrollListener;
    private Query initialQuery;
    private int queryIndex;

    private ArrayList<StoryRviewCard> storyCardList = new ArrayList<>();
    private SwipeRefreshLayout feedSwipeRefresh;
    private RecyclerView storyRview;
    private StoryRviewAdapter storyRviewAdapter;
//    private RecyclerView.LayoutManager storyRviewLayoutManager;
    private LinearLayoutManager storyRviewLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        doLoginCheck();

        createStorySwipeToRefresh();
        createStoryRecyclerView();

        storyRef = FirebaseDatabase.getInstance().getReference(storyDBKey);

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

    @Override
    protected void onResume() {
        super.onResume();
        doLoginCheck();
    }

    //Kick the user out of the Feed if they aren't logged in for some reason
    private void doLoginCheck()
    {
        if(!AuthUtils.userIsLoggedIn(getApplicationContext()))
        {
            goToLogin();
        }
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
        storyRviewAdapter = new StoryRviewAdapter(storyCardList,getApplicationContext());

        StoryRviewCardClickListener storyClickListener = new StoryRviewCardClickListener() {
            @Override
            public void onStoryClick(int position) {
                goToReadStory(storyCardList.get(position).getStory());
            }
        };

        storyRviewAdapter.setOnStoryClickListener(storyClickListener);

        storyRview.setAdapter(storyRviewAdapter);
        storyRview.setLayoutManager(storyRviewLayoutManager);
    }



    private void loadFirstStories() {
        initialQuery = storyRef.orderByChild("id").limitToFirst(10);
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
                    Story story = snapshot.getValue(Story.class);
                    int pos;

                    boolean replaced = false;

                    //TODO: this could be more efficient the way it was originally
                    for(pos=0;pos<storyCardList.size();pos++) {
                        if(storyCardList.get(pos).getID().equals(story.getID())) {
                            storyCardList.set(pos, new StoryRviewCard(story));
                            storyRviewAdapter.notifyItemChanged(pos);
                            replaced = true;
                        }
                    }

                    if(replaced) {
                        continue;
                    }

                    storyCardList.add(pos, new StoryRviewCard(story));
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

    private void goToLogin() {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    private void goToReadStory(Story story) {
        Intent intent = new Intent(this,ReadStoryActivity.class);
        intent.putExtra("story",story);
        startActivity(intent);
    }
}