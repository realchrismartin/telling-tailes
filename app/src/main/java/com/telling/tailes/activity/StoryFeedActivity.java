package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
    public boolean includes(Story story)
    {
        switch(this) {
            case MY : {
                return story.getAuthorID().equals(AuthUtils.getLoggedInUserID());
            }
            case DRAFTS: {
                return story.getAuthorID().equals(AuthUtils.getLoggedInUserID()) && story.getIsDraft(); //TODO: story is bugged loading data from DB, isDraft is not set right
            }
            case BOOKMARKS: {
                return story.getAuthorID().equals(AuthUtils.getLoggedInUserID()); //TODO: This does nothing currently
            }
            default: {
                return true;
            }
        }
    }
}

public class StoryFeedActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String storyDBKey = "stories"; //TODO move to app-wide variable?

    private DatabaseReference ref;

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

        createStorySwipeToRefresh();
        createStoryRecyclerView();

        ref = FirebaseDatabase.getInstance().getReference(storyDBKey);

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

    private void createFilterSpinner() {
        filterSpinner = findViewById(R.id.filterSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.filter_spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setOnItemSelectedListener(this);
        currentFilter = FilterType.NONE;
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
                    int pos = storyCardList.size();
                    Story story = snapshot.getValue(Story.class);

                    if(story == null) {
                        continue;
                    }

                    //Track the last seen id, even if excluded by filter
                    lastLoadedStoryId = story.getId();

                    if(!currentFilter.includes(story)) {
                        continue;
                    }

                    storyCardList.add(pos, new StoryRviewCard(
                            story.getId(),
                            story.getAuthorID(),
                            story.getTitle(),
                            story.getLovers()
                    ));
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
        initialQuery = currentFilter.getQuery(ref);
    }
}