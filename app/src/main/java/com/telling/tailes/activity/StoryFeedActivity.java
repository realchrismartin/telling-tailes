package com.telling.tailes.activity;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.telling.tailes.adapter.FilterSpinnerAdapter;
import com.telling.tailes.adapter.StoryRviewAdapter;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.card.StoryRviewCardClickListener;
import com.telling.tailes.fragment.AuthorProfileDialogFragment;
import com.telling.tailes.model.AuthorProfile;
import com.telling.tailes.model.FilterSpinnerItem;
import com.telling.tailes.model.Story;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;
import com.telling.tailes.util.FilterType;
import com.telling.tailes.util.EndlessScrollListener;
import com.telling.tailes.R;

public class StoryFeedActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnAuthorClickCallbackListener {

    private static final String storyDBKey = "stories"; //TODO move to app-wide variable?

    private DatabaseReference storyRef;

    private EndlessScrollListener scrollListener;
    private Query query;

    private ArrayList<StoryRviewCard> storyCardList = new ArrayList<>();
    private SwipeRefreshLayout feedSwipeRefresh;
    private RecyclerView storyRview;
    private StoryRviewAdapter storyRviewAdapter;

    private ArrayAdapter<FilterSpinnerItem> spinnerAdapter;
    private ArrayList<FilterSpinnerItem> filterSpinnerItems;
    private Spinner filterSpinner;
    private LinearLayoutManager storyRviewLayoutManager;

    private AuthorProfileDialogFragment authorProfileDialogFragment;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private Object lastLoadedStorySortValue;
    private Toast toast;

    private FilterType currentFilter;

    private int refreshIterations;
    private int maxRefreshIterations;
    private int maxStoryCards;

    boolean loadedFirstStories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        loadedFirstStories = false;
        lastLoadedStorySortValue = null;

        refreshIterations = 0;
        maxStoryCards = 10;
        maxRefreshIterations = 5; //TODO: adjust this

        storyRef = FirebaseDatabase.getInstance().getReference(storyDBKey);

        backgroundTaskExecutor = Executors.newFixedThreadPool(5);

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        doLoginCheck(); //TODO: DO THIS FIRST

        createStorySwipeToRefresh();
        createStoryRecyclerView();
        createFilterSpinner();

        loadFirstStories();

        //Define handling for data results from the background thread
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if(msg.getData() == null) {
                    return;
                }

