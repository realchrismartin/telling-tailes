package com.telling.tailes.activity;

import java.util.ArrayList;
import java.util.Date;
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
import com.telling.tailes.util.StringUtils;

public class StoryFeedActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnAuthorClickCallbackListener {


    private DatabaseReference storyRef;

    private EndlessScrollListener scrollListener;
    private Query query;

    private ArrayList<StoryRviewCard> storyCardList = new ArrayList<>();
    private int loadingCardIndex = -1;
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

    private long filterLastChangedTimestamp;

    boolean loadedFirstStories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);

        filterLastChangedTimestamp = new Date().toInstant().toEpochMilli();

        loadedFirstStories = false;
        lastLoadedStorySortValue = null;

        refreshIterations = 0;
        maxStoryCards = 10;
        maxRefreshIterations = 5;

        storyRef = FirebaseDatabase.getInstance().getReference(StringUtils.storyDBKey);

        backgroundTaskExecutor = Executors.newFixedThreadPool(5);

        toast = Toast.makeText(getApplicationContext(), StringUtils.emptyString, Toast.LENGTH_SHORT);

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

                switch(msg.getData().getString(StringUtils.backgroundTaskResultType)) {
                    case(StringUtils.backgroundResultPropertyStoryData): {
                        if(msg.getData().getInt(StringUtils.backgroundTaskResultResult) != 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                            return;
                        }

                        if(msg.getData().getString(StringUtils.backgroundTaskResultDataLastType) == null) {
                            return;
                        }

                        //Ignore any result if it was sent by the feed PRIOR to a filter refresh
                        if(msg.getData().getLong(StringUtils.backgroundTaskResultDataTimeStamp) < filterLastChangedTimestamp) {
                            return;
                        }

                        //Set data type of last loaded story sort value
                        if(msg.getData().getString(StringUtils.backgroundTaskResultDataLastType).equals(StringUtils.doubleString)) {
                            lastLoadedStorySortValue = msg.getData().getDouble(StringUtils.backgroundTaskResultDataLastStory);
                        } else {
                            lastLoadedStorySortValue = msg.getData().getString(StringUtils.backgroundTaskResultDataLastStory);
                        }

                        int storyCount=msg.getData().getInt(StringUtils.backgroundTaskResultDataStoryCount);

                        removeLoadingCard();

                        for(int i=0;i<storyCount;i++) {

                            Story story = (Story)msg.getData().getSerializable(StringUtils.backgroundTaskResultDataStoryUnderscore + (i + 1));
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
                    case(StringUtils.backgroundResultPropertyStoryTokenRefresh): {
                        if(msg.getData().getInt(StringUtils.backgroundTaskResultResult) != 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                        }
                        break;
                    }
                    case(StringUtils.backgroundResultPropertyStoryBookmark): {
                        currentFilter = FilterType.get(StringUtils.filterTypeBookmarks);
                        currentFilter.setBookmarksFilter(msg.getData().getStringArrayList(StringUtils.backgroundTaskResultDataBookmarks));
                        refreshStories();
                        break;
                    }
                    case(StringUtils.backgroundResultPropertyFollowed): {
                        currentFilter = FilterType.get(StringUtils.filterTypeByFollowedAuthors);
                        currentFilter.setFollowsFilter(msg.getData().getStringArrayList(StringUtils.backgroundTaskResultFollows));
                        refreshStories();
                        break;
                    }
                    case(StringUtils.backgroundResultPropertyAuthorProfile): {

                        if (authorProfileDialogFragment != null) {
                            authorProfileDialogFragment.dismiss();
                        }

                        //Show a generic error instead of loading author profile if data wasn't retrieved properly
                        if (msg.getData() == null || msg.getData().getInt(StringUtils.backgroundTaskResultResult) > 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                            return;
                        }

                        //If all is well, show the author profile fragment with the retrieved data
                        authorProfileDialogFragment = new AuthorProfileDialogFragment();
                        authorProfileDialogFragment.setArguments(msg.getData());
                        authorProfileDialogFragment.show(getSupportFragmentManager(), StringUtils.authorProfileDialogFragment);
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
        getSupportFragmentManager().setFragmentResultListener(StringUtils.authorProfileFollowDialogFragment, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {

                if(currentFilter == FilterType.FOLLOWING) {

                    boolean followed = bundle.getBoolean(StringUtils.backgroundTaskResultFollowed);
                    String username = bundle.getString(StringUtils.backgroundTaskResultUsername);

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
                        bundle.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyStoryTokenRefresh);
                        bundle.putInt(StringUtils.backgroundTaskResultResult, user == null ? 1 : 0);
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
            if (extras.containsKey(StringUtils.intentExtraFeedFilter)) {
                String intentFilter = extras.getString(StringUtils.intentExtraFeedFilter);
                int pos = 0;
                if (intentFilter.equals(StringUtils.filterTypeByAuthor)) {
                    if (extras.containsKey(StringUtils.intentExtraAuthorId)) {
                        String authorId = extras.getString(StringUtils.intentExtraAuthorId);

                        filterSpinnerItems.add(new FilterSpinnerItem(authorId + getString(R.string.author_profile_read_option)));
                        spinnerAdapter.notifyDataSetChanged();
                        pos = filterSpinnerItems.size() - 1;
                    }
                } else {
                    for (FilterSpinnerItem item : filterSpinnerItems) {
                        if (item.getFilterTitle().equals(intentFilter)) {
                            pos = filterSpinnerItems.indexOf(item);
                            break;
                        }
                    }
                }
                filterSpinner.setSelection(pos);
                filter = FilterType.get(intentFilter);
            }
            if (extras.containsKey(StringUtils.intentExtraAuthorId)) {
                String authorId = extras.getString(StringUtils.intentExtraAuthorId);
                filter.setAuthorFilter(authorId);
            }
        }

        currentFilter = filter;

        loadStoryData();

    }

    //Show a toast indicating more stories are being loaded, then load more stories (in a background thread)
    private void loadNextStories() {
        if (storyCardList.size() <= 0) {
            return;
        }

        loadStoryData();
    }


    //Load story data in a background thread after applying filter
    //Call the main thread when done
    private void loadStoryData() {

        addLoadingCard();

        applyFilter(currentFilter);

        long timestamp = filterLastChangedTimestamp;

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
                               data.putDouble(StringUtils.backgroundTaskResultDataLastStory, (Double)currentFilter.getSortPropertyValue(story));
                               data.putString(StringUtils.backgroundTaskResultDataLastType, StringUtils.doubleString);
                           } else {
                               data.putString(StringUtils.backgroundTaskResultDataLastStory, (String)currentFilter.getSortPropertyValue(story));
                               data.putString(StringUtils.backgroundTaskResultDataLastType, StringUtils.stringString);
                           }


                           if (currentFilter.includes(getApplicationContext(), story)) {
                               storyCount++;
                               data.putSerializable(StringUtils.backgroundTaskResultDataStoryUnderscore + storyCount,story);
                           }
                       }

                       data.putInt(StringUtils.backgroundTaskResultDataStoryCount, storyCount);
                       data.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyStoryData);
                       data.putInt(StringUtils.backgroundTaskResultResult, 0);
                       data.putLong(StringUtils.backgroundTaskResultDataTimeStamp, timestamp);
                       message.setData(data);

                       backgroundTaskResultHandler.sendMessage(message);

                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {
                       Message message = new Message();
                       Bundle data = new Bundle();
                       data.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyStoryData);
                       data.putInt(StringUtils.backgroundTaskResultResult,1);
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
        loadingCardIndex = -1;
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
        intent.putExtra(StringUtils.intentExtraPrompt, story.getPromptText());
        intent.putExtra(StringUtils.intentExtraStory, story.getStoryText());
        intent.putExtra(StringUtils.intentExtraStoryId, story.getId());
        startActivity(intent);
    }

    //Navigate to the Read Story activity
    private void goToReadStory(Story story) {
        Intent intent = new Intent(this, ReadStoryActivity.class);
        intent.putExtra(StringUtils.intentExtraStory, story);
        startActivity(intent);
    }


    //Listener method for filter spinner item selection
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        //Set the timestamp for the filter having been changed
        filterLastChangedTimestamp = new Date().toInstant().toEpochMilli();

        FilterSpinnerItem item = (FilterSpinnerItem) adapterView.getItemAtPosition(i);
        String selection = item.getFilterTitle();

        if (selection.contains(StringUtils.apostropheS)) {
            selection = StringUtils.filterTypeByAuthor;
        } else {
            if (filterSpinnerItems.get(filterSpinnerItems.size() - 1).getFilterTitle().contains(StringUtils.apostropheS)) {
                filterSpinnerItems.remove(filterSpinnerItems.size() - 1);
                spinnerAdapter.notifyDataSetChanged();
            }
        }

        switch(selection) {
            case(StringUtils.filterTypeBookmarks): {
                loadBookmarks();
                break;
            }
            case(StringUtils.filterTypeByFollowedAuthors): {
                loadFollowedAuthors();
                break;
            }
            default: {
                Bundle extras = getIntent().getExtras();
                String authorId = StringUtils.emptyString;

                if (extras != null) {
                    if (extras.containsKey(StringUtils.intentExtraAuthorId)) {
                        authorId = extras.getString(StringUtils.intentExtraAuthorId);
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
                        resultData.putString(StringUtils.backgroundTaskResultType,StringUtils.backgroundResultPropertyStoryBookmark);
                        resultData.putStringArrayList(StringUtils.backgroundTaskResultDataBookmarks, bookmarks);

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
                        resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyFollowed);
                        resultData.putStringArrayList(StringUtils.backgroundTaskResultFollows, user.getFollows());

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
                        resultData.putString(StringUtils.backgroundTaskResultType, StringUtils.backgroundResultPropertyAuthorProfile);
                        resultData.putInt(StringUtils.backgroundTaskResultResult, authorProfile != null ? 0 : 1); //If authorProfile, there's some issue - handle error

                        if (authorProfile != null) {
                            resultData.putString(StringUtils.backgroundTaskResultDataAuthorId, authorProfile.getAuthorId());
                            resultData.putInt(StringUtils.backgroundTaskResultDataStoryCount, authorProfile.getStoryCount());
                            resultData.putInt(StringUtils.backgroundTaskResultDataLoveCount, authorProfile.getLoveCount());
                            resultData.putInt(StringUtils.backgroundTaskResultDataFollowCount, authorProfile.getFollowCount());
                            resultData.putBoolean(StringUtils.backgroundTaskResultDataFollowing, authorProfile.following());
                            resultData.putInt(StringUtils.backgroundTaskResultDataProfileIcon, authorProfile.getProfileIcon());
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


    public void addLoadingCard() {
        if (loadingCardIndex < 0) {
            loadingCardIndex = storyCardList.size();
            storyCardList.add(new StoryRviewCard());
            storyRviewAdapter.notifyItemInserted(loadingCardIndex);
        }
    }

    public void removeLoadingCard() {
        if (loadingCardIndex >= 0) {
            if (storyCardList.size() - 1 == loadingCardIndex) {
                storyCardList.remove(storyCardList.size() - 1);
                storyRviewAdapter.notifyItemRemoved(loadingCardIndex);
                loadingCardIndex = -1;
            }
        }
    }
}