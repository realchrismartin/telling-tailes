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
import com.telling.tailes.fragment.AuthorProfileDialogFragment;
import com.telling.tailes.model.AuthorProfile;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FollowedAuthorsActivity extends AppCompatActivity implements OnUnfollowClickCallbackListener {

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    private ArrayList<AuthorRviewCard> authorCardList;
    private RecyclerView authorRview;
    private LinearLayoutManager authorRviewLayoutManager;
    private AuthorRviewAdapter authorRviewAdapter;

    private ArrayList<String> followedAuthorIds;
    private AuthorProfileDialogFragment authorProfileDialogFragment;

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
                    case("unfollowAuthor"): {
                        if (msg.getData()== null || msg.getData().getInt("error") > 0) {
                            toast.setText(R.string.generic_error_notification);
                            toast.show();
                            return;
                        }
                        String removedId = msg.getData().getString("username");
                        for (AuthorRviewCard authorCard : authorCardList) {
                            if (authorCard.getAuthor() == removedId) {
                                authorCardList.remove(authorCard);
                                if (authorCardList.size() == 0) {
                                    authorCardList.add(new AuthorRviewCard(2));
                                    authorPullRefresh.setRefreshing(false);
                                }
                                authorRviewAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                        break;
                    }
                    case ("followedAuthors"): {
                        followedAuthorIds = msg.getData().getStringArrayList("follows");
                        if (followedAuthorIds.size() == 0) {
                            authorCardList.add(new AuthorRviewCard(2));
                            authorRviewAdapter.notifyDataSetChanged();
                            break;
                        }
                        for (String authorId : followedAuthorIds) {
                            authorCardList.add(new AuthorRviewCard(authorId));
                            authorRviewAdapter.notifyDataSetChanged();
                        }
                        authorPullRefresh.setRefreshing(false);
                        break;
                    }

                    case ("authorProfile"): {

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
        authorRviewAdapter = new AuthorRviewAdapter(authorCardList, getApplicationContext(), this);
        AuthorRviewCardClickListener authorClickListener = new AuthorRviewCardClickListener() {
            @Override
            public void onAuthorClick(int position) {
                Log.d("Author list", "handle card click");
                String username = authorCardList.get(position).getAuthor();
                handleAuthorClick(username);
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
                        resultData.putStringArrayList("follows", user.getFollows());

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

    private void handleAuthorClick(String username) {
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

    public void handleUnfollowClick(String username) {
        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FBUtils.updateFollow(getApplicationContext(), username, new Consumer<User>() {
                    @Override
                    public void accept(User user) {
                        Bundle resultData = new Bundle();
                        resultData.putString("type", "unfollowAuthor");
                        resultData.putInt("error", user != null ? 0:1);
                        if (user != null) {
                            resultData.putString("username", username);
                        }
                        Message resultMessage = new Message();
                        resultMessage.setData(resultData);
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });

            }
        });

    }

}