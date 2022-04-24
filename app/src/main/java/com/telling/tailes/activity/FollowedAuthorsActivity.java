package com.telling.tailes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.telling.tailes.R;
import com.telling.tailes.adapter.AuthorRviewAdapter;
import com.telling.tailes.adapter.StoryRviewAdapter;
import com.telling.tailes.card.AuthorRviewCard;
import com.telling.tailes.card.AuthorRviewCardClickListener;
import com.telling.tailes.card.StoryRviewCardClickListener;

import java.util.ArrayList;

public class FollowedAuthorsActivity extends AppCompatActivity {

    private ArrayList<AuthorRviewCard> authorCardList;
    private RecyclerView authorRview;
    private LinearLayoutManager authorRviewLayoutManager;
    private AuthorRviewAdapter authorRviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_authors);

//        authorCardList.add();
    }


    private void createAuthorRecyclerView() {
        authorRviewLayoutManager = new LinearLayoutManager(this);
        authorRview = findViewById(R.id.author_rview);
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
}