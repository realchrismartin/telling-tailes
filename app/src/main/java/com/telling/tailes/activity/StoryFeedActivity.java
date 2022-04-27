package com.telling.tailes.activity;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

        createStorySwipeToRefresh();
        createStoryRecyclerView();
        createFilterSpinner();

        createListeners();

        //Check if user is logged in
        //Leave the feed if not
        //Update the user's messaging token if the local one differs from the database
        doLoginCheckCreate();

        loadFirstStories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        doLoginCheckResume();
    }

    //Set up listeners
    private void createListeners() {

        //Define handling for data results from the background thread
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if(msg.getData() == null) {
                    return;
                }

                switch(msg.getData().getString("type")) {
                    case("storyData"): {
                        if(msg.getData().getInt("result") != 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                            return;
                        }

                        if(msg.getData().getString("last_type") == null) {
                            return;
                        }

                        //Set data type of last loaded story sort value
                        if(msg.getData().getString("last_type").equals("double")) {
                            lastLoadedStorySortValue = msg.getData().getDouble("last_story");
                        } else {
                            lastLoadedStorySortValue = msg.getData().getString("last_story");
                        }

                        int storyCount=msg.getData().getInt("story_count");

                        for(int i=0;i<storyCount;i++) {

                            Story story = (Story)msg.getData().getSerializable("story_" + (i + 1));
                            boolean replaced = false;
                            int pos = 0;

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

                        //Recurse if the story card list isn't loaded yet
                        if (storyCardList.size() <= maxStoryCards && refreshIterations < maxRefreshIterations) {
                            refreshIterations++;
                            loadStoryData();
                            return;
                        }

                        //Stop refreshing when complete
                        feedSwipeRefresh.setRefreshing(false);
                        break;
                    }
                    case("tokenRefresh"): {
                        if(msg.getData().getInt("result") != 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                        }
                        break;
                    }
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

        //Set up a listener to receive follow/unfollow data from the profile dialog and act accordingly if the current filter is Following
        getSupportFragmentManager().setFragmentResultListener("AuthorProfileDialogFragmentFollow", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {

                if(currentFilter == FilterType.FOLLOWING) {

                    boolean followed = bundle.getBoolean("followed");
                    String username = bundle.getString("username");

                    if(followed) {
                        currentFilter.addFollowFilter(username);
                    } else {
                        currentFilter.removeFollowFilter(username);
                    }

                    refreshStories();
                }
            }
        });

        findViewById(R.id.goToCreateStoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCreateStory();
            }
        });
    }

    //Set up the filter spinner
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
    private boolean doLoginCheckResume() {
        if (!AuthUtils.userIsLoggedIn(getApplicationContext())) {
            goToLogin();
            return false;
        }

        return true;
    }

    //Kick the user out of the Feed if they aren't logged in for some reason
    private void doLoginCheckCreate() {

        if(!doLoginCheckResume()) {
            return;
        }

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //Update the locally stored token, or get a new one, if needed
                AuthUtils.updateUserToken(getApplicationContext(), AuthUtils.getMessagingToken(getApplicationContext()), new Consumer<User>() {
                    @Override
                    public void accept(User user) {
                        Message resultMessage = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("type","tokenRefresh");
                        bundle.putInt("result", user == null ? 1 : 0);
                        resultMessage.setData(bundle);
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }

    //Set up the swipe refresh functionality
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

    //Set up the recycler view
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

    //Load initial stories, setting up any filter passed in from the current intent
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

    //Show a toast indicating more stories are being loaded, then load more stories (in a background thread)
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


    //Load story data in a background thread after applying filter
    //Call the main thread when done
    private void loadStoryData() {

        applyFilter(currentFilter);

        backgroundTaskExecutor.execute(new Runnable() {
           @Override
           public void run() {

               query.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {

                       Message message = new Message();
                       Bundle data = new Bundle();

                       int storyCount = 0;

                       for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                           Story story = dataSnapshot.getValue(Story.class);

                           if (story == null) {
                               return;
                           }

                           if(currentFilter.getSortPropertyValue(story) instanceof Double) {
                               data.putDouble("last_story",(Double)currentFilter.getSortPropertyValue(story));
                               data.putString("last_type","double");
                           } else {
                               data.putString("last_story",(String)currentFilter.getSortPropertyValue(story));
                               data.putString("last_type","string");
                           }


                           if (currentFilter.includes(getApplicationContext(), story)) {
                               storyCount++;
                               data.putSerializable("story_" + storyCount,story);
                           }
                       }

                       data.putInt("story_count",storyCount);
                       data.putString("type","storyData");
                       data.putInt("result",0);
                       message.setData(data);

                       backgroundTaskResultHandler.sendMessage(message);

                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {
                       Message message = new Message();
                       Bundle data = new Bundle();
                       data.putString("type","storyData");
                       data.putInt("result",1);
                       message.setData(data);
                       backgroundTaskResultHandler.sendMessage(message);
                   }
               });
           }
       });
    }

    //Handle pull-down refresh for feed
    private void refreshStories() {
        storyCardList.clear();
        storyRviewAdapter.notifyDataSetChanged();
        scrollListener.resetState();
        refreshIterations = 0;
        lastLoadedStorySortValue = null;

        loadStoryData();
    }

    //Navigate to the Create Story activity
    private void goToCreateStory() {
        Intent intent = new Intent(this, CreateStoryActivity.class);
        startActivity(intent);
    }

    //Navigate to the Login activity
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

    //Navigate to the Read Story activity
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