package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.telling.tailes.R;
import com.telling.tailes.adapter.AuthorRviewAdapter;
import com.telling.tailes.card.AuthorRviewCard;
import com.telling.tailes.card.AuthorRviewCardClickListener;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FollowedAuthorsActivity extends AppCompatActivity {

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private ArrayList<AuthorRviewCard> authorCardList;
    private RecyclerView authorRview;
    private LinearLayoutManager authorRviewLayoutManager;
    private AuthorRviewAdapter authorRviewAdapter;

    private ArrayList<String> followedAuthorIds;

    private SwipeRefreshLayout authorPullRefresh;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_authors);

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        backgroundTaskExecutor = Executors.newFixedThreadPool(3);
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.getData() == null) {
                    return;
                }

                switch (msg.getData().getString("type")) {
                    case("followedAuthors"): {
                        followedAuthorIds = msg.getData().getStringArrayList("follows");
                        for (String authorId : followedAuthorIds) {
                            authorCardList.add(new AuthorRviewCard(authorId));
                            authorRviewAdapter.notifyDataSetChanged();
                        }
                        authorPullRefresh.setRefreshing(false);
                        break;
                    }
                    default: {
                        if (msg.getData() == null || msg.getData().getInt("result") > 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                            return;
                        }
                    }
                }
            }
        };

        createAuthorPullToRefresh();
        createAuthorRecyclerView();
        loadFirstAuthors();
    }

    private void createAuthorRecyclerView() {
        authorCardList = new ArrayList<>();
        authorRviewLayoutManager = new LinearLayoutManager(this);
        authorRview = findViewById(R.id.author_recycler_view);
        authorRview.setHasFixedSize(true);
        authorRviewAdapter = new AuthorRviewAdapter(authorCardList, getApplicationContext());
        AuthorRviewCardClickListener authorClickListener = new AuthorRviewCardClickListener() {
            @Override
            public void onAuthorClick(int position) {
                Log.d("Author Feed", "handle card click");
            }
        };
        authorRviewAdapter.setOnAuthorClickListener(authorClickListener);

        authorRview.setAdapter(authorRviewAdapter);
        authorRview.setLayoutManager(authorRviewLayoutManager);
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

    private void loadFirstAuthors() {
        loadFollowedAuthors(); //which then calls loadAuthorData
    }

    private void refreshAuthors() {
        authorCardList.clear();
        authorRviewAdapter.notifyDataSetChanged();
        loadFollowedAuthors(); //which then calls loadAuthorData
    }

    private void createAuthorPullToRefresh() {
        authorPullRefresh = findViewById(R.id.authorSwipeRefresh);
        authorPullRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(FollowedAuthorsActivity.this,
                        "Pulled to refresh!",
                        Toast.LENGTH_SHORT)
                        .show();
                refreshAuthors();
            }
        });
    }
}