                switch(msg.getData().getString("type")) {
                    case("bookmarks"): {
                        currentFilter = FilterType.get("Bookmarks");
                        currentFilter.setBookmarksFilter(msg.getData().getStringArrayList("bookmarks"));
                        refreshStories();
                        break;
                    }
                    case("followedAuthors"): {
                        currentFilter = FilterType.get("Followed Authors");
                        currentFilter.setFollowsFilter(msg.getData().getStringArrayList("follows"));
                        refreshStories();
                        break;
                    }
                    case("authorProfile"): {

                        if (authorProfileDialogFragment != null) {
                            authorProfileDialogFragment.dismiss();
                        }

                        //Show a generic error instead of loading author profile if data wasn't retrieved properly
                        if (msg.getData() == null || msg.getData().getInt("result") > 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                            return;
                        }

                        //If all is well, show the author profile fragment with the retrieved data
                        authorProfileDialogFragment = new AuthorProfileDialogFragment();
                        authorProfileDialogFragment.setArguments(msg.getData());
                        authorProfileDialogFragment.show(getSupportFragmentManager(), "AuthorProfileDialogFragment");
                        break;
                   }
                }
            }
        };

        scrollListener = new EndlessScrollListener(storyRviewLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (page < 2) {
                    return;
                }
                refreshIterations = 0;
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
        filterSpinnerItems = new ArrayList<>();
        String[] resourceArray = getResources().getStringArray(R.array.filter_spinner_options);
        for (String option : resourceArray) {
            filterSpinnerItems.add(new FilterSpinnerItem(option));
        }
        filterSpinner = findViewById(R.id.filterSpinner);
        spinnerAdapter = new FilterSpinnerAdapter(this, filterSpinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setOnItemSelectedListener(this);
        currentFilter = FilterType.NONE;
    }

    //Kick the user out of the Feed if they aren't logged in for some reason
    private void doLoginCheck() {
        if (!AuthUtils.userIsLoggedIn(getApplicationContext())) {
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

        storyRviewAdapter = new StoryRviewAdapter(storyCardList, getApplicationContext(), this);


        StoryRviewCardClickListener storyClickListener = new StoryRviewCardClickListener() {
            @Override
            public void onStoryClick(int position) {
                if(storyCardList.get(position).getStory().getIsDraft()) {
                    goToPublishStory(storyCardList.get(position).getStory());
                    return;
                }

                goToReadStory(storyCardList.get(position).getStory());
            }
        };

        storyRviewAdapter.setOnStoryClickListener(storyClickListener);

        storyRview.setAdapter(storyRviewAdapter);
        storyRview.setLayoutManager(storyRviewLayoutManager);
    }

    private void loadFirstStories() {

        FilterType filter = FilterType.NONE;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("feedFilter")) {
                String intentFilter = extras.getString("feedFilter");
                int pos = 0;
                for (FilterSpinnerItem item : filterSpinnerItems) {
                    if (item.getFilterTitle().equals(intentFilter)) {
                        pos = filterSpinnerItems.indexOf(item);
                        break;
                    }
                }
                filterSpinner.setSelection(pos);
                filter = FilterType.get(intentFilter);
            }
            if (extras.containsKey("authorId")) {
                String authorId = extras.getString("authorId");
                filter.setAuthorFilter(authorId);
            }
        }


        loadStoryData();

    }

    private void loadNextStories() {
        if (storyCardList.size() <= 0) {
            return;
        }

        Toast.makeText(StoryFeedActivity.this,
                "Loading more stories",
                Toast.LENGTH_SHORT)
                .show();

        loadStoryData();
    }

    private void loadStoryData() {

        applyFilter(currentFilter);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Story story = dataSnapshot.getValue(Story.class);


                    if (story == null) {
                        return;
                    }

                    //Track the last seen id, even if excluded by filter
                    lastLoadedStorySortValue = currentFilter.getSortPropertyValue(story);
                    //wrc
                    int i = 44;

                    if (currentFilter.includes(getApplicationContext(), story)) {
                        //TODO: this could be more efficient the way it was originally
                        int pos;
                        boolean replaced = false;
                        for (pos = 0; pos < storyCardList.size(); pos++) {
                            if (storyCardList.get(pos).getID().equals(story.getId())) {
                                storyCardList.set(pos, new StoryRviewCard(story));
                                storyRviewAdapter.notifyItemChanged(pos);
                                replaced = true;
                            }
                        }

                        if (!replaced) {
                            storyCardList.add(pos, new StoryRviewCard(story));
                            storyRviewAdapter.notifyItemInserted(pos);
                        }

                        loadedFirstStories = true;
                    }

                    feedSwipeRefresh.setRefreshing(false);
                }

                if (storyCardList.size() <= maxStoryCards && refreshIterations < maxRefreshIterations) {
                    refreshIterations++;
                    loadStoryData();
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
        refreshIterations = 0;
        lastLoadedStorySortValue = null;

        loadStoryData();
    }

    private void goToCreateStory() {
        Intent intent = new Intent(this, CreateStoryActivity.class);
        startActivity(intent);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //Publish a (draft) story, if applicable - called by the holder on the recycler view
    private void goToPublishStory(Story story) {
        Intent intent = new Intent(this,PublishStoryActivity.class);
        intent.putExtra("prompt",story.getPromptText());
        intent.putExtra("story",story.getStoryText());
        intent.putExtra("storyId",story.getId());
        startActivity(intent);
    }

    private void goToReadStory(Story story) {
        Intent intent = new Intent(this, ReadStoryActivity.class);
        intent.putExtra("story", story);
        startActivity(intent);
    }


    //Listener method for filter spinner item selection
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        FilterSpinnerItem item = (FilterSpinnerItem) adapterView.getItemAtPosition(i);
        String selection = item.getFilterTitle();

        switch(selection) {
            case("Bookmarks"): {
                loadBookmarks();
                break;
            }
            case("Followed Authors"): {
                loadFollowedAuthors();
                break;
            }
            default: {
                Bundle extras = getIntent().getExtras();
                String authorId = "";

                if (extras != null) {
                    if (extras.containsKey("authorId")) {
                        authorId = extras.getString("authorId");
                    }
                }

                currentFilter = FilterType.get(selection);
                currentFilter.setAuthorFilter(authorId);
                refreshStories();
                break;
            }
        }

    }

    //Listener method for filter spinner item deselection
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    //Apply the requested filter to the current query
    private void applyFilter(FilterType filter) {
        currentFilter = filter;
        query = currentFilter.getQuery(storyRef, lastLoadedStorySortValue);
    }

    /*
        Handler for loading bookmarks when a user picks bookmarked filter
     */
    private void loadBookmarks() {

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FBUtils.getBookmarks(getApplicationContext(), new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(ArrayList<String> bookmarks) {

                        //Set up a bundle
                        Bundle resultData = new Bundle();
                        resultData.putString("type","bookmarks");
                        resultData.putStringArrayList("bookmarks", bookmarks);

                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);

                        //Notify the activity that bookmarks have been retrieved
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }

    /*
        Handler for loading followed user filter when a user picks followed user filter
     */
    private void loadFollowedAuthors() {

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FBUtils.getUser(getApplicationContext(), AuthUtils.getLoggedInUserID(getApplicationContext()), new Consumer<User>() {
                    @Override
                    public void accept(User user) {
                        //Set up a bundle
                        Bundle resultData = new Bundle();
                        resultData.putString("type", "followedAuthors");
                        resultData.putStringArrayList("follows",user.getFollows());

                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);

                        //Notify the activity that follow data has been retrieved
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }

    /*
        Card onClick handler for opening an author profile
     */
    public void handleAuthorClick (String username){

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FBUtils.getAuthorProfile(getApplicationContext(), username, new Consumer<AuthorProfile>() {
                    @Override
                    public void accept(AuthorProfile authorProfile) {

                        //Set up a bundle of author profile result data
                        Bundle resultData = new Bundle();
                        resultData.putString("type", "authorProfile");
                        resultData.putInt("result", authorProfile != null ? 0 : 1); //If authorProfile, there's some issue - handle error

                        if (authorProfile != null) {
                            resultData.putString("authorId", authorProfile.getAuthorId());
                            resultData.putInt("storyCount", authorProfile.getStoryCount());
                            resultData.putInt("loveCount", authorProfile.getLoveCount());
                            resultData.putInt("followCount", authorProfile.getFollowCount());
                            resultData.putBoolean("following", authorProfile.following());
                            resultData.putInt("profileIcon", authorProfile.getProfileIcon());
                        }
                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);

                        //Notify the activity that profile data has been retrieved
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }
}