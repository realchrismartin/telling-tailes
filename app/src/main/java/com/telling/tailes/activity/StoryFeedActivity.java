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

        toast = Toast.makeText(getApplicationContext(), getString(R.string.empty_string), Toast.LENGTH_SHORT);

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

                switch(msg.getData().getString(getString(R.string.background_task_result_type))) {
                    case(StringUtils.backgroundResultPropertyStoryData): {
                        if(msg.getData().getInt(getString(R.string.background_task_result_result)) != 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                            return;
                        }

                        if(msg.getData().getString(getString(R.string.background_task_result_data_last_type)) == null) {
                            return;
                        }

                        //Ignore any result if it was sent by the feed PRIOR to a filter refresh
                        if(msg.getData().getLong(getString(R.string.background_task_result_data_timestamp)) < filterLastChangedTimestamp) {
                            return;
                        }

                        //Set data type of last loaded story sort value
                        if(msg.getData().getString(getString(R.string.background_task_result_data_last_type)).equals(getString(R.string.double_string))) {
                            lastLoadedStorySortValue = msg.getData().getDouble(getString(R.string.background_task_result_data_last_story));
                        } else {
                            lastLoadedStorySortValue = msg.getData().getString(getString(R.string.background_task_result_data_last_story));
                        }

                        int storyCount=msg.getData().getInt(getString(R.string.background_task_result_data_story_count));

                        removeLoadingCard();

                        for(int i=0;i<storyCount;i++) {

                            Story story = (Story)msg.getData().getSerializable(getString(R.string.background_task_result_data_story_) + (i + 1));
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
                        if(msg.getData().getInt(getString(R.string.background_task_result_result)) != 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                        }
                        break;
                    }
                    case(StringUtils.backgroundResultPropertyStoryBookmark): {
                        currentFilter = FilterType.get(getString(R.string.filter_type_bookmarks));
                        currentFilter.setBookmarksFilter(msg.getData().getStringArrayList(getString(R.string.background_task_result_data_bookmarks)));
                        refreshStories();
                        break;
                    }
                    case(StringUtils.backgroundResultPropertyFollowed): {
                        currentFilter = FilterType.get(getString(R.string.filter_type_by_followed_authors));
                        currentFilter.setFollowsFilter(msg.getData().getStringArrayList(getString(R.string.background_task_result_follows)));
                        refreshStories();
                        break;
                    }
                    case(StringUtils.backgroundResultPropertyAuthorProfile): {

                        if (authorProfileDialogFragment != null) {
                            authorProfileDialogFragment.dismiss();
                        }

                        //Show a generic error instead of loading author profile if data wasn't retrieved properly
                        if (msg.getData() == null || msg.getData().getInt(getString(R.string.background_task_result_result)) > 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                            return;
                        }

                        //If all is well, show the author profile fragment with the retrieved data
                        authorProfileDialogFragment = new AuthorProfileDialogFragment();
                        authorProfileDialogFragment.setArguments(msg.getData());
                        authorProfileDialogFragment.show(getSupportFragmentManager(), getString(R.string.author_profile_dialog_fragment));
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
        getSupportFragmentManager().setFragmentResultListener(getString(R.string.author_profile_dialog_fragment_follow), this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {

                if(currentFilter == FilterType.FOLLOWING) {

                    boolean followed = bundle.getBoolean(getString(R.string.background_task_result_followed));
                    String username = bundle.getString(getString(R.string.background_task_result_username));

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
                        bundle.putString(getString(R.string.background_task_result_type), StringUtils.backgroundResultPropertyStoryTokenRefresh);
                        bundle.putInt(getString(R.string.background_task_result_result), user == null ? 1 : 0);
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
            if (extras.containsKey(getString(R.string.intent_extra_feed_filter))) {
                String intentFilter = extras.getString(getString(R.string.intent_extra_feed_filter));
                int pos = 0;
                if (intentFilter.equals(getString(R.string.filter_type_by_author))) {
                    if (extras.containsKey(getString(R.string.intent_extra_author_id))) {
                        String authorId = extras.getString(getString(R.string.intent_extra_author_id));

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
            if (extras.containsKey(getString(R.string.intent_extra_author_id))) {
                String authorId = extras.getString(getString(R.string.intent_extra_author_id));
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
                               data.putDouble(getString(R.string.background_task_result_data_last_story),(Double)currentFilter.getSortPropertyValue(story));
                               data.putString(getString(R.string.background_task_result_data_last_type), getString(R.string.double_string));
                           } else {
                               data.putString(getString(R.string.background_task_result_data_last_story),(String)currentFilter.getSortPropertyValue(story));
                               data.putString(getString(R.string.background_task_result_data_last_type),getString(R.string.string_string));
                           }


                           if (currentFilter.includes(getApplicationContext(), story)) {
                               storyCount++;
                               data.putSerializable(getString(R.string.background_task_result_data_story_) + storyCount,story);
                           }
                       }

                       data.putInt(getString(R.string.background_task_result_data_story_count),storyCount);
                       data.putString(getString(R.string.background_task_result_type),StringUtils.backgroundResultPropertyStoryData);
                       data.putInt(getString(R.string.background_task_result_result),0);
                       data.putLong(getString(R.string.background_task_result_data_timestamp),timestamp);
                       message.setData(data);

                       backgroundTaskResultHandler.sendMessage(message);

                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {
                       Message message = new Message();
                       Bundle data = new Bundle();
                       data.putString(getString(R.string.background_task_result_type),StringUtils.backgroundResultPropertyStoryData);
                       data.putInt(getString(R.string.background_task_result_result),1);
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
        intent.putExtra(getString(R.string.intent_extra_prompt),story.getPromptText());
        intent.putExtra(getString(R.string.intent_extra_story),story.getStoryText());
        intent.putExtra(getString(R.string.intent_extra_story_id),story.getId());
        startActivity(intent);
    }

    //Navigate to the Read Story activity
    private void goToReadStory(Story story) {
        Intent intent = new Intent(this, ReadStoryActivity.class);
        intent.putExtra(getString(R.string.intent_extra_story), story);
        startActivity(intent);
    }


    //Listener method for filter spinner item selection
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        //Set the timestamp for the filter having been changed
        filterLastChangedTimestamp = new Date().toInstant().toEpochMilli();

        FilterSpinnerItem item = (FilterSpinnerItem) adapterView.getItemAtPosition(i);
        String selection = item.getFilterTitle();

        if (selection.contains(getString(R.string.apostrophe_s))) {
            selection = getString(R.string.filter_type_by_author);
        } else {
            if (filterSpinnerItems.get(filterSpinnerItems.size() - 1).getFilterTitle().contains(getString(R.string.apostrophe_s))) {
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
                String authorId = getString(R.string.empty_string);

                if (extras != null) {
                    if (extras.containsKey(getString(R.string.intent_extra_author_id))) {
                        authorId = extras.getString(getString(R.string.intent_extra_author_id));
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
                        resultData.putString(getString(R.string.background_task_result_type),StringUtils.backgroundResultPropertyStoryBookmark);
                        resultData.putStringArrayList(getString(R.string.background_task_result_data_bookmarks), bookmarks);

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
                        resultData.putString(getString(R.string.background_task_result_type), StringUtils.backgroundResultPropertyFollowed);
                        resultData.putStringArrayList(getString(R.string.background_task_result_follows),user.getFollows());

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
                        resultData.putString(getString(R.string.background_task_result_type), StringUtils.backgroundResultPropertyAuthorProfile);
                        resultData.putInt(getString(R.string.background_task_result_result), authorProfile != null ? 0 : 1); //If authorProfile, there's some issue - handle error

                        if (authorProfile != null) {
                            resultData.putString(getString(R.string.background_task_result_data_author_id), authorProfile.getAuthorId());
                            resultData.putInt(getString(R.string.background_task_result_data_story_count), authorProfile.getStoryCount());
                            resultData.putInt(getString(R.string.background_task_result_data_love_count), authorProfile.getLoveCount());
                            resultData.putInt(getString(R.string.background_task_result_data_follow_count), authorProfile.getFollowCount());
                            resultData.putBoolean(getString(R.string.background_task_result_data_following), authorProfile.following());
                            resultData.putInt(getString(R.string.background_task_result_data_profile_icon), authorProfile.getProfileIcon());
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