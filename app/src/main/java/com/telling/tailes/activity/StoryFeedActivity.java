package com.telling.tailes.activity;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import com.telling.tailes.adapter.StoryRviewAdapter;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.card.StoryRviewCardClickListener;
import com.telling.tailes.fragment.AuthorProfileDialogFragment;
import com.telling.tailes.model.AuthorProfile;
import com.telling.tailes.model.Story;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;
import com.telling.tailes.util.FilterType;
import com.telling.tailes.util.EndlessScrollListener;
import com.telling.tailes.R;
import com.telling.tailes.util.GPTUtils;

public class StoryFeedActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnAuthorClickCallbackListener {

    private static final String storyDBKey = "stories"; //TODO move to app-wide variable?

    private DatabaseReference storyRef;

    private EndlessScrollListener scrollListener;
    private Query initialQuery;
    private int queryIndex;

    private ArrayList<StoryRviewCard> storyCardList = new ArrayList<>();
    private int loadingCardIndex = -1;
    private SwipeRefreshLayout feedSwipeRefresh;
    private RecyclerView storyRview;
    private StoryRviewAdapter storyRviewAdapter;

    private ArrayAdapter<CharSequence> spinnerAdapter;
    private Spinner filterSpinner;
//    private RecyclerView.LayoutManager storyRviewLayoutManager;
    private LinearLayoutManager storyRviewLayoutManager;

    private AuthorProfileDialogFragment authorProfileDialogFragment;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private String lastLoadedStoryId;
    private Toast toast;

    private FilterType currentFilter;

    private int refreshIterations;
    private int maxRefreshIterations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_feed);
        lastLoadedStoryId = "";
        maxRefreshIterations = 5; //TODO: adjust this
        storyRef = FirebaseDatabase.getInstance().getReference(storyDBKey);
        toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        doLoginCheck();

        createStorySwipeToRefresh();
        createStoryRecyclerView();
        createFilterSpinner();

        loadFirstStories();

        //Set up background executor for handling author profile data request threads
        backgroundTaskExecutor = Executors.newFixedThreadPool(2);

        //Define handling for author profile data results from the background thread
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

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
                authorProfileDialogFragment.show(getSupportFragmentManager(),"AuthorProfileDialogFragment");

            }
        };

        scrollListener = new EndlessScrollListener(storyRviewLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                addLoadingCard();
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
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.filter_spinner_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);
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
        storyRviewAdapter = new StoryRviewAdapter(storyCardList,getApplicationContext(),this);
        addLoadingCard();

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
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.containsKey("feedFilter")) {

                String intentFilter = extras.getString("feedFilter");
                String authorId = "";
                filterSpinner.setSelection(spinnerAdapter.getPosition(intentFilter));

                //If an author is also passed, apply the author's username to the filter
                if(extras.containsKey("authorId")) {
                    authorId = extras.getString("authorId");
                }

                FilterType filter = FilterType.get(intentFilter,authorId);

                applyFilter(filter);
            }
        }

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

//                    if (loadingCardIndex > -1) {
//                        removeLoadingCard();
//                        pos--;
//                    }

                    storyCardList.add(pos, new StoryRviewCard(story));
                    storyRviewAdapter.notifyItemInserted(pos);

//                    if (loadingCardIndex < 0) {
//                        addLoadingCard();
//                    }
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
        loadStoryData(initialQuery);
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
        String selection = adapterView.getItemAtPosition(i).toString();


        Bundle extras = getIntent().getExtras();
        String authorId = "";

        if (extras != null) {
            if (extras.containsKey("authorId")) {
                authorId = extras.getString("authorId");
            }
        }

        FilterType filter = FilterType.get(selection,authorId);
        applyFilter(filter);
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

    /*
        Card onClick handler for opening an author profile
     */
    public void handleAuthorClick(String username)
    {

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                FBUtils.getAuthorProfile(getApplicationContext(),username, new Consumer<AuthorProfile>() {
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

    public void addLoadingCard() {
        if (loadingCardIndex < 0) {
            loadingCardIndex = storyCardList.size();
            storyCardList.add(new StoryRviewCard());
            storyRviewAdapter.notifyItemInserted(loadingCardIndex);
        }
    }

    public void removeLoadingCard() {
        if (storyCardList.size() - 1 == loadingCardIndex) {
            storyCardList.remove(storyCardList.size() - 1);
            storyRviewAdapter.notifyItemRemoved(loadingCardIndex);
            loadingCardIndex = -1;
        }
    }
}