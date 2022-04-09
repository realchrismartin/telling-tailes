package com.telling.tailes.adapter;

import android.content.Context;
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
import java.util.function.Consumer;

public class StoryRviewAdapter extends RecyclerView.Adapter<StoryRviewHolder> {
    private ArrayList<StoryRviewCard> storyCardList;
    private StoryRviewCardClickListener listener;
    private Context context;

    public StoryRviewAdapter(ArrayList<StoryRviewCard> storyCardList, Context context) {
        this.storyCardList = storyCardList;
        this.context = context;
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

        if (currentItem.getLovers().contains(AuthUtils.getLoggedInUserID(context))) {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
        } else {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
        }

        holder.loveButton.setText(currentItem.getLovers().size() > 0 ? Integer.toString(currentItem.getLovers().size()) : "");

        holder.loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FBUtils.updateLove(context, currentItem, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) {

                        if(!result)
                        {
                            return; //TODO: indicate to user that love failed?
                        }

                        holder.loveButton.setText(currentItem.getLovers().size() > 0 ? Integer.toString(currentItem.getLovers().size()) : "");

                        if (currentItem.getLovers().contains(AuthUtils.getLoggedInUserID(context))) {
                            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
                        } else {
                            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() { return storyCardList.size(); }
}
