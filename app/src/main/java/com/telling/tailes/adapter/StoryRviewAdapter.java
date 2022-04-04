package com.telling.tailes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.card.StoryRviewCardClickListener;

import java.util.ArrayList;

public class StoryRviewAdapter extends RecyclerView.Adapter<StoryRviewHolder> {
    private ArrayList<StoryRviewCard> storyCardList;
    private StoryRviewCardClickListener listener;

    public StoryRviewAdapter(ArrayList<StoryRviewCard> storyCardList) {
        this.storyCardList = storyCardList;
    }

    public void setOnStoryClickListener(StoryRviewCardClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoryRviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_card, parent, false);
        return new StoryRviewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(StoryRviewHolder holder, int position) {
        StoryRviewCard currentItem = storyCardList.get(position);
        holder.testText.setText(currentItem.getVal() + "");
    }

    @Override
    public int getItemCount() { return storyCardList.size(); }
}
