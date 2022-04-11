package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

enum FilterType {

    MY,
    BOOKMARKS,
    DRAFTS,
    NONE;

    //Get a FilterType given a string
    public static FilterType get(String str) {
        switch (str) {
            case ("My Tailes"): {
                return MY;
            }
            case ("Bookmarks"): {
                return BOOKMARKS;
            }
            case ("Drafts"): {
                return DRAFTS;
            }
            default:
                return NONE;
        }
    }

    //Get a Query for this FilterType from the provided ref that is appropriate for this filter
    //TODO: This is unused right now, but will be helpful to order by love count etc when implemented
    public Query getQuery(DatabaseReference ref) {
        switch(this) {
            case MY: {
                return ref.orderByChild("id").limitToFirst(10); //TODO: duplicates: make these appropriate for each filter
            }
            case DRAFTS: {
                return ref.orderByChild("id").limitToFirst(10);
            }
            case BOOKMARKS: {
                return ref.orderByChild("id").limitToFirst(10);
            }
            default: {
                return ref.orderByChild("id").limitToFirst(10);
            }
        }
    }

    //Return true if this filter includes the provided story, false otherwise
    //This is not efficient, sorry.
    //TODO: make more efficient
    public boolean includes(Context context, Story story)
    {
        switch(this) {
            case MY : {
                return story.getAuthorID().equals(AuthUtils.getLoggedInUserID(context));
            }
            case DRAFTS: {
                return story.getAuthorID().equals(AuthUtils.getLoggedInUserID(context)) && story.getIsDraft(); //TODO: story is bugged loading data from DB, isDraft is not set right
            }
            case BOOKMARKS: {
                return story.getAuthorID().equals(AuthUtils.getLoggedInUserID(context)); //TODO: This does nothing currently
            }
            default: {
                return true;
            }
        }
    }
}

public class StoryFeedActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String storyDBKey = "stories"; //TODO move to app-wide variable?

    private DatabaseReference storyRef;

    private EndlessScrollListener scrollListener;
    private Query initialQuery;
    private int queryIndex;

    private ArrayList<StoryRviewCard> storyCardList = new ArrayList<>();
    private SwipeRefreshLayout feedSwipeRefresh;
    private RecyclerView storyRview;
    private StoryRviewAdapter storyRviewAdapter;
    private Spinner filterSpinner;
//    private RecyclerView.LayoutManager storyRviewLayoutManager;
    private LinearLayoutManager storyRviewLayoutManager;

    private String lastLoadedStoryId;

    private FilterType currentFilter;

    private int refreshIterations;
    private int maxRefreshIterations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);
        lastLoadedStoryId = "";
        maxRefreshIterations = 5; //TODO: adjust this

        doLoginCheck();

        createStorySwipeToRefresh();
        createStoryRecyclerView();

        storyRef = FirebaseDatabase.getInstance().getReference(storyDBKey);

        createFilterSpinner();

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

    private void createFilterSpinner() {
        filterSpinner = findViewById(R.id.filterSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.filter_spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setOnItemSelectedListener(this);
        currentFilter = FilterType.NONE;
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

        if(!lastLoadedStoryId.equals(""))
        {
            Query newQuery = initialQuery.startAfter(lastLoadedStoryId);
            loadStoryData(newQuery);
            return;
        }

        loadStoryData(initialQuery);
    }

    private void loadStoryData(Query query) {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);

                    if(story == null) {
                        continue;
                    }

                    //Track the last seen id, even if excluded by filter
                    lastLoadedStoryId = story.getId();

                    if(!currentFilter.includes(getApplicationContext(),story)) {
                        continue;
                    }

                    //TODO: this could be more efficient the way it was originally
                    int pos;
                    boolean replaced = false;
                    for(pos=0;pos<storyCardList.size();pos++) {
                        if(storyCardList.get(pos).getID().equals(story.getId())) {
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

                if(storyCardList.size() <= 0 && refreshIterations < maxRefreshIterations) {
                    refreshIterations++;
                    loadNextStories();
                }
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

    //Listener method for filter spinner item selection
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        applyFilter(FilterType.get(adapterView.getItemAtPosition(i).toString()));
        refreshStories();
    }

    //Listener method for filter spinner item deselection
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    //Apply the requested filter to the initialQuery
    private void applyFilter(FilterType filter)
    {
        currentFilter = filter;
        refreshIterations = 0;
        initialQuery = currentFilter.getQuery(storyRef);
    }
}