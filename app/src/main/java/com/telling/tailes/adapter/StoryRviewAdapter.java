package com.telling.tailes.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.activity.OnAuthorClickCallbackListener;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.card.StoryRviewCardClickListener;
import com.telling.tailes.model.Story;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

public class StoryRviewAdapter extends RecyclerView.Adapter<StoryRviewHolder> {
    private ArrayList<StoryRviewCard> storyCardList;
    private StoryRviewCardClickListener listener;
    private Context context;
    private String currentUser;
    private OnAuthorClickCallbackListener authorClickCallbackListener;


    public StoryRviewAdapter(ArrayList<StoryRviewCard> storyCardList, Context context, OnAuthorClickCallbackListener authorClickCallbackListener) {
        this.storyCardList = storyCardList;
        this.context = context;
        this.currentUser = AuthUtils.getLoggedInUserID(context);
        this.authorClickCallbackListener = authorClickCallbackListener;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(StoryRviewHolder holder, int position) {

        StoryRviewCard currentItem = storyCardList.get(position);
        holder.titleText.setText(currentItem.getTitle());
        holder.authorText.setText(currentItem.getAuthorId());

        if (currentItem.getStory().getLovers().contains(AuthUtils.getLoggedInUserID(context))) {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
        } else {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
        }

        holder.loveButton.setText(Integer.toString(currentItem.getStory().getLovers().size()));

        holder.loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FBUtils.updateLove(context.getApplicationContext(), currentItem.getStory(), new Consumer<Story>() {
                    @Override
                    public void accept(Story result) {

                        if (result == null) {
                            return; //TODO: indicate to user that love failed?
                        }

                        currentItem.setStory(result);

                        holder.loveButton.setText(result.getLovers().size() > 0 ? Integer.toString(result.getLovers().size()) : "");

                        if (result.getLovers().contains(AuthUtils.getLoggedInUserID(context))) {
                            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
                        } else {
                            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
                        }
                    }
                });
            }
        });

        if (currentItem.getBookmarkers().contains(currentUser)) {
            holder.bookmarkButton.setImageResource(R.drawable.ic_baseline_bookmark_24);
        } else {
            holder.bookmarkButton.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
        }

        holder.bookmarkButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FBUtils.updateBookmark(context.getApplicationContext(), currentItem.getStory(), new Consumer<Story>() {
                    @Override
                    public void accept(Story result) {
                        if (result == null) {
                            return;
                        }
                        if (currentItem.getBookmarkers().contains(currentUser)) {
                            holder.bookmarkButton.setImageResource(R.drawable.ic_baseline_bookmark_24);
                        } else {
                            holder.bookmarkButton.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                        }
                    }
                });
            }
        });


        holder.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authorClickCallbackListener.handleAuthorClick(currentItem.getAuthorId());
            }
        });

    }

    @Override
    public int getItemCount() { return storyCardList.size(); }
}
