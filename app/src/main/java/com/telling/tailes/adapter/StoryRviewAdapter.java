package com.telling.tailes.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.card.StoryRviewCardClickListener;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

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
        holder.titleText.setText(currentItem.getID());
        holder.authorText.setText(currentItem.getAuthorId());

        if (currentItem.getLovers().contains(AuthUtils.getLoggedInUserID())) {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
        } else {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
        }

        holder.loveButton.setText(currentItem.getLovers().size() > 0 ? currentItem.getLovers().size() + "" : ""); //TODO: better way to make the int a string?
        holder.loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FBUtils.updateLove(currentItem.getStory()); //TODO: this won't work properly, need to use callback and set text/update card from Story after the story is update in updateLove. Deal with this post merge!
                holder.loveButton.setText(currentItem.getLovers().size() > 0 ? currentItem.getLovers().size() + "" : ""); //TODO: better way to make the int a string?
                if (currentItem.getLovers().contains(AuthUtils.getLoggedInUserID())) {
                    holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
                } else {
                    holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
                }
            }
        });
    }

    @Override
    public int getItemCount() { return storyCardList.size(); }
}
